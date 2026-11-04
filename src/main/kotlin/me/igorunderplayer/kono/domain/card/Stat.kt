package me.igorunderplayer.kono.domain.card

enum class Stat {
    HP,
    ATK,
    INT,
    DEF
}

enum class StatSource {
    SELF,
    TARGET
}


fun Stat.prettyName(): String {
    return when (this) {
        Stat.HP -> "\uD83D\uDC9A HP"
        Stat.ATK -> "⚔️ ATK"
        Stat.INT -> "🔮 INT"
        Stat.DEF -> "🛡️ DEF"
    }
}

fun prettyValue(stat: Stat, value: Double): String {
    val absValue = kotlin.math.abs(value)

    val formatted = when (stat) {
        else -> {
            if (absValue % 1.0 == 0.0) {
                absValue.toInt().toString()
            } else {
                "%.1f".format(absValue)
            }
        }
    }

    return when {
        value > 0 -> "+$formatted"
        value < 0 -> "-$formatted"
        else -> formatted
    }
}
