package com.example.echowise.logic

import android.content.Context
import android.util.Log
import com.example.echowise.R
import com.example.echowise.utils.BatteryUtils
import com.example.echowise.utils.TimeUtils

class CommandProcessor(private val context: Context, private val deviceController: DeviceController) {

    fun processCommand(command: String): String {
        Log.d("CommandProcessor", "Processing command: $command")
        return when {
            command.contains(context.getString(R.string.command_time), true) -> TimeUtils.getCurrentTime(context)
            command.contains(context.getString(R.string.command_date), true) -> TimeUtils.getCurrentDate(context)
            command.contains(context.getString(R.string.command_battery), true) -> BatteryUtils.getBatteryLevel(context)
            command.contains(context.getString(R.string.command_alarm), true) -> deviceController.setAlarm()
            command.contains(context.getString(R.string.command_settings), true) -> deviceController.openSettings()
            command.contains(context.getString(R.string.command_camera), true) -> deviceController.openCamera()
            command.contains(context.getString(R.string.command_flashlight), true) -> {
                val enable = command.contains(context.getString(R.string.on_keyword), true)
                deviceController.toggleFlashlight(enable)
            }
            command.contains("bluetooth", true) -> {
                val enable = command.contains(context.getString(R.string.on_keyword), true)
                deviceController.toggleBluetooth(enable)
            }
            command.contains(Regex("wi-?fi", RegexOption.IGNORE_CASE)) -> {
                val enable = command.contains(context.getString(R.string.on_keyword), true)
                deviceController.toggleWiFi(enable)
            }
            else -> {
                Log.w("CommandProcessor", "Command not recognized: $command")
                context.getString(R.string.command_not_recognized)
            }
        }
    }
}
