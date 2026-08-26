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


fun AbilityTrigger.prettyName(): String = when (this) {
    AbilityTrigger.Manual ->
        "Manual"

    AbilityTrigger.OnBattleStart ->
        "Início da batalha"

    AbilityTrigger.OnTurnStart ->
        "Início do turno"

    is AbilityTrigger.OnTurnEvery ->
        "A cada $turns turnos"

    is AbilityTrigger.OnAttack ->
        if (every == 1) "Ao atacar"
        else "A cada $every ataques"

    is AbilityTrigger.OnHit ->
        if (every == 1) "Ao acertar"
        else "A cada $every acertos"

    is AbilityTrigger.OnAttackAgainstTag ->
        "Contra $tag"

    AbilityTrigger.OnDamageDealt ->
        "Ao causar dano"

    is AbilityTrigger.OnDamageTaken ->
        if (damageType == null) "Ao receber dano"
        else "Ao receber dano de tipo $damageType"

    is AbilityTrigger.OnBellowHealth ->
        "Ao ficar abaixo de ${threshold * 100}% de vida"

    AbilityTrigger.OnDeath ->
        "Ao morrer"

    AbilityTrigger.OnCrit ->
        "Ao realizar um acerto crítico"
}
