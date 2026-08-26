package me.igorunderplayer.kono.domain.card.ability


enum class AbilityType {
    ACTIVE,
    PASSIVE
}

fun AbilityType.prettyName(): String {
    return when (this) {
        AbilityType.ACTIVE -> "🔵 ATIVA"
        AbilityType.PASSIVE -> "🟢 PASSIVA"
    }
}
