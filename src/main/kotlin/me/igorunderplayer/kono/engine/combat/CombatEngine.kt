package me.igorunderplayer.kono.engine.combat

import me.igorunderplayer.kono.domain.card.Stat
import me.igorunderplayer.kono.domain.gameplay.CombatEvent
import me.igorunderplayer.kono.domain.gameplay.CombatState
import me.igorunderplayer.kono.domain.gameplay.Unit
import kotlin.math.floor

object CombatEngine {

    suspend fun run(state: CombatState): CombatState {

        while (!state.isFinished()) {

            val units = state.teams
                .flatMap { it.units }
                .filter { it.hp > 0 }
                .sortedByDescending { it.stats[Stat.SPEED] ?: 0.0 }

            for (unit in units) {

                if (unit.hp <= 0) continue

                state.queue.add(CombatEvent.TurnStart(unit))

                val attackCount = resolveAttackCount(unit, state)

                repeat(attackCount) {
                    val target = findTarget(unit, state) ?: return@repeat
                    state.queue.add(CombatEvent.Attack(unit, target))
                }

                processQueue(state)

                if (state.isFinished()) break
            }

            state.turn++
        }

        return state
    }

    private suspend fun processQueue(state: CombatState) {
        while (state.queue.isNotEmpty()) {
            val event = state.queue.removeFirst()
            EventProcessor.process(event, state)
        }
    }

    private fun findTarget(unit: Unit, state: CombatState): Unit? {
        val enemyTeam = state.teams.firstOrNull { team ->
            team.units.none { it == unit }
        } ?: return null

        return enemyTeam.units.firstOrNull { it.hp > 0 }
    }

    private fun resolveAttackCount(unit: Unit, state: CombatState): Int {
        val speed = unit.stats[Stat.SPEED] ?: 0.0

        // SPEED acima de 120 comeca a ganhar ataques extras (max 3 ataques no turno).
        val extraAttackBudget = ((speed - 120.0) / 100.0).coerceAtLeast(0.0)
        val guaranteedExtra = floor(extraAttackBudget).toInt()
        val extraChance = extraAttackBudget - guaranteedExtra

        var extraAttacks = guaranteedExtra
        if (extraChance > 0.0 && state.rng.nextDouble() < extraChance) {
            extraAttacks += 1
        }

        return (1 + extraAttacks).coerceIn(1, 3)
    }
}
