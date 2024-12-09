package com.itclubdev.wv

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Bundle
import android.webkit.CookieManager
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Composable

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
                startLockTask()
            } else {
                Toast.makeText(this, "Password Salah", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

class WebViewActivity : AppCompatActivity() {
    private lateinit var mediaPlayer: MediaPlayer

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_webview)
        mediaPlayer = MediaPlayer.create(this, R.raw.alert) //add that annoying tinung sound
        val myWebView: WebView = findViewById(R.id.webview)
        myWebView.settings.javaScriptEnabled = true
        myWebView.webViewClient = WebViewClient()
        myWebView.loadUrl("https://elearning.man1metro.sch.id")


        val exitButton: Button = findViewById(R.id.exit)
        exitButton.setOnClickListener {
            playExitSound()
            stopLockTask()
            CookieManager.getInstance().removeAllCookies(null)
            finishAffinity()
        }
    }
}

