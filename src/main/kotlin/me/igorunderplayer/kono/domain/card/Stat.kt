package me.igorunderplayer.kono.domain.card

import java.util.Locale
import kotlin.math.roundToInt

enum class Stat {
    HP,
    ATK,
    DEF,
    CRIT_CHANCE,
    CRIT_DAMAGE,
    SPEED,
    LIFESTEAL
}

enum class StatSource {
    SELF,
    TARGET
}


fun Stat.prettyName(): String {
    return when (this) {
        Stat.HP -> "\uD83D\uDC9A HP"
        Stat.ATK -> "⚔️ ATK"
        Stat.DEF -> "🛡️ DEF"
        Stat.CRIT_CHANCE -> "🎯 Crit Chance"
        Stat.CRIT_DAMAGE -> "💥 Crit Damage"
        Stat.SPEED -> "💨 Speed"
        Stat.LIFESTEAL -> "🩸 Roubo de vida"
    }
}

fun prettyValue(stat: Stat, value: Double): String {
    return when (stat) {
        Stat.CRIT_CHANCE -> "${(value * 100).roundToInt()}%"
        Stat.CRIT_DAMAGE -> "+${((value - 1) * 100).roundToInt()}%"
        Stat.LIFESTEAL -> "${(value * 100).roundToInt()}%"
        else -> {
            if (value % 1.0 == 0.0) value.toInt().toString()
            else "%.1f".format(Locale.US, value)
        }
    }
}
