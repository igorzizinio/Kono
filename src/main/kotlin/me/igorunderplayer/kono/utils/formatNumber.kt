package me.igorunderplayer.kono.utils

const val million = 1_000_000
const val thousand = 1_000

fun formatNumber(number: Int): String {
    return when {
        number >= million -> {
            val value = number / million

            "${value}M"
        }
        number >= thousand -> {
            val value = number / thousand

            "${value}K"
        }
        else -> number.toString()
    }
}