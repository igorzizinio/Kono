package me.igorunderplayer.kono.domain.gameplay

import kotlin.random.Random

data class CombatState(
    var turn: Int = 1,
    val teams: List<Team>,
    val rng: Random = Random.Default,

    // fila de eventos
    val queue: ArrayDeque<CombatEvent> = ArrayDeque(),

    // Buffs temporarios de negacao (consumidos no proximo ataque/defesa).
    val pendingIncomingDamageNegationByUnitId: MutableMap<String, Int> = mutableMapOf(),
    val pendingOutgoingDamageNegationByUnitId: MutableMap<String, Int> = mutableMapOf()
) {
    fun isFinished(): Boolean {
        return teams.any { team ->
            team.units.none { it.hp > 0 }
        }
    }
}
