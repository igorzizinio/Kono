package me.igorunderplayer.kono.domain.gameplay

import kotlin.random.Random

data class CombatState(
    var turn: Int = 1,
    val teams: List<Team>,
    val rng: Random = Random.Default,

    // fila de eventos
    val queue: ArrayDeque<CombatEvent> = ArrayDeque()
) {
    fun isFinished(): Boolean {
        return teams.any { team ->
            team.units.none { it.hp > 0 }
        }
    }
}
