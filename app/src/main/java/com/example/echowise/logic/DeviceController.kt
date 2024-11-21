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
        Log.d("DeviceController", "toggleFlashlight called with enable=$enable")
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            Log.w("DeviceController", "Camera permission not granted, requesting permission")
            ActivityCompat.requestPermissions(context as Activity, arrayOf(Manifest.permission.CAMERA), REQUEST_PERMISSIONS_CODE)
        }

        val cameraManager: CameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        val cameraId: String = cameraManager.cameraIdList.firstOrNull() ?: ""

        return try {
            cameraManager.setTorchMode(cameraId, enable)
            Log.i("DeviceController", "Flashlight turned ${if (enable) "on" else "off"}.")
            "Turning ${if (enable) "on" else "off"} flashlight..."
        } catch (e: Exception) {
            Log.e("DeviceController", "Error toggling flashlight: ${e.message}")
            "Failed to toggle flashlight."
        }
    }

    fun toggleBluetooth(enable: Boolean): String {
        Log.d("DeviceController", "toggleBluetooth called with enable=$enable")
        val bluetoothAdapter: BluetoothAdapter? = (context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter

        if (bluetoothAdapter == null) {
            Log.e("DeviceController", "Bluetooth is not supported on this device.")
            Toast.makeText(context, "Bluetooth is not supported on this device.", Toast.LENGTH_SHORT).show()
            return "Bluetooth is not supported."
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                Log.w("DeviceController", "Bluetooth connect permission not granted, requesting permission")
                ActivityCompat.requestPermissions(context as Activity, arrayOf(Manifest.permission.BLUETOOTH_CONNECT), REQUEST_PERMISSIONS_CODE)
            }
        }

        return if (enable) {
            if (!bluetoothAdapter.isEnabled) {
                val enableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                context.startActivity(enableIntent)
                Log.i("DeviceController", "Bluetooth enabling process initiated.")
                "Turning on Bluetooth..."
            } else {
                "Bluetooth is already on."
            }
        } else {
            if (bluetoothAdapter.isEnabled) {
                bluetoothAdapter.disable()
                Log.i("DeviceController", "Bluetooth disabling process initiated.")
                "Turning off Bluetooth..."
            } else {
                "Bluetooth is already off."
            }
        }
    }

    fun toggleWiFi(enable: Boolean): String {
        Log.d("DeviceController", "toggleWiFi called with enable=$enable")

        return try {
            val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager

            if ((wifiManager.isWifiEnabled && enable) || (!wifiManager.isWifiEnabled && !enable)) {
                Log.d("DeviceController", "Wi-Fi is already ${if (enable) "on" else "off"}.")
                "Wi-Fi is already ${if (enable) "on" else "off"}."
            } else {
                Toast.makeText(context, String.format("Please turn Wi-Fi %s manually.", if (enable) "on" else "off"), Toast.LENGTH_SHORT).show()
                val intent = Intent(Settings.ACTION_WIFI_SETTINGS)
                context.startActivity(intent)
                Log.i("DeviceController", "Redirecting to Wi-Fi settings.")
                String.format("Turning %s Wi-Fi...", if (enable) "on" else "off")
            }
        } catch (e: Exception) {
            Log.e("DeviceController", "Error opening settings: ${e.message}")
            "Failed to open settings."
        }
    }

    fun setAlarm(): String {
        Log.d("DeviceController", "setAlarm called")
        return try {
            val alarmIntent = Intent(AlarmClock.ACTION_SET_ALARM)

            if(alarmIntent.resolveActivity(context.packageManager) != null) {
                context.startActivity(alarmIntent)
                Log.i("DeviceController", "Alarm app opened successfully.")
                "Setting alarm..."
            } else {
                Log.e("DeviceController", "No alarm app found.")
                "No alarm app found :("
            }
        } catch (e: Exception) {
            Log.e("DeviceController", "Error opening alarm app: ${e.message}")
            "Failed to open alarm app."
        }
    }

    fun openCamera(): String {
        Log.d("DeviceController", "openCamera called")
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            Log.w("DeviceController", "Camera permission not granted, requesting permission")
            ActivityCompat.requestPermissions(context as Activity, arrayOf(Manifest.permission.CAMERA), REQUEST_PERMISSIONS_CODE)
        }

        return try {
            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

            if (cameraIntent.resolveActivity(context.packageManager) != null) {
                context.startActivity(cameraIntent)
                Log.i("DeviceController", "Camera app opened successfully.")
                "Opening camera..."
            } else {
                Log.e("DeviceController", "No camera app found.")
                "No camera app found :("
            }
        } catch(e: Exception) {
            Log.e("DeviceController", "Error opening camera: ${e.message}")
            "Error opening camera: ${e.message}"
        }
    }

    fun openSettings(): String {
        Log.d("DeviceController", "openSettings called")
        return try {
            val settingsIntent = Intent(Settings.ACTION_SETTINGS)

            if (settingsIntent.resolveActivity(context.packageManager) != null) {
                context.startActivity(settingsIntent)
                Log.i("DeviceController", "Settings opened successfully.")
                "Opening settings..."
            } else {
                Log.e("DeviceController", "Unable to open settings.")
                "Unable to open settings :("
            }
        } catch (e: Exception) {
            Log.e("DeviceController", "Error opening settings: ${e.message}")
            "Error opening settings: ${e.message}"
        }
    }
}