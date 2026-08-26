package me.igorunderplayer.kono.domain.card.ability

import me.igorunderplayer.kono.domain.card.Stat
import me.igorunderplayer.kono.domain.card.StatSource
import me.igorunderplayer.kono.domain.card.prettyName
import me.igorunderplayer.kono.domain.card.prettyValue
import me.igorunderplayer.kono.domain.gameplay.CombatState
import me.igorunderplayer.kono.domain.team.TeamState
import me.igorunderplayer.kono.utils.prettyPercent
import me.igorunderplayer.kono.domain.gameplay.Unit as CombatUnit


sealed class Effect {
    data class Damage(
        val value: Double,
        val target: AbilityTarget = AbilityTarget.ENEMY,
        val damageType: DamageType = DamageType.PHYSICAL
    ) : Effect()

    data class DamageIncreasePercent(
        val value: Double,
        val target: AbilityTarget = AbilityTarget.ENEMY
    ) : Effect()

    data class DamageBasedOnStat(
        val stat: Stat,
        val scaling: Double,
        val statSource: StatSource = StatSource.SELF,
        val target: AbilityTarget = AbilityTarget.ENEMY,
        val damageType: DamageType = DamageType.PHYSICAL
    ) : Effect()

    data class StatIncreaseWhileBelowHealth(
        val stat: Stat,
        val value: Double,
        val threshold: Double
    ) : Effect()

    data class StatIncreasePercent(
        val stat: Stat,
        val percent: Double,
        val target: AbilityTarget = AbilityTarget.SELF
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
        val scaleWithGangSynergy: Boolean = false
    ) : Effect()

    data class AddCoinsScaling(
        val base: Int,
        val coinsPerStack: Int,
        val bonusPerStack: Int = 1,
        val allyFactionForBaseBonus: String? = null,
        val requiredAlliesForBaseBonus: Int = 0,
        val baseBonus: Int = 0
    ) : Effect()

    data class BuffStatByTeamCoins(
        val stat: Stat,
        val valuePerStack: Double,
        val coinsPerStack: Int,
        val mode: ScalingMode = ScalingMode.STACK,
        val maxStacks: Int? = null,
        val target: AbilityTarget = AbilityTarget.SELF
    ) : Effect()

    data class ProtectAlliesDamageShare(
        val sharePercent: Double
    ) : Effect()

    data object Taunt : Effect()

    data class Random(val profile: String) : Effect()

    data class ExecuteBellowHealth(
        val threshold: Double,
        val target: AbilityTarget = AbilityTarget.ENEMY
    ) : Effect()

    data class Custom(
        val name: String,
        val action: (self: CombatUnit, target: CombatUnit?, state: CombatState, team: TeamState?) -> Unit
    ) : Effect()
}


fun Effect.pretty(): String {
    return when (this) {

        is Effect.Damage ->
            "💥 ${this.value} dano ${this.damageType.prettyName()}"

        is Effect.DamageBasedOnStat ->
            "💥 ${this.scaling}x ${this.stat.prettyName()}"

        is Effect.DamageIncreasePercent ->
            "💥 +${prettyPercent(this.value)} dano"

        is Effect.Heal ->
            "💚 Cura ${prettyValue(Stat.HP, this.value)}"

        is Effect.BuffStat ->
            "📈 +${this.value} ${this.stat.prettyName()}"

        is Effect.StatIncreasePercent ->
            "📈 +${prettyPercent(this.percent)} ${this.stat.prettyName()}"

        is Effect.AddCoins ->
            "💰 +${this.value} fichas"

        is Effect.AddCoinsScaling ->
            "💰 +${this.base} fichas + bônus por fichas do time"

        is Effect.BuffStatByTeamCoins -> {
            val mode = when (this.mode) {
                ScalingMode.STACK -> "stack"
                ScalingMode.HIGHEST_ONLY -> "maior stack"
            }

            "🎰 +${this.valuePerStack} ${this.stat.prettyName()} / ${this.coinsPerStack} fichas ($mode)"
        }

        is Effect.ProtectAlliesDamageShare ->
            "🛡️ Intercepta ${(this.sharePercent * 100).toInt()}% do dano aliado"

        Effect.Taunt ->
            "🎯 Provoca inimigos"

        is Effect.Random ->
            "🎲 Efeito aleatório: ${this.profile}"

        is Effect.StatIncreaseWhileBelowHealth ->
            "⚠️ +${this.value} ${this.stat.prettyName()} abaixo de ${(this.threshold * 100).toInt()}% HP"

        else -> ""
    }
}
