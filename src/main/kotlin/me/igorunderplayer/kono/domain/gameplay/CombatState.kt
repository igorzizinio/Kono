package me.igorunderplayer.kono.domain.gameplay

import me.igorunderplayer.kono.domain.card.Stat
import me.igorunderplayer.kono.domain.team.TeamState
import kotlin.random.Random

data class TemporaryStatModifier(
    val unitId: String,
    val stat: Stat,
    val delta: Double,
    var remainingRounds: Int,
    val source: String
)

class CombatState(
    val teams: List<TeamState>,
    val rng: Random = Random.Default,
    val deck: ArrayDeque<String> = ArrayDeque(),
    val hand: MutableList<String> = mutableListOf(),
    val discard: MutableList<String> = mutableListOf(),
    val decksByTeamId: MutableMap<String, ArrayDeque<String>> = mutableMapOf(),
    val handsByTeamId: MutableMap<String, MutableList<String>> = mutableMapOf(),
    val discardsByTeamId: MutableMap<String, MutableList<String>> = mutableMapOf()
) {

    var turn: Int = 1
    var ap: Int = DuelRulesConfig.AP_PER_TURN
    var activeCharacter: Unit? = null
    val usesThisTurn: MutableMap<String, Int> = mutableMapOf()
    var freeSwapCount: Int = 0
    var swapCount: Int = 0
    var turnOwnerTeamId: String? = null
    var firstPlayerTeamId: String? = null
    var firstPlayerSkippedDraw: Boolean = false
    var mulligansUsed: Int = 0
    val timeoutCountsByTeamId: MutableMap<String, Int> = mutableMapOf()
    var winnerTeamId: String? = null
    var tieResolvedByHp: Boolean = false
    var drawResolved: Boolean = false

    val queue: ArrayDeque<CombatEvent> = ArrayDeque()
    val combatLog: MutableList<String> = mutableListOf()

    val unitDisplayNamesById: MutableMap<String, String> = mutableMapOf()

    // 🎯 ABILITIES STATE
    val hitCounterByAbilityKey: MutableMap<String, Int> = mutableMapOf()
    val attackCountByUnitId: MutableMap<String, Int> = mutableMapOf()
    val lastDamageSourceByTeamId: MutableMap<String, String> = mutableMapOf()
    val lastDamageTurnByUnitId: MutableMap<String, Int> = mutableMapOf()
    val conditionalEffectStatesByKey: MutableMap<String, Boolean> = mutableMapOf()
    val dynamicScaleAppliedValueByKey: MutableMap<String, Double> = mutableMapOf()
    val protectorShareByUnitId: MutableMap<String, Double> = mutableMapOf()
    val tauntByUnitId: MutableSet<String> = mutableSetOf()
    val onceTriggeredAbilityKeys: MutableSet<String> = mutableSetOf()
    val temporaryStatModifiers: MutableList<TemporaryStatModifier> = mutableListOf()


    // 🎲 GLOBAL FLAGS
    val globalFlags: MutableMap<String, Any> = mutableMapOf()

    // 📊 DEBUG / REPLAY
    val eventHistory: MutableList<CombatEvent> = mutableListOf()

    fun beginTurn(handSize: Int = DuelRulesConfig.STARTING_HAND_SIZE) {
        if (turnOwnerTeamId == null) {
            turnOwnerTeamId = teams.firstOrNull()?.id
        }

        if (activeCharacter == null) {
            activeCharacter = teams.firstOrNull { it.id == turnOwnerTeamId }?.aliveUnits()?.firstOrNull()
        }

        ap = DuelRulesConfig.AP_PER_TURN
        usesThisTurn.clear()
        if (turn == 1 && handsByTeamId.isEmpty()) {
            repeat(handSize) { drawCard() }
            if (turnOwnerTeamId == firstPlayerTeamId && !firstPlayerSkippedDraw) {
                firstPlayerSkippedDraw = true
            }
        } else if (turn == 1) {
            firstPlayerSkippedDraw = true
        } else {
            repeat(DuelRulesConfig.DRAW_PER_TURN) { drawCard() }
        }
    }

    fun shuffleDecks() {
        deck.shuffle(rng)
        decksByTeamId.values.forEach { it.shuffle(rng) }
    }

    fun prepareMatch(firstTeamId: String) {
        firstPlayerTeamId = firstTeamId
        turnOwnerTeamId = firstTeamId
        activeCharacter = teams.firstOrNull { it.id == firstTeamId }?.aliveUnits()?.firstOrNull()
        shuffleDecks()
        teams.forEach { team ->
            val teamDeck = decksByTeamId[team.id] ?: return@forEach
            val teamHand = handsByTeamId.getOrPut(team.id) { mutableListOf() }
            repeat(DuelRulesConfig.STARTING_HAND_SIZE) {
                teamDeck.removeFirstOrNull()?.let(teamHand::add)
            }
        }
        ap = DuelRulesConfig.AP_PER_TURN
        usesThisTurn.clear()
        firstPlayerSkippedDraw = true
    }

    fun drawCard() {
        val activeDeck = turnOwnerTeamId?.let { decksByTeamId[it] } ?: deck
        val activeHand = turnOwnerTeamId?.let { handsByTeamId.getOrPut(it) { hand } } ?: hand
        val card = activeDeck.removeFirstOrNull()
        if (card == null) {
            activeCharacter?.let {
                it.hp = (it.hp - DuelRulesConfig.FATIGUE_DAMAGE).coerceAtLeast(0.0)
                combatLog += "☠️ ${it.card.name} sofreu ${DuelRulesConfig.FATIGUE_DAMAGE.toInt()} de fadiga."
            }
        } else {
            activeHand += card
        }
    }

    fun currentHand(): MutableList<String> =
        turnOwnerTeamId?.let { handsByTeamId.getOrPut(it) { hand } } ?: hand

    fun currentDiscard(): MutableList<String> =
        turnOwnerTeamId?.let { discardsByTeamId.getOrPut(it) { discard } } ?: discard

    fun mulligan(cardIds: List<String>): Boolean {
        if (mulligansUsed >= DuelRulesConfig.MULLIGAN_LIMIT ||
            cardIds.size > DuelRulesConfig.MULLIGAN_MAX_CARDS ||
            cardIds.any { it !in currentHand() }
        ) return false

        cardIds.forEach { id ->
            currentHand().remove(id)
            currentDiscard() += id
        }
        cardIds.forEach { drawCard() }
        mulligansUsed++
        return true
    }

    fun discardExcessHand() {
        val activeHand = currentHand()
        val activeDiscard = currentDiscard()
        while (activeHand.size > DuelRulesConfig.MAX_HAND_SIZE) {
            activeDiscard += activeHand.removeAt(activeHand.lastIndex)
        }
    }

    fun recordTimeout(teamId: String): Boolean {
        val count = (timeoutCountsByTeamId[teamId] ?: 0) + 1
        timeoutCountsByTeamId[teamId] = count
        return count >= DuelRulesConfig.TIMEOUTS_FOR_LOSS
    }

    fun checkVictory(): String? {
        if (winnerTeamId != null || drawResolved) return winnerTeamId
        val aliveTeams = teams.filter { it.aliveUnits().isNotEmpty() }
        fun resolveByHp(): String? {
            val totals = teams.associate { team -> team.id to team.units.sumOf { it.hp.coerceAtLeast(0.0) } }
            val highest = totals.values.maxOrNull() ?: 0.0
            val leaders = totals.filterValues { it == highest }.keys
            return if (leaders.size == 1) leaders.single() else null
        }
        winnerTeamId = when {
            aliveTeams.size == 1 -> aliveTeams.single().id
            aliveTeams.isEmpty() -> {
                tieResolvedByHp = true
                resolveByHp()
            }
            turn >= DuelRulesConfig.TURN_LIMIT -> {
                tieResolvedByHp = true
                resolveByHp()
            }
            else -> null
        }
        if (tieResolvedByHp && winnerTeamId == null) drawResolved = true
        return winnerTeamId
    }

    fun spendAp(cost: Int): Boolean {
        if (cost < 0 || ap < cost) return false
        ap -= cost
        return true
    }

    fun teamOf(unit: Unit): TeamState? = teams.firstOrNull { unit in it.units }

    fun isCurrentActor(unit: Unit): Boolean =
        unit == activeCharacter && turnOwnerTeamId == teamOf(unit)?.id && unit.hp > 0

    fun isFinished(): Boolean {
        return checkVictory() != null
    }
}
