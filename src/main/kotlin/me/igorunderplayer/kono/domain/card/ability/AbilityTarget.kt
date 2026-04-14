package me.igorunderplayer.kono.domain.card.ability

import kotlinx.serialization.Serializable

@Serializable
enum class AbilityTarget {
    SELF,
    ENEMY,
    ALLY,
    ALL_ENEMIES,
    ALL_ALLIES
}
