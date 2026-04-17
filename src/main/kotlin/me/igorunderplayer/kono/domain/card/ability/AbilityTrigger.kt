package me.igorunderplayer.kono.domain.card.ability


sealed class AbilityTrigger {

    data object OnBattleStart : AbilityTrigger() {}

    data object OnTurnStart : AbilityTrigger()
    data object OnAttack : AbilityTrigger()
    data object OnDamageDealt : AbilityTrigger()
    data object OnDamageTaken : AbilityTrigger()
    data object OnDeath : AbilityTrigger()

    data object Manual : AbilityTrigger()
}
