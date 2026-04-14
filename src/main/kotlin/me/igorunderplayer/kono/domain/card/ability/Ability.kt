package me.igorunderplayer.kono.domain.card.ability

import kotlinx.serialization.Serializable

@Serializable
data class Ability(
    val type: AbilityType,
    val value: Double? = null,
    val trigger: AbilityTrigger? = null,
    val target: AbilityTarget? = null,

    // dados extras (pra não travar teu design)
    val params: Map<String, String>? = null
)
