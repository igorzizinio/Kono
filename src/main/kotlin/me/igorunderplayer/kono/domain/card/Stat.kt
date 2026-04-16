package me.igorunderplayer.kono.domain.card

enum class Stat {
    HP,
    ATK,
    DEF,
    CRIT_CHANCE,
    CRIT_DAMAGE,
    SPEED
}


fun Stat.prettyName(): String {
    return when (this) {
        Stat.HP -> "🩸 HP"
        Stat.ATK -> "⚔️ ATK"
        Stat.DEF -> "🛡️ DEF"
        Stat.CRIT_CHANCE -> "🎯 Crit Chance"
        Stat.CRIT_DAMAGE -> "💥 Crit Damage"
        Stat.SPEED -> "💨 Speed"
    }
}
