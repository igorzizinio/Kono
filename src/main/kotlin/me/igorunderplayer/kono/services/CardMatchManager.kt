package me.igorunderplayer.kono.services

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import me.igorunderplayer.kono.data.dto.CreateMatchRequest
import me.igorunderplayer.kono.data.dto.MatchActionRequest
import me.igorunderplayer.kono.data.dto.MatchActionResponse
import me.igorunderplayer.kono.data.dto.MatchSummaryDTO
import me.igorunderplayer.kono.domain.card.CardCatalog
import me.igorunderplayer.kono.domain.card.CardType
import me.igorunderplayer.kono.domain.gameplay.CombatState
import me.igorunderplayer.kono.domain.gameplay.TurnAction
import me.igorunderplayer.kono.engine.combat.ActionValidation
import me.igorunderplayer.kono.engine.combat.CombatEngine
import me.igorunderplayer.kono.domain.team.CombatUnitFactory
import me.igorunderplayer.kono.domain.team.TeamState
import java.util.UUID

class CardMatchManager(
    private val cardService: CardService
) {
    private data class Match(
        val state: CombatState,
        val playerOne: TeamState,
        val playerTwo: TeamState,
        val engine: CombatEngine,
        val playerOneId: String?,
        val playerTwoId: String?
    )

    private val mutex = Mutex()
    private val matches = mutableMapOf<String, Match>()

    suspend fun create(request: CreateMatchRequest): MatchSummaryDTO = mutex.withLock {
        val playerOne = createTeam("p1", request.playerOneCards)
        val playerTwo = createTeam("p2", request.playerTwoCards)
        val state = CombatState(listOf(playerOne, playerTwo))
        state.firstPlayerTeamId = "p1"
        state.turnOwnerTeamId = "p1"
        state.activeCharacter = playerOne.units.firstOrNull()
        state.beginTurn()

        val id = UUID.randomUUID().toString()
        matches[id] = Match(
            state,
            playerOne,
            playerTwo,
            CombatEngine(state),
            request.playerOneId,
            request.playerTwoId
        )
        snapshot(id, matches.getValue(id))
    }

    suspend fun get(id: String): MatchSummaryDTO? = mutex.withLock {
        matches[id]?.let { snapshot(id, it) }
    }

    suspend fun remove(id: String): Boolean = mutex.withLock {
        matches.remove(id) != null
    }

    suspend fun execute(id: String, request: MatchActionRequest): MatchActionResponse? = mutex.withLock {
        val match = matches[id] ?: return@withLock null
        if (match.state.isFinished()) {
            return@withLock MatchActionResponse(false, "Match is already finished.", snapshot(id, match))
        }
        val expectedPlayerId = when (match.state.turnOwnerTeamId) {
            "p1" -> match.playerOneId
            "p2" -> match.playerTwoId
            else -> null
        }
        if (expectedPlayerId != null && request.playerId != expectedPlayerId) {
            return@withLock invalid(id, match, "It is not this player's turn.")
        }

        val active = match.state.activeCharacter
            ?: return@withLock MatchActionResponse(false, "No active character.", snapshot(id, match))
        val target = request.targetId?.let { targetId ->
            (match.playerOne.units + match.playerTwo.units).firstOrNull { it.id == targetId }
        }

        val action = try {
            when (request.action.lowercase()) {
                "attack" -> TurnAction.Attack(
                    target ?: return@withLock invalid(id, match, "A targetId is required.")
                )

                "ability", "use_ability" -> {
                    val ability = active.abilities.firstOrNull {
                        it.type.name == "ACTIVE" &&
                                (request.abilityName == null || it.name.equals(request.abilityName, true))
                    } ?: return@withLock invalid(id, match, "Active ability not found.")
                    TurnAction.UseAbility(ability, target)
                }

                "card", "play_card" -> TurnAction.PlayCard(
                    request.cardId ?: return@withLock invalid(id, match, "A cardId is required."),
                    target
                )

                "equip" -> TurnAction.Equip(
                    request.cardId ?: return@withLock invalid(id, match, "A cardId is required.")
                )

                "swap" -> TurnAction.Swap(
                    target ?: return@withLock invalid(id, match, "A targetId is required.")
                )

                "end_turn", "end" -> TurnAction.EndTurn
                else -> return@withLock invalid(id, match, "Unknown action: ${request.action}.")
            }
        } catch (exception: IllegalArgumentException) {
            return@withLock invalid(id, match, exception.message ?: "Invalid action.")
        }

        when (val result = match.engine.executeTurnAction(active, action)) {
            ActionValidation.Valid -> MatchActionResponse(true, match = snapshot(id, match))
            is ActionValidation.Invalid -> MatchActionResponse(false, result.reason, snapshot(id, match))
        }
    }

    private suspend fun createTeam(id: String, cardIds: List<String>): TeamState {
        require(cardIds.size == 3) { "Each player must provide exactly 3 character cards." }

        val units = cardIds.mapIndexed { index, cardId ->
            val definition = cardService.getCardDefinition(cardId)
                ?: throw IllegalArgumentException("Unknown card: $cardId")
            require(definition.type == CardType.CHARACTER) {
                "Card $cardId is not a character."
            }
            CombatUnitFactory.createUnit(
                unitId = "${id}_${definition.id.lowercase()}_$index",
                definition = definition,
                level = 1
            ).also { it.slot = index }
        }
        return TeamState(id, units.toMutableList())
    }

    private fun snapshot(id: String, match: Match): MatchSummaryDTO {
        fun hp(team: TeamState) = team.units.associate { it.id to it.hp }

        val status = when {
            match.state.isFinished() -> "FINISHED"
            else -> "WAITING_FOR_ACTION"
        }

        return MatchSummaryDTO(
            id = id,
            status = status,
            turn = match.state.turn,
            playerOneUnitIds = match.playerOne.units.map { it.id },
            playerTwoUnitIds = match.playerTwo.units.map { it.id },
            playerOneHp = hp(match.playerOne),
            playerTwoHp = hp(match.playerTwo),
            ap = match.state.ap,
            hand = match.state.hand.toList(),
            discard = match.state.discard.toList(),
            log = match.state.combatLog.takeLast(20)
        )
    }

    private fun invalid(id: String, match: Match, message: String) =
        MatchActionResponse(false, message, snapshot(id, match))
}
