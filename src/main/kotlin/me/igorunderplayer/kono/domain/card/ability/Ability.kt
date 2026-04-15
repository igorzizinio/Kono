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
    val enemyHealMax: Double? = null,

    // RNG/gambler tuning
    val atkBuffMin: Double? = null,
    val atkBuffMax: Double? = null,
    val speedBuffMin: Double? = null,
    val speedBuffMax: Double? = null,
    val shieldMin: Int? = null,
    val shieldMax: Int? = null,
    val strongEveryTurns: Int? = null,
    val coinBiasPerCoin: Double? = null,
    val strongMultiplier: Double? = null,
    val extraRollSpeedFactor: Double? = null
)
