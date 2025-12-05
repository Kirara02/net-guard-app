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

fun formatRelativeTimeValue(timestamp: String): Pair<String, Long> {
    return try {
        val instant = Instant.parse(timestamp)
        val now = Clock.System.now()
        val diff = now - instant

        val seconds = diff.inWholeSeconds
        val minutes = diff.inWholeMinutes
        val hours = diff.inWholeHours
        val days = diff.inWholeDays

        when {
            abs(seconds) < 60 -> "JUST_NOW" to 0
            abs(minutes) < 60 -> "MINUTES" to abs(minutes)
            abs(hours) < 24 -> "HOURS" to abs(hours)
            abs(days) < 7 -> "DAYS" to abs(days)
            else -> "ABSOLUTE" to 0
        }
    } catch (e: Exception) {
        "UNKNOWN" to 0
    }
}

fun formatUtcToLocal(timestamp: String): String {
    return try {
        val instant = Instant.parse(timestamp)
        val dateTime =
            instant.toLocalDateTime(TimeZone.currentSystemDefault())

        val day = dateTime.day.toString().padStart(2, '0')
        val month = dateTime.month
            .name
            .lowercase()
            .replaceFirstChar { it.uppercase() }
            .take(3)

        val year = dateTime.year
        val hour = dateTime.hour.toString().padStart(2, '0')
        val minute = dateTime.minute.toString().padStart(2, '0')

        "$day $month $year, $hour:$minute"
    } catch (e: Exception) {
        "-"
    }
}