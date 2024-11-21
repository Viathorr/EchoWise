package com.example.echowise.logic

import android.content.Context
import android.util.Log
import com.example.echowise.utils.BatteryUtils
import com.example.echowise.utils.TimeUtils

class CommandProcessor(private val context: Context, private val deviceController: DeviceController) {

    fun processCommand(command: String): String {
        Log.d("CommandProcessor", "Processing command: $command")
        return when {
            command.contains("time", true) -> TimeUtils.getCurrentTime()
            command.contains("date", true) -> TimeUtils.getCurrentDate()
            command.contains("battery", true) -> BatteryUtils.getBatteryLevel(context)
            command.contains("alarm", true) -> deviceController.setAlarm()
            command.contains("settings", true) -> deviceController.openSettings()
            command.contains("camera", true) -> deviceController.openCamera()
            command.contains("flashlight", true) -> {
                val enable = command.contains("on", true)
                deviceController.toggleFlashlight(enable)
            }
            command.contains("bluetooth", true) -> {
                val enable = command.contains("on", true)
                deviceController.toggleBluetooth(enable)
            }
            command.contains(Regex("wi-?fi", RegexOption.IGNORE_CASE)) -> {
                val enable = command.contains("on", true)
                deviceController.toggleWiFi(enable)
            }
            else -> {
                Log.w("CommandProcessor", "Command not recognized: $command")
                "Command not recognized. Please try again."
            }
        }
    }
}
