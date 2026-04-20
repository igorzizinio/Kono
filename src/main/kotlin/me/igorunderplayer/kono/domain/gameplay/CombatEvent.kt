package me.igorunderplayer.kono.domain.gameplay

import me.igorunderplayer.kono.domain.card.ability.Effect.DamageType
import me.igorunderplayer.kono.domain.card.ability.AbilityType

sealed class CombatEvent {

    data object BattleStart : CombatEvent()

    data class TurnStart(val unit: Unit) : CombatEvent()

    data class Attack(
        val attacker: Unit,
        val target: Unit
    ) : CombatEvent()

    data class BeforeDamage(
        val source: Unit,
        val target: Unit,
        var damage: Double,
        val damageType: DamageType = DamageType.PHYSICAL,
        val canCrit: Boolean = true,
        val canBeDodged: Boolean = true,
        val sourceAbilityType: AbilityType? = null
    ) : CombatEvent()

    data class AfterDamage(
        val source: Unit,
        val target: Unit,
        val damage: Double,
        val damageType: DamageType = DamageType.PHYSICAL,
        val wasCritical: Boolean = false,
        val sourceAbilityType: AbilityType? = null
    ) : CombatEvent()

    data class Death(val unit: Unit) : CombatEvent()
}
