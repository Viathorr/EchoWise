package com.example.echowise.logic

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.camera2.CameraManager
import android.net.wifi.WifiManager
import android.os.Build
import android.provider.AlarmClock
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.echowise.utils.AppConstants.REQUEST_PERMISSIONS_CODE

class DeviceController(private val context: Context) {
    fun toggleFlashlight(enable: Boolean): String {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(context as Activity, arrayOf(Manifest.permission.CAMERA), REQUEST_PERMISSIONS_CODE)
        }

        val cameraManager: CameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        val cameraId: String = cameraManager.cameraIdList.firstOrNull() ?: ""

        if (enable && cameraId != "") {
            cameraManager.setTorchMode(cameraId, true)
            return "Turning on flashlight..."
        } else if (!enable && cameraId != "") {
            cameraManager.setTorchMode(cameraId, false)
            return "Turning off flashlight..."
        } else {
            Log.e("HardwareController", "Failed to toggle flashlight: No flashlight is available")
            return "No flashlight is present."
        }
    }

    fun toggleBluetooth(enable: Boolean): String {
        val bluetoothAdapter: BluetoothAdapter? = (context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter

        if (bluetoothAdapter == null) {
            Log.e("HardwareController", "Failed to toggle Bluetooth: Bluetooth is not supported on this device.")
            Toast.makeText(context, "Bluetooth is not supported on this device.", Toast.LENGTH_SHORT).show()
            return "Bluetooth is not supported."
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(context as Activity, arrayOf(Manifest.permission.BLUETOOTH_CONNECT), REQUEST_PERMISSIONS_CODE)
            }
        }

        if (enable) {
            if (!bluetoothAdapter.isEnabled) {
                val enableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                context.startActivity(enableIntent)
                Log.i("HardwareController", "Turned on Bluetooth.")
                return "Turning on Bluetooth..."
            } else {
                return "Bluetooth is already on."
            }
        } else {
            if (bluetoothAdapter.isEnabled) {
                bluetoothAdapter.disable()
                Log.i("HardwareController", "Turned off Bluetooth.")
                return "Turning off Bluetooth..."
            } else {
                return "Bluetooth is already off."
            }
        }
    }

    fun toggleWiFi(enable: Boolean): String {
        val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager

        if (wifiManager.isWifiEnabled && enable) {
            return "Wi-Fi is already on."
        } else if (!wifiManager.isWifiEnabled && enable) {
            return "Wi-Fi is already off."
        } else {
            Toast.makeText(context, String.format("Please turn Wi-Fi %s manually.", if (enable) "on" else "off"), Toast.LENGTH_SHORT).show()
            val intent = Intent(Settings.ACTION_WIFI_SETTINGS)
            context.startActivity(intent)
            return String.format("Turning %s Wi-Fi...", if (enable) "on" else "off")
        }
    }

    fun setAlarm(): String {
        val alarmIntent = Intent(AlarmClock.ACTION_SET_ALARM)
        context.startActivity(alarmIntent)
        return "Setting alarm..."
    }

    fun openCamera(): String {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(context as Activity, arrayOf(Manifest.permission.CAMERA), REQUEST_PERMISSIONS_CODE)
        }

        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        if (cameraIntent.resolveActivity(context.packageManager) != null) {
            context.startActivity(cameraIntent)
            return "Opening camera..."
        } else {
            return "No camera app found :("
        }
    }

    fun openSettings(): String {
        try {
            val settingsIntent = Intent(Settings.ACTION_SETTINGS)

            if (settingsIntent.resolveActivity(context.packageManager) != null) {
                context.startActivity(settingsIntent)
                return "Opening settings..."
            } else {
                return "Unable to open settings :("
            }
        } catch (e: Exception) {
            return "Error opening settings: ${e.message}"
        }
    }
}