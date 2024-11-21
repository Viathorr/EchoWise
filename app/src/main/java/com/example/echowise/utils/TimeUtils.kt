package com.example.echowise.utils

import android.content.Context
import com.example.echowise.R
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object TimeUtils {
    fun getCurrentTime(context: Context): String {
        val formatter = SimpleDateFormat("hh:mm a", Locale.getDefault())
        return "${context.getString(R.string.current_time_message)} ${formatter.format(Calendar.getInstance().time)}"
    }

    fun getCurrentDate(context: Context): String {
        val formatter = SimpleDateFormat("EEEE, MMMM dd, yyyy", Locale.getDefault())
        return "${context.getString(R.string.current_date_message)} ${formatter.format(Calendar.getInstance().time)}"
    }
}