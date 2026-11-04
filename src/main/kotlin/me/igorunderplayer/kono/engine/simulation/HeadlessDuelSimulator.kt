package me.igorunderplayer.kono.engine.simulation

import me.igorunderplayer.kono.domain.card.CardCatalog
import me.igorunderplayer.kono.domain.card.CardType
import me.igorunderplayer.kono.domain.gameplay.CombatState
import me.igorunderplayer.kono.domain.team.CombatUnitFactory
import me.igorunderplayer.kono.domain.team.TeamState
import me.igorunderplayer.kono.engine.combat.CombatEngine
import kotlin.random.Random

data class DuelSimulationMetrics(
    val games: Int,
    val winsByCharacter: Map<String, Int>,
    val draws: Int,
    val averageTurns: Double,
    val averageUnspentAp: Double,
    val averageSwaps: Double
)

/**
 * Deterministic, headless smoke simulator for balance work.
 * It deliberately uses only catalog definitions and never touches persistence or Discord.
 */
class HeadlessDuelSimulator {
    fun run(games: Int, seed: Int = 0): DuelSimulationMetrics {
        require(games > 0) { "games must be positive" }
        val characters = CardCatalog.all.filter { it.type == CardType.CHARACTER }
        require(characters.size >= 2) { "At least two character cards are required." }
        val random = Random(seed)
        val wins = mutableMapOf<String, Int>()
        var draws = 0
        var turns = 0
        var unspentAp = 0
        var swaps = 0

        repeat(games) {
            val first = characters[random.nextInt(characters.size)]
            var second = characters[random.nextInt(characters.size)]
            if (second == first) second = characters[(characters.indexOf(first) + 1) % characters.size]
            val firstUnit = CombatUnitFactory.createUnit("sim_p1_$it", first, 1)
            val secondUnit = CombatUnitFactory.createUnit("sim_p2_$it", second, 1)
            val state = CombatState(
                teams = listOf(
                    TeamState("p1", mutableListOf(firstUnit)),
                    TeamState("p2", mutableListOf(secondUnit))
                ),
                rng = random
            )
            state.activeCharacter = firstUnit
            state.firstPlayerTeamId = "p1"
            state.turnOwnerTeamId = "p1"
            val engine = CombatEngine(state)
            engine.run()
            turns += state.turn
            unspentAp += state.ap
            swaps += state.swapCount
            val winner = state.winnerTeamId
            if (winner == null || state.drawResolved) {
                draws++
            } else {
                val card = if (winner == "p1") first.id else second.id
                wins[card] = (wins[card] ?: 0) + 1
            }
        }

        return DuelSimulationMetrics(
            games = games,
            winsByCharacter = wins,
            draws = draws,
            averageTurns = turns.toDouble() / games,
            averageUnspentAp = unspentAp.toDouble() / games,
            averageSwaps = swaps.toDouble() / games
        )
    }
}
