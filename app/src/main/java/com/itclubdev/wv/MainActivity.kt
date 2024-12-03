package com.itclubdev.wv

import android.content.Intent
import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val button: Button = findViewById(R.id.login)
        val passwordEditText: EditText = findViewById(R.id.pass)

        button.setOnClickListener {
            val enteredPassword = passwordEditText.text.toString()
            if (enteredPassword == "1234") {
                val intent = Intent(this, WebViewActivity::class.java)
                startActivity(intent)
                startLockTask()
            } else {
                Toast.makeText(this, "Incorrect password", Toast.LENGTH_SHORT).show()
            }
        }
    }

    class WebViewActivity : AppCompatActivity() {
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_webview)

            val myWebView: WebView = findViewById(R.id.webview)
            myWebView.settings.javaScriptEnabled = true
            myWebView.webViewClient = WebViewClient()
            myWebView.loadUrl("https://elearning.man1metro.sch.id")


            val exitButton: Button = findViewById(R.id.exit_button)
            exitButton.setOnClickListener {
                finishAffinity()
                stopLockTask()
            }
        }
    }
}