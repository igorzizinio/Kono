package me.igorunderplayer.kono.domain.card.ability


data class Ability(
    val name: String,
    val type: AbilityType,
    val trigger: AbilityTrigger,
    val effects: List<Effect>
)
