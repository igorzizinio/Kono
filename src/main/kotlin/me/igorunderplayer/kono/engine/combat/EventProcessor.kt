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
                triggerAbilities(event, state)

                val atk = event.attacker.stats[Stat.ATK] ?: 0.0
                val def = event.target.stats[Stat.DEF] ?: 0.0

                val baseDamage = atk * (100 / (100 + def))

                println("ATK: $atk, DEF: $def, DAMAGE: $baseDamage")



                state.queue.add(
                    CombatEvent.BeforeDamage(event.attacker, event.target, baseDamage)
                )
            }

            is CombatEvent.BeforeDamage -> {
                val modifiers = mutableListOf<DamageModifier>()

                triggerAbilities(event, state, modifiers)

                var damage = event.damage
                modifiers.forEach { damage = it(damage) }

                damage = applyCriticalDamage(event.source, damage, state)

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
}
