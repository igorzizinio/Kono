package me.igorunderplayer.kono.domain.gameplay

sealed class CombatEvent {

    data class TurnStart(val unit: Unit) : CombatEvent()

    data class Attack(
        val attacker: Unit,
        val target: Unit
    ) : CombatEvent()

    data class BeforeDamage(
        val source: Unit,
        val target: Unit,
        var damage: Double
    ) : CombatEvent()

    data class AfterDamage(
        val source: Unit,
        val target: Unit,
        val damage: Double
    ) : CombatEvent()

    data class Death(val unit: Unit) : CombatEvent()
}
