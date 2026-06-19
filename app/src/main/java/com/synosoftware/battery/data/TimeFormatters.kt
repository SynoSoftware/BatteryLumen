package com.synosoftware.battery.data

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun formatTimeRange(startMs: Long, endMs: Long?): String {
    val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
    val start = formatter.format(Date(startMs))
    val end = endMs?.let { formatter.format(Date(it)) } ?: ""
    return "$start–$end"
}

fun formatDuration(ms: Long): String {
    val totalMinutes = (ms / 60_000L).coerceAtLeast(0L)
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    return when {
        hours > 0 -> "${hours}h ${minutes}m"
        else -> "${minutes}m"
    }
}
