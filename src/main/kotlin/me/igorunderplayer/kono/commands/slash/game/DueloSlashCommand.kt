package me.igorunderplayer.kono.commands.slash.game

import dev.kord.common.entity.ApplicationCommandOption
import dev.kord.common.entity.ApplicationCommandOptionType
import dev.kord.common.entity.optional.Optional
import dev.kord.common.entity.optional.OptionalBoolean
import dev.kord.core.entity.interaction.SubCommand
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.behavior.interaction.response.edit
import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.common.entity.ButtonStyle
import dev.kord.rest.builder.component.ActionRowBuilder
import dev.kord.rest.builder.message.embed
import me.igorunderplayer.kono.commands.KonoSlashCommand
import me.igorunderplayer.kono.commands.KonoSlashSubCommand
import me.igorunderplayer.kono.data.repositories.CardInstanceRepository
import me.igorunderplayer.kono.domain.card.CardCatalog
import me.igorunderplayer.kono.domain.card.CardType
import me.igorunderplayer.kono.domain.gameplay.CombatState
import me.igorunderplayer.kono.domain.gameplay.DuelRulesConfig
import me.igorunderplayer.kono.domain.gameplay.TurnAction
import me.igorunderplayer.kono.domain.team.CombatUnitFactory
import me.igorunderplayer.kono.domain.team.TeamState
import me.igorunderplayer.kono.engine.combat.CombatEngine
import me.igorunderplayer.kono.engine.combat.ActionValidation
import me.igorunderplayer.kono.services.DeckService
import me.igorunderplayer.kono.utils.interaction.awaitFirstButtonInteraction

class DueloSlashCommand(
    deckService: DeckService,
    cardInstanceRepository: CardInstanceRepository
) : KonoSlashCommand {
    override val name = "duelo"
    override val description = "Duelos 1v1 usando seu deck ativo"

    private val subCommands = listOf(DuelPlayerSubCommand(deckService, cardInstanceRepository))

    override val options = subCommands.map {
        ApplicationCommandOption(
            name = it.name,
            description = it.description,
            type = ApplicationCommandOptionType.SubCommand,
            options = Optional(it.options)
        )
    }

    override suspend fun run(event: ChatInputCommandInteractionCreateEvent) {
        val command = event.interaction.command as SubCommand
        subCommands.firstOrNull { it.name == command.name }?.run(event)
    }
}

private class DuelPlayerSubCommand(
    private val deckService: DeckService,
    private val cardInstanceRepository: CardInstanceRepository
) : KonoSlashSubCommand {
    override val name = "jogador"
    override val description = "Desafia outro jogador para um duelo 1v1"
    override val options = listOf(
        ApplicationCommandOption(
            name = "oponente",
            description = "Jogador desafiado",
            type = ApplicationCommandOptionType.User,
            required = OptionalBoolean.Value(true)
        )
    )

    override suspend fun run(event: ChatInputCommandInteractionCreateEvent) {
        val deferred = event.interaction.deferPublicResponse()
        val opponent = event.interaction.command.users["oponente"]
        if (opponent == null || opponent.id == event.interaction.user.id) {
            deferred.respond { content = "❌ Selecione um oponente válido." }
            return
        }

        val first = buildFighter(event.interaction.user.id.value.toLong(), event.interaction.user.username)
        val second = buildFighter(opponent.id.value.toLong(), opponent.username)
        if (first.error != null || second.error != null) {
            deferred.respond {
                content = listOfNotNull(first.error, second.error).joinToString("\n")
            }
            return
        }

        val state = CombatState(
            teams = listOf(
                TeamState("p1", mutableListOf(first.unit!!)),
                TeamState("p2", mutableListOf(second.unit!!))
            ),
            decksByTeamId = mutableMapOf(
                "p1" to ArrayDeque(first.remainingCards),
                "p2" to ArrayDeque(second.remainingCards)
            ),
            handsByTeamId = mutableMapOf("p1" to mutableListOf(), "p2" to mutableListOf()),
            discardsByTeamId = mutableMapOf("p1" to mutableListOf(), "p2" to mutableListOf())
        )
        state.unitDisplayNamesById[first.unit.id] = event.interaction.user.username
        state.unitDisplayNamesById[second.unit.id] = opponent.username
        state.prepareMatch("p1")
        val engine = CombatEngine(state)
        engine.startInteractiveMatch()
        var response = deferred.respond {
            embed {
                title = "⚔️ Duelo 1v1 — ${event.interaction.user.username} vs ${opponent.username}"
                description = buildDescription(state, event.interaction.user.username, opponent.username)
            }
        }

        while (!state.isFinished()) {
            val actor = state.activeCharacter ?: break
            val actorId = if (state.turnOwnerTeamId == "p1") event.interaction.user.id.value.toLong() else opponent.id.value.toLong()
            val enemy = state.teams.first { it.id != state.turnOwnerTeamId }.aliveUnits().firstOrNull()
            if (enemy == null) break

            val actionId = "duel:${event.interaction.id.value}:${state.turn}:${System.nanoTime()}"
            val abilityIds = actor.abilities.filter { it.type.name == "ACTIVE" }
                .mapIndexed { index, _ -> "$actionId:ability:$index" }
            val cardIds = state.currentHand().mapIndexed { index, _ -> "$actionId:card:$index" }
            val ids = listOf("$actionId:attack", "$actionId:end", "$actionId:surrender") + abilityIds + cardIds

            response = response.edit {
                embed {
                    title = "⚔️ Duelo 1v1 — ${event.interaction.user.username} vs ${opponent.username}"
                    description = buildDescription(state, event.interaction.user.username, opponent.username)
                }
                rows(state, actionId, abilityIds, cardIds).forEach { addComponent(it) }
            }
            val clicked = event.kord.awaitFirstButtonInteraction(ids, actorId)
            if (clicked == null) {
                engine.registerTimeout(state.turnOwnerTeamId ?: "p1")
                if (!state.isFinished()) engine.executeTurnAction(actor, TurnAction.EndTurn)
                continue
            }
            val (customId, interaction) = clicked
            val result = when {
                customId == "$actionId:attack" -> engine.executeTurnAction(actor, TurnAction.Attack(enemy))
                customId == "$actionId:end" -> engine.executeTurnAction(actor, TurnAction.EndTurn)
                customId == "$actionId:surrender" -> engine.executeTurnAction(actor, TurnAction.Surrender)
                customId.startsWith("$actionId:ability:") -> {
                    val index = customId.substringAfterLast(':').toIntOrNull()
                    val ability = actor.abilities.filter { it.type.name == "ACTIVE" }.getOrNull(index ?: -1)
                    if (ability == null) ActionValidation.Invalid("Habilidade inválida.")
                    else engine.executeTurnAction(actor, TurnAction.UseAbility(ability, enemy))
                }
                customId.startsWith("$actionId:card:") -> {
                    val index = customId.substringAfterLast(':').toIntOrNull()
                    val card = state.currentHand().getOrNull(index ?: -1)
                    if (card == null) ActionValidation.Invalid("Carta inválida.")
                    else {
                        engine.executeTurnAction(actor, if (CardCatalog.getById(card)?.type == CardType.EQUIPMENT) {
                            TurnAction.Equip(card)
                        } else {
                            TurnAction.PlayCard(card, enemy)
                        })
                    }
                }
                else -> ActionValidation.Invalid("Ação inválida.")
            }
            val handText = state.currentHand().joinToString(", ") { "`$it`" }.ifBlank { "vazia" }
            interaction.interaction.respondEphemeral {
                content = if (result is ActionValidation.Invalid) {
                    "❌ ${result.reason}\n🃏 Sua mão: $handText"
                } else {
                    "✅ Ação resolvida.\n🃏 Sua mão: $handText"
                }
            }
        }

        response.edit {
            embed {
                title = "⚔️ Duelo 1v1 — ${event.interaction.user.username} vs ${opponent.username}"
                description = buildDescription(state, event.interaction.user.username, opponent.username)
            }
        }
    }

    private fun buildDescription(
        state: CombatState,
        firstName: String,
        secondName: String
    ): String = buildString {
                val winner = state.winnerTeamId
                if (winner != null || state.drawResolved) {
                    appendLine(
                        when {
                            state.drawResolved -> "🤝 Empate — HP restante igual."
                            state.tieResolvedByHp -> "🏁 Limite atingido — desempate por HP restante."
                            else -> "🏆 Vitória!"
                        }
                    )
                    winner?.let { appendLine("Vencedor: **${if (it == "p1") firstName else secondName}**") }
                } else {
                    appendLine("Turno ${state.turn}/${DuelRulesConfig.TURN_LIMIT} • AP: ${state.ap}")
                    appendLine("Vez de: **${if (state.turnOwnerTeamId == "p1") firstName else secondName}**")
                }
                appendLine()
                state.teams.forEach { team ->
                    val name = if (team.id == "p1") firstName else secondName
                    appendLine("**$name:** ${team.units.first().card.name} — ${team.units.first().hp.coerceAtLeast(0.0).toInt()} HP")
                }
                if (state.combatLog.isNotEmpty()) {
                    appendLine()
                    appendLine(state.combatLog.takeLast(4).joinToString("\n") { "• $it" })
                }
            }

    private fun rows(
        state: CombatState,
        actionId: String,
        abilityIds: List<String>,
        cardIds: List<String>
    ): List<ActionRowBuilder> {
        if (state.isFinished()) return emptyList()
        val rows = mutableListOf<ActionRowBuilder>()
        rows += ActionRowBuilder().apply {
            interactionButton(ButtonStyle.Primary, "$actionId:attack") { label = "⚔️ Atacar" }
            interactionButton(ButtonStyle.Secondary, "$actionId:end") { label = "✅ Encerrar turno" }
            interactionButton(ButtonStyle.Danger, "$actionId:surrender") { label = "🏳️ Render-se" }
        }
        abilityIds.chunked(5).forEach { chunk ->
            rows += ActionRowBuilder().apply {
                chunk.forEachIndexed { index, id ->
                    interactionButton(ButtonStyle.Secondary, id) { label = "✨ Habilidade ${index + 1}" }
                }
            }
        }
        cardIds.chunked(5).forEach { chunk ->
            rows += ActionRowBuilder().apply {
                chunk.forEachIndexed { index, id ->
                    interactionButton(ButtonStyle.Secondary, id) { label = "🃏 Carta ${index + 1}" }
                }
            }
        }
        return rows.take(5)
    }

    private data class Fighter(val unit: me.igorunderplayer.kono.domain.gameplay.Unit?, val remainingCards: List<String>, val error: String?)

    private suspend fun buildFighter(discordId: Long, name: String): Fighter {
        val deck = deckService.getActive(discordId)
            ?: return Fighter(null, emptyList(), "❌ **$name** não possui um deck ativo.")
        val validation = deckService.validate(deck.cardIds)
        if (validation.isNotEmpty()) return Fighter(null, emptyList(), "❌ **$name**: ${validation.joinToString(" ")}")

        val definitions = deck.cardIds.mapNotNull { CardCatalog.getById(it) }
        val character = definitions.singleOrNull { it.type == CardType.CHARACTER }
            ?: return Fighter(null, emptyList(), "❌ **$name** precisa ter exatamente 1 personagem no deck.")
        val ownedCards = cardInstanceRepository.getByDiscordId(discordId)
        val ownedByDefinition = ownedCards.groupingBy { it.definitionId.uppercase() }.eachCount()
        val missing = definitions.groupingBy { it.id.uppercase() }.eachCount()
            .filter { (id, count) -> count > (ownedByDefinition[id] ?: 0) }
        if (missing.isNotEmpty()) {
            return Fighter(null, emptyList(), "❌ **$name** não possui todas as cartas do deck.")
        }
        val owned = ownedCards
            .filter { it.definitionId.equals(character.id, true) }
            .maxByOrNull { it.level }
            ?: return Fighter(null, emptyList(), "❌ **$name** não possui o personagem do deck.")
        val unit = CombatUnitFactory.createUnit(
            unitId = "duel_${discordId}_${owned.id}",
            definition = character,
            level = owned.level,
            equips = emptyList()
        )
        return Fighter(unit, definitions.filter { it.id != character.id }.map { it.id }, null)
    }
}
