package me.igorunderplayer.kono.domain.card.ability

import kotlinx.serialization.Serializable

@Serializable
enum class AbilityTrigger {
    ON_ATTACK,
    ON_TURN_START,
    ON_HIT,
    ON_DAMAGE_TAKEN,
    PASSIVE
}
