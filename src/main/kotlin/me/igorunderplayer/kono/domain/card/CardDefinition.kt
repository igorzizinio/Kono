package me.igorunderplayer.kono.domain.card

import me.igorunderplayer.kono.domain.card.ability.Ability

data class CardDefinition(
    val id: String,
    val name: String,
    val description: String,

    val type: CardType,
    val rarity: Rarity,

    val faction: String? = null,

    val baseStats: Map<Stat, Double>,
    val statsPerLevel: Map<Stat, Double>,

    val tags: Set<String> = emptySet(),

    val abilities: List<Ability>
)
