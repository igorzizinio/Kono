package me.igorunderplayer.kono.utils

import kotlin.time.Duration

fun humanizeDuration(duration: Duration): String {
    val parts = mutableListOf<String>()

    duration.toComponents { days, hours, minutes, seconds, _ ->
        if (days > 0) parts.add("${days}d")
        if (hours > 0) parts.add("${hours}h")
        if (minutes > 0) parts.add("${minutes}m")
        if (seconds > 0) parts.add("${seconds}s")
    }

    return parts.joinToString(" ")
}