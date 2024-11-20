package com.example.echowise.logic

import android.content.Context
import com.example.echowise.utils.BatteryUtils
import com.example.echowise.utils.TimeUtils

class CommandProcessor(private val context: Context, private val deviceController: DeviceController) {

    fun processCommand(command: String): String {
        return when {
            command.contains("time", true) -> TimeUtils.getCurrentTime()
            command.contains("date", true) -> TimeUtils.getCurrentDate()
            command.contains("battery", true) -> BatteryUtils.getBatteryLevel(context)
            command.contains("alarm", true) -> deviceController.setAlarm()
            command.contains("flashlight", true) -> {
                val enable = command.contains("on", true)
                deviceController.toggleFlashlight(enable)
            }
            command.contains("bluetooth", true) -> {
                val enable = command.contains("on", true)
                deviceController.toggleBluetooth(enable)
            }
            command.contains("wifi", true) -> {
                val enable = command.contains("on", true)
                deviceController.toggleWiFi(enable)
            }
            command.contains("bluetooth", ignoreCase = true) -> {
                val enable = command.contains("on", true)
                deviceController.toggleBluetooth(enable)
            }
            command.contains("settings", ignoreCase = true) -> deviceController.openSettings()
            command.contains("camera", ignoreCase = true) -> deviceController.openCamera()
            else -> "Command not recognized. Please try again."
        }
    }
}
