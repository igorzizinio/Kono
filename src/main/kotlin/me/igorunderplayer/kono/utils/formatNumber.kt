package me.igorunderplayer.kono.utils

fun formatNumber(number: Int): String {
    val million = 1_000_000
    val thousand = 1_000

    return when {
        number >= million -> {
            val value = number / million
            return "${value}M"
        }
        number >= thousand -> {
            val value = number / thousand
            return "${value}K"
        }
        else -> number.toString()
    }
}