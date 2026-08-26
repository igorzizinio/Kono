package me.igorunderplayer.kono.domain.card.ability


sealed class AbilityTrigger {

    data object OnBattleStart : AbilityTrigger() {}

    data object OnTurnStart : AbilityTrigger()
    data class OnTurnEvery(val turns: Int) : AbilityTrigger()
    data class OnAttack(val every: Int = 1) : AbilityTrigger()
    data class OnHit(val every: Int = 1) : AbilityTrigger()
    data class OnAttackAgainstTag(val tag: String) : AbilityTrigger()
    data object OnDamageDealt : AbilityTrigger()
    data class OnDamageTaken(val damageType: DamageType? = null) : AbilityTrigger()
    data class OnBellowHealth(val threshold: Double, val target: AbilityTarget) : AbilityTrigger()
    data object OnDeath : AbilityTrigger()

    data object OnCrit : AbilityTrigger()

    data object Manual : AbilityTrigger()
}
