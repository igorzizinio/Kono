package me.igorunderplayer.kono.domain.card.ability


data class Ability(
    val name: String,
    val description: String?,
    val type: AbilityType,
    val trigger: AbilityTrigger,
    val effects: List<Effect>,
    val once: Boolean = false
)


