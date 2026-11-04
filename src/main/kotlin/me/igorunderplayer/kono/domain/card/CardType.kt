package me.igorunderplayer.kono.domain.card

enum class CardType {
    CHARACTER,
    EQUIPMENT,
    ACTION
}

fun CardType.toDisplayName(): String {
    return when (this) {
        CardType.CHARACTER -> "Personagem"
        CardType.EQUIPMENT -> "Equipamento"
        CardType.ACTION -> "Ação"
    }
}

fun CardType.toDisplayEmoji(): String {
    return when (this) {
        CardType.CHARACTER -> "👤"
        CardType.EQUIPMENT -> "🎒"
        CardType.ACTION -> "🃏"
    }
}
