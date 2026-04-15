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
    val pendingOutgoingDamageNegationByUnitId: MutableMap<String, Int> = mutableMapOf(),

    // Nome exibido por unidade para logs/embeds (ex.: "Slime de Igor").
    val unitDisplayNamesById: MutableMap<String, String> = mutableMapOf(),

    // Registro textual da simulacao, usado para retorno no comando fight.
    val combatLog: MutableList<String> = mutableListOf()
) {
    fun isFinished(): Boolean {
        return teams.any { team ->
            team.units.none { it.hp > 0 }
        }
    }
}
