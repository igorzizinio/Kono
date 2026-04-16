package me.igorunderplayer.kono.domain.card

enum class CardType {
    CHARACTER,
    EQUIPMENT
}

fun CardType.toDisplayName(): String {
    return when (this) {
        CardType.CHARACTER -> "Personagem"
        CardType.EQUIPMENT -> "Equipamento"
    }
}

fun CardType.toDisplayEmoji(): String {
    return when (this) {
        CardType.CHARACTER -> "👤"
        CardType.EQUIPMENT -> "🎒"
    }
}

