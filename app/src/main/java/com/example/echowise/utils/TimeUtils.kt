package com.example.echowise.utils

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object TimeUtils {
    fun getCurrentTime(): String {
        val formatter = SimpleDateFormat("hh:mm a", Locale.getDefault())
        return "The current time is ${formatter.format(Calendar.getInstance().time)}"
    }

    fun getCurrentDate(): String {
        val formatter = SimpleDateFormat("EEEE, MMMM dd, yyyy", Locale.getDefault())
        return "Today's date is ${formatter.format(Calendar.getInstance().time)}"
    }
}