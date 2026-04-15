package me.igorunderplayer.kono.engine.combat

import me.igorunderplayer.kono.domain.card.Stat
import me.igorunderplayer.kono.domain.card.ability.AbilityType
import me.igorunderplayer.kono.domain.gameplay.CombatEvent
import me.igorunderplayer.kono.domain.gameplay.CombatState

object EventProcessor {

    suspend fun process(event: CombatEvent, state: CombatState) {

        when (event) {

            is CombatEvent.TurnStart -> {
                triggerAbilities(event, state)
            }

            is CombatEvent.Attack -> {
                if (event.attacker.hp <= 0) return

                val liveTarget = resolveLiveTarget(event.attacker, event.target, state) ?: return

                val normalizedAttack = CombatEvent.Attack(event.attacker, liveTarget)
                triggerAbilities(normalizedAttack, state)

                val atk = event.attacker.stats[Stat.ATK] ?: 0.0
                val def = liveTarget.stats[Stat.DEF] ?: 0.0

                val baseDamage = atk * (100 / (100 + def))

                state.queue.add(
                    CombatEvent.BeforeDamage(event.attacker, liveTarget, baseDamage)
                )
            }

            is CombatEvent.BeforeDamage -> {
                val modifiers = mutableListOf<DamageModifier>()

                triggerAbilities(event, state, modifiers)

                var damage = event.damage
                modifiers.forEach { damage = it(damage) }

                if (isDodged(event, state)) {
                    damage = 0.0
                }

                damage = applyPendingDamageNegations(event, damage, state)

                damage = applyCriticalDamage(event.source, damage, state)
                damage = damage.coerceAtLeast(0.0)

                event.target.hp -= damage

                state.queue.add(
                    CombatEvent.AfterDamage(event.source, event.target, damage)
                )

                if (event.target.hp <= 0) {
                    state.queue.add(CombatEvent.Death(event.target))
                }
            }

            is CombatEvent.AfterDamage -> {
                triggerAbilities(event, state)
            }

            is CombatEvent.Death -> {
                // pode expandir depois
            }
        }
    }

    private fun applyCriticalDamage(
        source: me.igorunderplayer.kono.domain.gameplay.Unit,
        damage: Double,
        state: CombatState
    ): Double {
        val (critChance, critDamage) = resolveCriticalStats(source)

        if (critChance <= 0.0) return damage

        val roll = state.rng.nextDouble()

        return if (roll < critChance) {
            damage * critDamage
        } else {
            damage
        }
    }

    private fun resolveCriticalStats(
        source: me.igorunderplayer.kono.domain.gameplay.Unit
    ): Pair<Double, Double> {
        val critProfile = source.abilities.firstOrNull { it.type == AbilityType.CRIT_PROFILE }

        if (critProfile != null) {
            val critChance = critProfile.params
                ?.get("critChance")
                ?.toDoubleOrNull()
                ?: 0.05

            val critDamage = critProfile.params
                ?.get("critDamage")
                ?.toDoubleOrNull()
                ?: 3.0

            return critChance.coerceIn(0.0, 1.0) to critDamage
        }

        val critChance = (source.stats[Stat.CRIT_CHANCE] ?: 0.0)
            .coerceIn(0.0, 1.0)
        val critDamage = source.stats[Stat.CRIT_DAMAGE] ?: 1.0

        return critChance to critDamage
    }

    private suspend fun triggerAbilities(
        event: CombatEvent,
        state: CombatState,
        modifiers: MutableList<DamageModifier> = mutableListOf()
    ) {
        val units = state.teams.flatMap { it.units }

        for (unit in units) {
            for (ability in unit.abilities) {
                AbilityProcessor.process(
                    ability,
                    unit,
                    event,
                    state,
                    modifiers
                )
            }
        }
    }

    private fun applyPendingDamageNegations(
        event: CombatEvent.BeforeDamage,
        damage: Double,
        state: CombatState
    ): Double {
        val sourceNegates = consumeNegation(
            unitId = event.source.id,
            buckets = state.pendingOutgoingDamageNegationByUnitId
        )

        val targetNegates = consumeNegation(
            unitId = event.target.id,
            buckets = state.pendingIncomingDamageNegationByUnitId
        )

        if (sourceNegates || targetNegates) return 0.0

        return damage
    }

    private fun consumeNegation(
        unitId: String,
        buckets: MutableMap<String, Int>
    ): Boolean {
        val current = buckets[unitId] ?: return false
        val remaining = current - 1

        if (remaining <= 0) {
            buckets.remove(unitId)
        } else {
            buckets[unitId] = remaining
        }

        return true
    }

    private fun isDodged(event: CombatEvent.BeforeDamage, state: CombatState): Boolean {
        if (event.damage <= 0.0) return false

        val dodgeChance = resolveDodgeChance(event.source, event.target)
        if (dodgeChance <= 0.0) return false

        return state.rng.nextDouble() < dodgeChance
    }

    private fun resolveDodgeChance(
        attacker: me.igorunderplayer.kono.domain.gameplay.Unit,
        target: me.igorunderplayer.kono.domain.gameplay.Unit
    ): Double {
        val attackerSpeed = attacker.stats[Stat.SPEED] ?: 0.0
        val targetSpeed = target.stats[Stat.SPEED] ?: 0.0
        val speedAdvantage = targetSpeed - attackerSpeed

        if (speedAdvantage <= 0.0) return 0.0

        // +100 de vantagem em SPEED = +25% de esquiva, com teto de 45%.
        return (speedAdvantage / 400.0).coerceIn(0.0, 0.45)
    }

    private fun resolveLiveTarget(
        attacker: me.igorunderplayer.kono.domain.gameplay.Unit,
        requestedTarget: me.igorunderplayer.kono.domain.gameplay.Unit,
        state: CombatState
    ): me.igorunderplayer.kono.domain.gameplay.Unit? {
        if (requestedTarget.hp > 0) return requestedTarget

        val attackerTeam = state.teams.firstOrNull { team -> team.units.any { it == attacker } }
            ?: return null

        return state.teams
            .asSequence()
            .filter { it != attackerTeam }
            .flatMap { it.units.asSequence() }
            .firstOrNull { it.hp > 0 }
    }
}
