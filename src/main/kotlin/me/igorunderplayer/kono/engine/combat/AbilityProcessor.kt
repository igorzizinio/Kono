package me.igorunderplayer.kono.engine.combat

import me.igorunderplayer.kono.domain.card.ability.Ability
import me.igorunderplayer.kono.domain.card.ability.AbilityTarget
import me.igorunderplayer.kono.domain.card.ability.AbilityTrigger
import me.igorunderplayer.kono.domain.card.ability.AbilityType
import me.igorunderplayer.kono.domain.gameplay.CombatEvent
import me.igorunderplayer.kono.domain.gameplay.CombatState
import me.igorunderplayer.kono.domain.gameplay.Unit

object AbilityProcessor {

    fun process(
        ability: Ability,
        abilityIndex: Int,
        owner: Unit,
        event: CombatEvent,
        state: CombatState,
        modifiers: MutableList<DamageModifier>
    ) {
        if (!matches(ability.trigger, event)) return

        when (ability.type) {

            AbilityType.LIFESTEAL -> {
                if (event is CombatEvent.AfterDamage && event.source == owner) {
                    if (event.damage <= 0.0) return
                    if (!shouldActivateByHitCycle(ability, owner, abilityIndex, state)) return

                    val value = ability.value ?: return
                    val heal = event.damage * (value / 100.0)
                    if (heal <= 0.0) return

                    val before = owner.hp
                    healUnit(owner, heal)
                    val healedAmount = (owner.hp - before).coerceAtLeast(0.0)
                    if (healedAmount > 0.0) {
                        state.combatLog += "🩸 ${unitLabel(owner, state)} drenou ${formatValue(healedAmount)} de vida."
                    }
                }
            }

            AbilityType.DAMAGE -> {
                queueDamageAbility(ability, abilityIndex, owner, event, state)
            }

            AbilityType.RNG_EFFECT -> {
                if (event is CombatEvent.TurnStart && event.unit == owner) {
                    processRandomEffect(ability, owner, state)
                }
            }

            AbilityType.INCOMING_DAMAGE_REDUCTION -> {
                if (event is CombatEvent.BeforeDamage && event.target == owner) {
                    addIncomingDamageReductionModifier(ability, modifiers)
                }
            }

            AbilityType.OUTGOING_DAMAGE_AMPLIFICATION -> {
                if (event is CombatEvent.BeforeDamage && event.source == owner) {
                    addOutgoingDamageAmplificationModifier(ability, modifiers)
                }
            }

            else -> {}
        }
    }

    private fun queueDamageAbility(
        ability: Ability,
        abilityIndex: Int,
        owner: Unit,
        event: CombatEvent,
        state: CombatState
    ) {
        val value = ability.value ?: return

        when (event) {
            is CombatEvent.Attack -> {
                if (event.attacker != owner) return
                val target = resolveTarget(ability.target, owner, event.target)
                queueAbilityDamageEvent(
                    owner = owner,
                    target = target,
                    baseDamage = value,
                    ability = ability,
                    state = state,
                    defaultTrueDamage = false
                )
            }

            is CombatEvent.AfterDamage -> {
                if (event.source != owner || event.damage <= 0.0) return
                if (event.sourceAbilityType == AbilityType.DAMAGE) return
                if (!shouldActivateByHitCycle(ability, owner, abilityIndex, state)) return

                val target = resolveTarget(ability.target, owner, event.target)
                queueAbilityDamageEvent(
                    owner = owner,
                    target = target,
                    baseDamage = value,
                    ability = ability,
                    state = state,
                    defaultTrueDamage = true
                )
            }

            else -> return
        }
    }

    private fun queueAbilityDamageEvent(
        owner: Unit,
        target: Unit,
        baseDamage: Double,
        ability: Ability,
        state: CombatState,
        defaultTrueDamage: Boolean
    ) {
        val isTrueDamage = ability.params?.trueDamage ?: defaultTrueDamage
        val canCrit = ability.params?.canCrit ?: !isTrueDamage
        val canBeDodged = ability.params?.canBeDodged ?: !isTrueDamage

        state.queue.add(
            CombatEvent.BeforeDamage(
                source = owner,
                target = target,
                damage = baseDamage,
                isTrueDamage = isTrueDamage,
                canCrit = canCrit,
                canBeDodged = canBeDodged,
                sourceAbilityType = ability.type
            )
        )
    }

    private fun resolveTarget(target: AbilityTarget?, owner: Unit, defaultTarget: Unit): Unit {
        return when (target) {
            AbilityTarget.SELF -> owner
            else -> defaultTarget
        }
    }

    private fun addIncomingDamageReductionModifier(
        ability: Ability,
        modifiers: MutableList<DamageModifier>
    ) {
        val value = ability.value ?: return
        modifiers.add { dmg -> dmg * (1 - value / 100.0) }
    }

    private fun addOutgoingDamageAmplificationModifier(
        ability: Ability,
        modifiers: MutableList<DamageModifier>
    ) {
        val value = ability.value ?: return
        modifiers.add { dmg -> dmg * (1 + value / 100.0) }
    }

    private fun matches(trigger: AbilityTrigger?, event: CombatEvent): Boolean {
        return when (trigger) {
            AbilityTrigger.ON_TURN_START -> event is CombatEvent.TurnStart
            AbilityTrigger.ON_ATTACK -> event is CombatEvent.Attack
            AbilityTrigger.ON_DAMAGE_TAKEN -> event is CombatEvent.BeforeDamage
            AbilityTrigger.ON_HIT -> event is CombatEvent.AfterDamage
            AbilityTrigger.PASSIVE -> true
            else -> false
        }
    }

    private fun processRandomEffect(
        ability: Ability,
        owner: Unit,
        state: CombatState
    ) {
        val profile = ability.params?.profile

        if (profile == "UNDEFINED_BUG") {
            applyUndefinedEffect(owner, state, ability)
            return
        }

        // Legacy RNG behavior (used by older items like Gambler Charm).
        val roll = state.rng.nextDouble()
        if (roll < 0.5) {
            healUnit(owner, 10.0)
        } else {
            owner.hp -= 5
        }
    }

    private fun applyUndefinedEffect(
        owner: Unit,
        state: CombatState,
        ability: Ability
    ) {
        val enemy = findPrimaryEnemy(owner, state)

        val selfDamage = randomRange(state, ability, "selfDamageMin", "selfDamageMax", 60.0, 140.0)
        val enemyDamage = randomRange(state, ability, "enemyDamageMin", "enemyDamageMax", 70.0, 160.0)
        val selfHeal = randomRange(state, ability, "selfHealMin", "selfHealMax", 50.0, 120.0)
        val enemyHeal = randomRange(state, ability, "enemyHealMin", "enemyHealMax", 50.0, 120.0)

        val effects = mutableListOf<() -> kotlin.Unit>()
        effects += { owner.hp -= selfDamage }
        effects += { healUnit(owner, selfHeal) }
        effects += {
            state.pendingIncomingDamageNegationByUnitId[owner.id] =
                (state.pendingIncomingDamageNegationByUnitId[owner.id] ?: 0) + 1
        }
        effects += {
            state.pendingOutgoingDamageNegationByUnitId[owner.id] =
                (state.pendingOutgoingDamageNegationByUnitId[owner.id] ?: 0) + 1
        }

        if (enemy != null) {
            effects += {
                state.queue.add(
                    CombatEvent.BeforeDamage(
                        source = owner,
                        target = enemy,
                        damage = enemyDamage,
                        sourceAbilityType = AbilityType.RNG_EFFECT
                    )
                )
            }
            effects += { healUnit(enemy, enemyHeal) }
            effects += {
                state.pendingIncomingDamageNegationByUnitId[enemy.id] =
                    (state.pendingIncomingDamageNegationByUnitId[enemy.id] ?: 0) + 1
            }
            effects += {
                state.pendingOutgoingDamageNegationByUnitId[enemy.id] =
                    (state.pendingOutgoingDamageNegationByUnitId[enemy.id] ?: 0) + 1
            }
        }

        if (effects.isEmpty()) return

        val chosenEffect = effects[state.rng.nextInt(effects.size)]
        chosenEffect.invoke()
    }

    private fun healUnit(unit: Unit, amount: Double) {
        if (amount <= 0.0) return

        val maxHp = unit.stats[me.igorunderplayer.kono.domain.card.Stat.HP]
        unit.hp = if (maxHp != null && maxHp > 0.0) {
            (unit.hp + amount).coerceAtMost(maxHp)
        } else {
            unit.hp + amount
        }
    }

    private fun findPrimaryEnemy(owner: Unit, state: CombatState): Unit? {
        val ownerTeam = state.teams.firstOrNull { team -> team.units.any { it === owner } }
            ?: return null

        return state.teams
            .asSequence()
            .filter { it !== ownerTeam }
            .flatMap { it.units.asSequence() }
            .firstOrNull { it.hp > 0 }
    }

    private fun randomRange(
        state: CombatState,
        ability: Ability,
        minKey: String,
        maxKey: String,
        fallbackMin: Double,
        fallbackMax: Double
    ): Double {
        val minValue = resolveDoubleParam(ability, minKey) ?: fallbackMin
        val maxValue = resolveDoubleParam(ability, maxKey) ?: fallbackMax
        val low = minOf(minValue, maxValue)
        val high = maxOf(minValue, maxValue)

        if (low == high) return low

        return state.rng.nextDouble(low, high)
    }

    private fun shouldActivateByHitCycle(
        ability: Ability,
        owner: Unit,
        abilityIndex: Int,
        state: CombatState
    ): Boolean {
        val everyHits = ability.params?.everyHits?.coerceAtLeast(1) ?: 1

        if (everyHits == 1) return true

        val key = "${owner.id}#$abilityIndex"
        val nextCounter = (state.hitCounterByAbilityKey[key] ?: 0) + 1
        state.hitCounterByAbilityKey[key] = nextCounter

        return nextCounter % everyHits == 0
    }

    private fun resolveDoubleParam(ability: Ability, key: String): Double? {
        return when (key) {
            "selfDamageMin" -> ability.params?.selfDamageMin
            "selfDamageMax" -> ability.params?.selfDamageMax
            "enemyDamageMin" -> ability.params?.enemyDamageMin
            "enemyDamageMax" -> ability.params?.enemyDamageMax
            "selfHealMin" -> ability.params?.selfHealMin
            "selfHealMax" -> ability.params?.selfHealMax
            "enemyHealMin" -> ability.params?.enemyHealMin
            "enemyHealMax" -> ability.params?.enemyHealMax
            else -> null
        }
    }

    private fun unitLabel(unit: Unit, state: CombatState): String {
        return state.unitDisplayNamesById[unit.id] ?: unit.card.name
    }

    private fun formatValue(value: Double): String {
        val rounded = String.format(java.util.Locale.US, "%.2f", value)
        return rounded.trimEnd('0').trimEnd('.')
    }
}
