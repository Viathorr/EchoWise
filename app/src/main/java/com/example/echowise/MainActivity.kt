package com.example.echowise

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.BatteryManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.provider.Settings
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

//  TODO: list of simple commands my app can handle:
//  1. Open Camera  +
//  2. Open Settings
//  3. "What's the time?", "What's the date?" +
//  4. Battery Level +
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
        private const val REQUEST_IMAGE_CAPTURE = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        recordButton = findViewById(R.id.recordButton)
        responseLog = findViewById(R.id.responseLog)
        instructionTextView = findViewById(R.id.instruction)

        startTextViewAnimations()
        recordButton.setOnClickListener {
            startVoiceInput()
//            openCamera()
//            openSettings()
        }
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

    private fun handleUserCommand(command: String) {
        when {
            command.contains("time", ignoreCase = true) -> {
                val response = "The current time is ${getCurrentDateOrTime("hh:mm a")}"
                // TODO: add other logic
            }
            command.contains("date", ignoreCase = true) -> {
                val response = "Today's date is ${getCurrentDateOrTime("EEEE, MMMM dd, yyyy")}"
                // TODO: add other logic
            }
            command.contains("battery", ignoreCase = true) -> {
                val response = getBatteryLevel()?.let { "Battery level is at ${it.toInt()}%." } ?: "Unable to retrieve battery level."
                // TODO: add other logic
            }
        }
    }

    private fun requestPermissions(permissions: Array<String>) {
        ActivityCompat.requestPermissions(
            this,
            permissions,
            REQUEST_PERMISSIONS_CODE
        )
    }

    private fun getCurrentDateOrTime(format: String): String {
        val calendar = Calendar.getInstance()
        val formatter = SimpleDateFormat(format, Locale.getDefault())
        return formatter.format(calendar.time)
    }

    private fun getBatteryLevel(): Float? {
        var batteryStatus: Intent? = IntentFilter(Intent.ACTION_BATTERY_CHANGED).let { intentFilter ->
            this.registerReceiver(null, intentFilter)
        };

        return batteryStatus?.let { intent ->
            val level: Int = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale: Int = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
            if (level >= 0 && scale > 0) {
                level * 100 / scale.toFloat()
            } else null
        }
    }

    private fun openCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.CAMERA))
        } else {
            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

            if (cameraIntent.resolveActivity(this.packageManager) != null) {
                this.startActivityIfNeeded(cameraIntent, REQUEST_IMAGE_CAPTURE)
            } else {
                Toast.makeText(this, "No camera app found :(", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun openSettings() {
        try {
            val settingsIntent = Intent(Settings.ACTION_SETTINGS)

            if (settingsIntent.resolveActivity(this.packageManager) != null) {
                this.startActivity(settingsIntent)
            } else {
                Toast.makeText(this, "Unable to open settings :(", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error opening settings: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}