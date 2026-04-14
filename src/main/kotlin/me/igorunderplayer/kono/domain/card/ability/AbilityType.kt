package me.igorunderplayer.kono.domain.card.ability

import kotlinx.serialization.Serializable

@Serializable
enum class AbilityType {
    DAMAGE,
    HEAL,
    LIFESTEAL,

    // Explicit damage modifiers.
    INCOMING_DAMAGE_REDUCTION,
    OUTGOING_DAMAGE_AMPLIFICATION,

    CRIT_PROFILE,
    RNG_EFFECT
}
