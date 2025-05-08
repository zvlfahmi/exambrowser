package com.itclubdev.wv

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
import android.view.WindowManager
import android.webkit.CookieManager
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import android.view.WindowManager.LayoutParams
import java.time.Duration
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val disableFile = File(filesDir, "disable.txt")
        if (disableFile.exists()) {
            val timestampString = disableFile.readText()
            showLockedScreen(disableFile, timestampString)
        } else {
            initView()
        }
    }

    private var secretButtonTapCount = 0
    private lateinit var bypassButton: Button
    private lateinit var bypassEditText: EditText


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
            val enteredPassword = passwordEditText.text.toString()
            if (enteredPassword == "bypasslockdown") {
                Toast.makeText(this, "Bypassed", Toast.LENGTH_SHORT).show()
                disableFile.delete();
                initView();

            } else {
                Toast.makeText(this, "Password Salah", Toast.LENGTH_SHORT).show()
            }
        }
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
        myWebView.loadUrl("https://zvlfahmi.github.io")

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