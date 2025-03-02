package com.itclubdev.wv

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.view.KeyEvent
import android.view.View


import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Bundle
import android.webkit.CookieManager
import android.view.WindowManager.LayoutParams
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (mBluetoothAdapter == null) {
            // Device does not support Bluetooth
        } else if (!mBluetoothAdapter.isEnabled) {
            // Bluetooth is not enabled :)
        } else {
            Toast.makeText(this, "Matikan Bluetooth", Toast.LENGTH_SHORT).show()
            finishAffinity()
        }
        val button: Button = findViewById(R.id.login)
        val passwordEditText: EditText = findViewById(R.id.pass)

        button.setOnClickListener {
            val enteredPassword = passwordEditText.text.toString()
            if (enteredPassword == "1234") {
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
    var exit = false
    private fun playExitSound() {
        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val result = audioManager.requestAudioFocus(
            { _ ->

            }, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT
        )

        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxVolume, 0)
            mediaPlayer.start()

            mediaPlayer.setOnCompletionListener {
                audioManager.abandonAudioFocus(null)
            }
        }
    }


    override fun onStop() {
        if (exit == false) {
            window.clearFlags(LayoutParams.FLAG_KEEP_SCREEN_ON)
            mediaPlayer = MediaPlayer.create(this, R.raw.buzzer)
            playExitSound()
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
        exitButton.setOnClickListener {
            exit = true
            playExitSound()
            stopLockTask()
            CookieManager.getInstance().removeAllCookies(null)
            finishAffinity()
           // shutdownDevice()
        }
    }
}

    