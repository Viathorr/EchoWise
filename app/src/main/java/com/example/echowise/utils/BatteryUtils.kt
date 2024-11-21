package com.example.echowise.utils

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import com.example.echowise.R

object BatteryUtils {
    fun getBatteryLevel(context: Context): String {
        val batteryIntent = IntentFilter(Intent.ACTION_BATTERY_CHANGED).let { context.registerReceiver(null, it) }
        val level = batteryIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = batteryIntent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        return if (level >= 0 && scale > 0) {
            "${context.getString(R.string.battery_level_message)} ${(level * 100 / scale.toFloat()).toInt()}%."
        } else {
            context.getString(R.string.battery_error_message)
        }
    }
}