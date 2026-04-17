package me.igorunderplayer.kono.domain.card

import dev.kord.common.Color

enum class Rarity {
    COMMON,
    RARE,
    EPIC,
    LEGENDARY,
    MYTHIC,
    KONO
}


fun Rarity.toDisplayName(): String {
    return when (this) {
        Rarity.COMMON -> "Comum"
        Rarity.RARE -> "Rara"
        Rarity.EPIC -> "Épica"
        Rarity.LEGENDARY -> "Lendária"
        Rarity.MYTHIC -> "Mítica"
        Rarity.KONO -> "Kono"
    }
}

fun Rarity.toDisplayEmoji(): String {
    return when (this) {
        Rarity.COMMON -> "▫️"
        Rarity.RARE -> "🔹"
        Rarity.EPIC -> "🟣"
        Rarity.LEGENDARY -> "🟠"
        Rarity.MYTHIC -> "🔥"
        Rarity.KONO -> "\uD83C\uDF0C"
    }
}

fun Rarity.colorDefinition(): Color {
    return when (this) {
        Rarity.COMMON -> Color(180, 245, 255)
        Rarity.RARE -> Color(100, 110, 230)
        Rarity.EPIC -> Color(100, 0, 205)
        Rarity.LEGENDARY -> Color(255, 140, 0)
        Rarity.MYTHIC -> Color(255, 0, 75)
        Rarity.KONO -> Color(255, 0, 60)
    }
}
