package me.igorunderplayer.kono.domain.card.ability

import me.igorunderplayer.kono.domain.card.Stat

sealed class Effect {

    data class Damage(
        val value: Double,
        val target: AbilityTarget = AbilityTarget.ENEMY
    ) : Effect()

    data class DamageIncreasePercent(
        val value: Double,
        val target: AbilityTarget = AbilityTarget.ENEMY
    ) : Effect()

    data class StatIncreaseWhileBelowHealth(
        val stat: Stat,
        val value: Double,
        val threshold: Double
    ) : Effect()

    data class Heal(
        val value: Double,
        val target: AbilityTarget = AbilityTarget.SELF
    ) : Effect()

    data class BuffStat(
        val stat: Stat,
        val value: Double,
        val target: AbilityTarget = AbilityTarget.SELF
    ) : Effect()

    data class AddCoins(
        val value: Int,
        val scaleWithGangSynergy: Boolean = true
    ) : Effect()

    data class Random(val profile: String) : Effect()
}
