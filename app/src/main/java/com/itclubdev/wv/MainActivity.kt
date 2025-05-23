package com.itclubdev.wv

import android.os.PowerManager
import android.content.pm.PackageManager
import android.app.usage.UsageStatsManager
import android.annotation.SuppressLint
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraManager
import android.bluetooth.BluetoothAdapter
import android.graphics.Color
import android.content.Context
import android.content.Intent
import android.view.KeyEvent
import android.widget.TextView
import android.view.View
import android.media.AudioManager
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.view.WindowManager
import android.provider.Settings
import android.webkit.CookieManager
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import android.view.WindowManager.LayoutParams
import java.time.Duration
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import androidx.core.net.toUri
import kotlinx.coroutines.DelicateCoroutinesApi
import java.net.URL

class MainActivity : AppCompatActivity() {

    private val handler = Handler()
    private lateinit var runnable: Runnable
    private lateinit var bannedapp: List<String>

    @OptIn(DelicateCoroutinesApi::class)
    @SuppressLint("BatteryLife")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize bannedapp as an empty list first
        bannedapp = emptyList()

        // Coroutine to fetch and save banned apps
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val url = URL("https://zvlfahmi.github.io/banned-apps.txt")
                val content = url.readText()
                val blacklistFile = File(filesDir, "blacklist")
                blacklistFile.writeText(content)

                launch(Dispatchers.Main) {
                    bannedapp = blacklistFile.readLines()
                    continueOnCreate()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                launch(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Gagal mengupdate database, cek koneksi internet", Toast.LENGTH_LONG).show()
                    val blacklistFile = File(filesDir, "blacklist")
                    if (blacklistFile.exists()) {
                        bannedapp = blacklistFile.readLines()
                    }
                    continueOnCreate()
                }
            }
        }
    }

    @SuppressLint("BatteryLife")
    private fun continueOnCreate() {

        if (!isUsageAccessGranted()) {
            val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
            startActivity(intent)
            Toast.makeText(this@MainActivity, "Izinkan penggunaan aplikasi", Toast.LENGTH_SHORT).show()
        }

        val packageName = packageName
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        if (!powerManager.isIgnoringBatteryOptimizations(packageName)) {
            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
            intent.data = "package:$packageName".toUri()
            startActivity(intent)
            Toast.makeText(this@MainActivity, "Pilih Tidak ada pembatasan", Toast.LENGTH_SHORT).show()
        }

        runnable = object : Runnable {
            override fun run() {
                val usageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
                val time = System.currentTimeMillis()
                val stats = usageStatsManager.queryEvents(time - 1000 * 10, time)
                val event = android.app.usage.UsageEvents.Event()
                while (stats.hasNextEvent()) {
                    stats.getNextEvent(event)
                    if (event.eventType == android.app.usage.UsageEvents.Event.MOVE_TO_FOREGROUND) {

                        if (::bannedapp.isInitialized && event.packageName in bannedapp) {
                            val disableFile = File(filesDir, "disable.txt")
                            if (!disableFile.exists()) {
                                val currentDateTime = LocalDateTime.now()
                                val formatter = DateTimeFormatter.ISO_DATE_TIME
                                val formattedDateTime = currentDateTime.format(formatter)
                                disableFile.writeText(formattedDateTime)
                                finishAffinity()
                            }
                        }
                    }
                }
                handler.postDelayed(this, 100)
            }

        }
        handler.post(runnable)

        val disableFile = File(filesDir, "disable.txt")

        if (disableFile.exists()) {
            val timestampString = disableFile.readText()
            showLockedScreen(disableFile, timestampString)
        } else {
            initView()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(runnable)
    }

    private var secretButtonTapCount = 0
    private lateinit var bypassButton: Button
    private lateinit var bypassEditText: EditText

    @OptIn(DelicateCoroutinesApi::class)
    private fun showLockedScreen(disableFile: File, timestampString: String) {
        setContentView(R.layout.activity_locked)
        val timeLeftTextView: TextView = findViewById(R.id.timeremaining)
        val formatter = DateTimeFormatter.ISO_DATE_TIME
        val disableTime = LocalDateTime.parse(timestampString, formatter)
        val now = LocalDateTime.now()
        val remainingTime = Duration.between(now, disableTime.plusMinutes(15)).toMillis()

        if (remainingTime > 0) {
            val timer = object : android.os.CountDownTimer(remainingTime, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    val remainingDuration = Duration.ofMillis(millisUntilFinished)
                    val minutes = remainingDuration.toMinutes()
                    val seconds = remainingDuration.minusMinutes(minutes).seconds
                    val timeLeftString = String.format("%02d:%02d", minutes, seconds)
                    timeLeftTextView.text = timeLeftString
                }

                override fun onFinish() {
                    disableFile.delete()
                    initView()
                }
            }

            timer.start()
        } else {
            disableFile.delete();
            initView();

        }

        val secretButton: Button = findViewById(R.id.secretbutton)
        bypassButton = findViewById(R.id.bypassbutton)
        bypassEditText = findViewById(R.id.bypass)
        bypassButton.visibility = View.GONE
        bypassEditText.visibility = View.GONE

        secretButton.setBackgroundColor(Color.TRANSPARENT)
        secretButton.setOnClickListener {
            secretButtonTapCount++
            if (secretButtonTapCount >= 5) {
                bypassButton.visibility = View.VISIBLE
                bypassEditText.visibility = View.VISIBLE
                secretButtonTapCount = 0
            }
        }


        val button: Button = bypassButton
        val passwordEditText: EditText = bypassEditText

        button.setBackgroundColor(Color.TRANSPARENT)
        button.setTextColor(Color.WHITE)

        passwordEditText.setBackgroundColor(Color.BLACK)
        passwordEditText.setTextColor(Color.WHITE)

        button.setOnClickListener {
            val enteredPassword = passwordEditText.text.toString().trim()
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    val url = URL("https://zvlfahmi.github.io/bypass-lockdown-pass.txt")
                    val remotePassword = url.readText().trim()
                    launch(Dispatchers.Main) {
                        if (enteredPassword == remotePassword) {
                            Toast.makeText(this@MainActivity, "Bypassed", Toast.LENGTH_SHORT).show()
                            disableFile.delete()
                            initView()
                        } else {
                            Toast.makeText(this@MainActivity, "Password Salah", Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    launch(Dispatchers.Main) {
                        Toast.makeText(this@MainActivity, "Gagal mengupdate database", Toast.LENGTH_SHORT).show()
                    }
                    e.printStackTrace()
                }
            }        }
    }

    private fun initView() {
        setContentView(R.layout.activity_main)
        val mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled) {
            finishAffinity()
        }
        val button: Button = findViewById(R.id.login)
        val passwordEditText: EditText = findViewById(R.id.pass)

        button.setOnClickListener {
            val enteredPassword = passwordEditText.text.toString()
            if (enteredPassword == "112244") {
                val intent = Intent(this, WebViewActivity::class.java)
                startActivity(intent)

            } else {
                Toast.makeText(this, "Password Salah", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun isUsageAccessGranted(): Boolean {
        return try {
            val packageManager = packageManager
            val applicationInfo = packageManager.getApplicationInfo(packageName, 0)
            val appOpsManager = getSystemService(Context.APP_OPS_SERVICE) as android.app.AppOpsManager
            val mode = appOpsManager.checkOpNoThrow(android.app.AppOpsManager.OPSTR_GET_USAGE_STATS, applicationInfo.uid, applicationInfo.packageName)
            mode == android.app.AppOpsManager.MODE_ALLOWED
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }
}

class WebViewActivity : AppCompatActivity() {
    private lateinit var mediaPlayer: MediaPlayer
    private var exit = false
    private lateinit var cameraId: String
    private val cameraManager: CameraManager by lazy {
        getSystemService(Context.CAMERA_SERVICE) as CameraManager
    }

    private fun playExitSound() {
        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val result = audioManager.requestAudioFocus(
            { _ ->

            }, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT
        )

            val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxVolume, 0)
            mediaPlayer.start()

            mediaPlayer.setOnCompletionListener {
                audioManager.abandonAudioFocus(null)
            }
    }


    override fun onStop() {
        if (!exit) {
            mediaPlayer = MediaPlayer.create(this, R.raw.buzzer)
            playExitSound()

            window.clearFlags(LayoutParams.FLAG_KEEP_SCREEN_ON)
            val disableFile = File(filesDir, "disable.txt")
            if (!disableFile.exists()) {
                val currentDateTime = LocalDateTime.now()
                val formatter = DateTimeFormatter.ISO_DATE_TIME
                val formattedDateTime = currentDateTime.format(formatter)
                disableFile.writeText(formattedDateTime)
            }
            try {
                val cameraIds = cameraManager.cameraIdList
                if (cameraIds.isNotEmpty()) {
                    cameraId = cameraIds[0]
                    cameraManager.setTorchMode(cameraId, true)
                }

            } catch (e: CameraAccessException) {
                e.printStackTrace()
            }
            finishAffinity()
            super.onStop()
        } else {
            super.onStop()

        }
    }

    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        // no exit
        playExitSound()
    }


    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_HOME) {
            return true
        }
        if (keyCode == KeyEvent.KEYCODE_APP_SWITCH) {
            return true
        }
        return super.onKeyDown(keyCode, event)
    }



    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        startLockTask()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_webview)
        val myWebView: WebView = findViewById(R.id.webview)
        mediaPlayer = MediaPlayer.create(this, R.raw.alert)
        //prevent lockscreen
        window.addFlags(LayoutParams.FLAG_KEEP_SCREEN_ON)
        window.addFlags(LayoutParams.FLAG_DISMISS_KEYGUARD)
        window.addFlags(LayoutParams.FLAG_SHOW_WHEN_LOCKED)
        //disable screenshot and recording
        window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)

        // Hide the navigation bar and make the app full-screen
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        or View.SYSTEM_UI_FLAG_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                )

        myWebView.settings.javaScriptEnabled = true
        myWebView.webViewClient = WebViewClient()
        myWebView.loadUrl("https://elearning.man1metro.sch.id")

        val refreshButton: Button = findViewById(R.id.refresh)
        refreshButton.setOnClickListener {
            // Reload the current URL in the WebView
            myWebView.reload()
        }



        val exitButton: Button = findViewById(R.id.exit)
        val exitPasswordEditText: EditText = findViewById(R.id.exitpass)

        exitButton.setOnClickListener {
            val enteredPassword = exitPasswordEditText.text.toString()
            if(enteredPassword == "exitexambrowser"){
                exit = true
                playExitSound()
                stopLockTask()
                CookieManager.getInstance().removeAllCookies(null)
                finishAffinity()
            }

        }
    }
}