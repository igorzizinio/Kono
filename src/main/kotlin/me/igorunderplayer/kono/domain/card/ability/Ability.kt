package me.igorunderplayer.kono.domain.card.ability


data class Ability(
    val name: String,
    val description: String? = null,
    val type: AbilityType,
    val trigger: AbilityTrigger,
    val effects: List<Effect>,
    val apCost: Int = 0,
    val usesPerTurn: Int? = null,
    val once: Boolean = false
)

