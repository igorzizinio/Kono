package me.igorunderplayer.kono.utils

fun formatNumber(number: Int): String {
    if (number == 0) return "0"

    val million = 1_000_000L
    val thousand = 1_000L

    return when {
        number >= million -> {
            val value = number / million.toDouble()
            String.format("%.1fM", value)
        }
        number >= thousand -> {
            val value = number / thousand.toDouble()
            String.format("%.1fK", value)
        }
        else -> number.toString()
    }
}