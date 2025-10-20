package com.uniguard.netguard_app.utils

import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import kotlin.math.abs

fun formatRelativeTime(timestamp: String): String {
    return try {
        val instant = Instant.parse(timestamp)
        val now = Clock.System.now()
        val diff = now - instant

        val seconds = diff.inWholeSeconds
        val minutes = diff.inWholeMinutes
        val hours = diff.inWholeHours
        val days = diff.inWholeDays

        when {
            abs(seconds) < 60 -> "Just now"
            abs(minutes) < 60 -> "${abs(minutes)}m ago"
            abs(hours) < 24 -> "${abs(hours)}h ago"
            abs(days) < 7 -> "${abs(days)}d ago"
            else -> {
                val dateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
                "${dateTime.day}/${dateTime.month.number}/${dateTime.year}"
            }
        }
    } catch (e: Exception) {
        "Unknown time"
    }
}

fun getCurrentTimestamp(): String {
    return Clock.System.now().toString()
}