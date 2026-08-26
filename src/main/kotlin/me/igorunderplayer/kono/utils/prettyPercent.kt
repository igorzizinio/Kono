package me.igorunderplayer.kono.utils

fun prettyPercent(value: Double): String {
    return if (value % 1.0 == 0.0) {
        "${(value * 100).toInt()}%"
    } else {
        "${"%.1f".format(value * 100)}%"
    }
}
