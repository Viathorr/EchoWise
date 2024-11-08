package com.example.echowise

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

//  TODO: list of simple commands my app can handle:
//  1. Open Camera
//  2. Open Settings
//  3. "What's the time?", "What's the date?"
//  4. Battery Level
//  5. Turn on/off Wi-Fi/Bluetooth
//  6. Set a Reminder (basic)

class MainActivity : AppCompatActivity() {
    private lateinit var recordButton: ImageButton
    private lateinit var responseLog: TextView
    private lateinit var instructionTextView: TextView

    private var dotCount = 0
    private val dots = "..."

    companion object {
        private const val REQUEST_PERMISSIONS_CODE = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        recordButton = findViewById(R.id.recordButton)
        responseLog = findViewById(R.id.responseLog)
        instructionTextView = findViewById(R.id.instruction)

        startTextViewAnimations()
    }

    private fun startTextViewAnimations() {
        val fadeAnimation = android.view.animation.AnimationUtils.loadAnimation(this, R.anim.dots_animation)
        instructionTextView.startAnimation(fadeAnimation)

        val handler = Handler(Looper.getMainLooper())
        val runnable = object : Runnable {
            @SuppressLint("SetTextI18n")
            override fun run() {
                if (dotCount < 3) {
                    responseLog.text = "Waiting for command" + dots.substring(0, ++dotCount)
                } else {
                    dotCount = 0
                    responseLog.text = "Waiting for command"
                }
                handler.postDelayed(this, 500)
            }
        }
        handler.post(runnable)
    }

    private fun startVoiceInput() {
        // Check for permissions
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO))
        } else {
            // TODO: implement voice recording logic
        }
    }

    private fun requestPermissions(permissions: Array<String>) {
        ActivityCompat.requestPermissions(
            this,
            permissions,
            REQUEST_PERMISSIONS_CODE
        )
    }
}