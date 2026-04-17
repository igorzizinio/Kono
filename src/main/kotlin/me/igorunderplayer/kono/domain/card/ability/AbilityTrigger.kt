package me.igorunderplayer.kono.domain.card.ability


sealed class AbilityTrigger {

    data object OnBattleStart : AbilityTrigger() {}

    data object OnTurnStart : AbilityTrigger()
    data class OnTurnEvery(val turns: Int) : AbilityTrigger()
    data object OnAttack : AbilityTrigger()
    data class OnAttackEvery(val attacks: Int) : AbilityTrigger()
    data class OnAttackAgainstTag(val tag: String) : AbilityTrigger()
    data object OnDamageDealt : AbilityTrigger()
    data object OnDamageTaken : AbilityTrigger()
    data class OnBellowHealth(val threshold: Double) : AbilityTrigger()
    data object OnDeath : AbilityTrigger()

    data object Manual : AbilityTrigger()
}
