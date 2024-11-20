package com.example.echowise

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.hardware.camera2.CameraManager
import android.net.wifi.WifiManager
import android.os.BatteryManager
import android.os.Build
import android.os.Bundle
import android.provider.AlarmClock
import android.provider.MediaStore
import android.provider.Settings
import android.speech.RecognizerIntent
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

//  list of simple commands my app can handle:
//  1. Open Camera  +
//  2. Open Settings  +
//  3. "What's the time?", "What's the date?" +
//  4. Battery Level +
//  5. Turn on/off Wi-Fi/Bluetooth +
//  6. Turn on/off a Flashlight +
//  7. Open an alarm clock app +

class MainActivity : AppCompatActivity() {
    private lateinit var recordButton: ImageButton
    private lateinit var responseLog: TextView
    private lateinit var instructionTextView: TextView

    private val speechRecognizerLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                val matches = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                if (!matches.isNullOrEmpty()) {
                    val command = matches[0].lowercase(Locale.getDefault())
                    handleUserCommand(command)
                } else {
                    Toast.makeText(this, "No speech recognized. Please try again.", Toast.LENGTH_SHORT).show()
                }
            }
        }

    companion object {
        private const val REQUEST_PERMISSIONS_CODE = 100
        private const val REQUEST_IMAGE_CAPTURE = 1
        private const val REQUEST_BLUETOOTH_CONNECT = 12
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        recordButton = findViewById(R.id.recordButton)
        responseLog = findViewById(R.id.responseLog)
        instructionTextView = findViewById(R.id.instruction)

        startFadingAnimation()
        recordButton.setOnClickListener {
            startVoiceInput()
        }
    }

    private fun startFadingAnimation() {
        val fadeAnimation = android.view.animation.AnimationUtils.loadAnimation(this, R.anim.fade_animation)
        instructionTextView.startAnimation(fadeAnimation)
    }

    private fun startVoiceInput() {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO))
        } else {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                putExtra(RecognizerIntent.EXTRA_PROMPT, "Listening... Please say a command.")
            }

            try {
                speechRecognizerLauncher.launch(intent)
            } catch (e: Exception) {
                Toast.makeText(this, "Speech recognition is not supported on this device.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setDefaultText() {
        responseLog.text = "Waiting for command..."
    }

    private fun handleUserCommand(command: String) {
        when {
            command.contains("time", ignoreCase = true) -> {
                val response = "The current time is ${getCurrentDateOrTime("hh:mm a")}"
                responseLog.text = response
            }
            command.contains("alarm", ignoreCase = true) -> {
                setAlarm()
                setDefaultText()
            }
            command.contains("date", ignoreCase = true) -> {
                val response = "Today's date is ${getCurrentDateOrTime("EEEE, MMMM dd, yyyy")}"
                responseLog.text = response
            }
            command.contains("battery", ignoreCase = true) -> {
                val response = getBatteryLevel()?.let { "Battery level is ${it.toInt()}%." } ?: "Unable to retrieve battery level."
                responseLog.text = response
            }
            command.contains(Regex("(on|off)?\\s+flashlight\\s?(on|off)?", RegexOption.IGNORE_CASE)) -> {
                toggleFlashlight(command.contains("on", ignoreCase = true))
            }
            command.contains(Regex("wi-?fi", RegexOption.IGNORE_CASE)) -> {
                if (command.contains("on", ignoreCase = true)) {
                    checkAndToggleWifi("on")
                } else {
                    checkAndToggleWifi("off")
                }
                setDefaultText()
            }
            command.contains("bluetooth", ignoreCase = true) -> {
                if (command.contains("on", ignoreCase = true)) {
                    toggleBluetooth(true)
                } else {
                    toggleBluetooth(false)
                }
                setDefaultText()
            }
            command.contains("settings", ignoreCase = true) -> {
                openSettings()
                responseLog.text = "Opening Settings..."
            }
            command.contains("camera", ignoreCase = true) -> {
                openCamera()
                responseLog.text = "Opening Camera..."
            }
            else -> {
                responseLog.text = "Sorry, I didn't understand that command."

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

    private fun setAlarm() {
        val alarmIntent = Intent(AlarmClock.ACTION_SET_ALARM)
        this.startActivity(alarmIntent)
    }

    private fun getBatteryLevel(): Float? {
        val batteryStatus: Intent? = IntentFilter(Intent.ACTION_BATTERY_CHANGED).let { intentFilter ->
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

    private fun toggleFlashlight(enable: Boolean) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.CAMERA))
        } else {
            val cameraManager: CameraManager = this.getSystemService(Context.CAMERA_SERVICE) as CameraManager
            val cameraId: String = cameraManager.cameraIdList.firstOrNull() ?: ""

            if (enable && cameraId != "") {
                cameraManager.setTorchMode(cameraId, true)
            } else if (!enable && cameraId != "") {
                cameraManager.setTorchMode(cameraId, false)
            } else {
                Toast.makeText(this, "No flashlight is present.", Toast.LENGTH_SHORT).show()
            }
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

    private fun checkAndToggleWifi(state: String) {
        val wifiManager = this.getSystemService(Context.WIFI_SERVICE) as WifiManager

        if (wifiManager.isWifiEnabled && state == "on") {
            Toast.makeText(this, "Wi-Fi is already on.", Toast.LENGTH_SHORT).show()
        } else if (!wifiManager.isWifiEnabled && state == "off") {
            Toast.makeText(this, "Wi-Fi is already off.", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, String.format("Please turn Wi-Fi %s manually.", state), Toast.LENGTH_SHORT).show()
            val intent = Intent(Settings.ACTION_WIFI_SETTINGS)
            this.startActivity(intent)
        }
    }

    private fun toggleBluetooth(enable: Boolean) {
        val bluetoothAdapter: BluetoothAdapter? = (this.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter

        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not supported on this device.", Toast.LENGTH_SHORT).show()
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this as Activity, arrayOf(Manifest.permission.BLUETOOTH_CONNECT), REQUEST_BLUETOOTH_CONNECT)
                return
            }
        }

        if (enable) {
            if (!bluetoothAdapter.isEnabled) {
                val enableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                this.startActivity(enableIntent)
                Toast.makeText(this, "Turning on Bluetooth...", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Bluetooth is already on.", Toast.LENGTH_SHORT).show()
            }
        } else {
            if (bluetoothAdapter.isEnabled) {
                bluetoothAdapter.disable()
                Toast.makeText(this, "Turning off Bluetooth...", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Bluetooth is already off.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}