package me.igorunderplayer.kono.domain.card.ability

import kotlinx.serialization.Serializable

@Serializable
data class Ability(
    val type: AbilityType,
    val value: Double? = null,
    val trigger: AbilityTrigger? = null,
    val target: AbilityTarget? = null,

    // Parametros tipados para deixar o comportamento explicito por habilidade.
    val params: AbilityParams? = null
)

@Serializable
data class AbilityParams(
    val trueDamage: Boolean? = null,
    val canCrit: Boolean? = null,
    val canBeDodged: Boolean? = null,
    val everyHits: Int? = null,
    val critChance: Double? = null,
    val critDamage: Double? = null,
    val profile: String? = null,
    val selfDamageMin: Double? = null,
    val selfDamageMax: Double? = null,
    val enemyDamageMin: Double? = null,
    val enemyDamageMax: Double? = null,
    val selfHealMin: Double? = null,
    val selfHealMax: Double? = null,
    val enemyHealMin: Double? = null,
    val enemyHealMax: Double? = null
)
