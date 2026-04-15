package me.igorunderplayer.kono.engine.combat

import me.igorunderplayer.kono.domain.card.ability.Ability
import me.igorunderplayer.kono.domain.card.ability.AbilityTrigger
import me.igorunderplayer.kono.domain.card.ability.AbilityType
import me.igorunderplayer.kono.domain.gameplay.CombatEvent
import me.igorunderplayer.kono.domain.gameplay.CombatState
import me.igorunderplayer.kono.domain.gameplay.Unit

object AbilityProcessor {

    fun process(
        ability: Ability,
        owner: Unit,
        event: CombatEvent,
        state: CombatState,
        modifiers: MutableList<DamageModifier>
    ) {
        if (!matches(ability.trigger, event)) return

        when (ability.type) {

            AbilityType.LIFESTEAL -> {
                if (event is CombatEvent.AfterDamage && event.source == owner) {
                    val value = ability.value ?: return
                    val heal = event.damage * (value / 100.0)
                    owner.hp += heal
                }
            }

            AbilityType.DAMAGE -> {
                if (event is CombatEvent.Attack && event.attacker == owner) {
                    val value = ability.value ?: return
                    state.queue.add(
                        CombatEvent.BeforeDamage(owner, event.target, value)
                    )
                }
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
        val profile = ability.params?.get("profile")

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
                state.queue.add(CombatEvent.BeforeDamage(owner, enemy, enemyDamage))
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
        val minValue = ability.params?.get(minKey)?.toDoubleOrNull() ?: fallbackMin
        val maxValue = ability.params?.get(maxKey)?.toDoubleOrNull() ?: fallbackMax
        val low = minOf(minValue, maxValue)
        val high = maxOf(minValue, maxValue)

        if (low == high) return low

        return state.rng.nextDouble(low, high)
    }
}
