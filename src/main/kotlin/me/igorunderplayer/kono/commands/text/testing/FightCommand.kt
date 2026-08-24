package me.igorunderplayer.kono.commands.text.testing

import dev.kord.common.entity.ButtonStyle
import dev.kord.core.behavior.edit
import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.behavior.interaction.response.createEphemeralFollowup
import dev.kord.core.behavior.reply
import dev.kord.core.event.interaction.ButtonInteractionCreateEvent
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.core.on
import dev.kord.rest.builder.component.ActionRowBuilder
import dev.kord.rest.builder.message.embed
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.time.delay
import kotlinx.coroutines.withTimeoutOrNull
import me.igorunderplayer.kono.commands.BaseCommand
import me.igorunderplayer.kono.commands.CommandCategory
import me.igorunderplayer.kono.data.repositories.CardRepository
import me.igorunderplayer.kono.domain.card.CardDefinition
import me.igorunderplayer.kono.domain.card.CardType
import me.igorunderplayer.kono.domain.card.Stat
import me.igorunderplayer.kono.domain.card.ability.Ability
import me.igorunderplayer.kono.domain.gameplay.CombatState
import me.igorunderplayer.kono.domain.gameplay.Team
import me.igorunderplayer.kono.domain.gameplay.Unit
import me.igorunderplayer.kono.domain.team.BuildUnitHandler
import me.igorunderplayer.kono.engine.combat.CombatAction
import me.igorunderplayer.kono.engine.combat.CombatEngine
import me.igorunderplayer.kono.engine.combat.PlayerTurnController
import me.igorunderplayer.kono.engine.combat.RandomAiTurnController
import me.igorunderplayer.kono.engine.combat.TurnController
import me.igorunderplayer.kono.utils.getMentionedUser
import me.igorunderplayer.kono.utils.interaction.awaitButtonInteraction
import me.igorunderplayer.kono.utils.interaction.awaitFirstButtonInteraction
import java.time.Duration
import kotlin.random.Random
import kotlin.time.Duration as KotlinDuration
import kotlin.time.Duration.Companion.seconds

class FightCommand(
    private val buildUnitHandler: BuildUnitHandler,
    private val cardRepository: CardRepository
) : BaseCommand(
    name = "fight",
    description = "Luta contra um inimigo",
    category = CommandCategory.Game
) {

    companion object {
        private const val EMBED_DESCRIPTION_LIMIT = 3500
    }

    override suspend fun run(event: MessageCreateEvent, args: Array<String>) {
        val discordId = event.message.author?.id?.value?.toLong() ?: return

        val enemyUser = getMentionedUser(event.message, args)

        val autoBattle = args.contains("auto")

        if (enemyUser == null) {
            fightWithBot(event, autoBattle, args)
            return
        }

        val player = resolveUnitOrReply(
            event = event,
            discordId = discordId,
            isEnemy = false
        ) ?: return

        val enemy = resolveUnitOrReply(
            event = event,
            discordId = enemyUser.id.value.toLong(),
            isEnemy = true
        ) ?: return

        val playerOwnerName = event.message.author?.username ?: "Jogador"
        val enemyOwnerName = enemyUser.username

        runCombat(
            event = event,
            player = player,
            enemy = enemy,
            playerOwnerName = playerOwnerName,
            enemyOwnerName = enemyOwnerName,
            playerDiscordId = discordId,
            enemyDiscordId = enemyUser.id.value.toLong(),
            isPvp = true,
            isAuto = true
        )
    }

    private suspend fun fightWithBot(event: MessageCreateEvent, auto: Boolean, args: Array<String>) {
        val discordId = event.message.author?.id?.value?.toLong() ?: return
        val enemyName = args.getOrNull(0)?.uppercase()

        if (enemyName == null) {
            event.message.channel.createMessage("⚠️ Use: `fight <enemy_id> (exemplo: slime)`")
            return
        }

        val player = resolveUnitOrReply(
            event = event,
            discordId = discordId,
            isEnemy = false
        ) ?: return

        val enemyDef = cardRepository.getDefinition(enemyName)

        if (enemyDef == null || enemyDef.type != CardType.CHARACTER) {
            event.message.channel.createMessage("❌ Inimigo inválido.")
            return
        }

        val enemy = createUnitFromDefinition(enemyDef)
        val playerOwnerName = event.message.author?.username ?: "Jogador"

        runCombat(
            event = event,
            player = player,
            enemy = enemy,
            playerOwnerName = playerOwnerName,
            enemyOwnerName = "Bot",
            playerDiscordId = discordId,
            enemyDiscordId = null,
            isPvp = false,
            isAuto = auto
        )
    }

    private fun createUnitFromDefinition(def: CardDefinition): Unit {
        val stats = def.baseStats.toMutableMap()

        return Unit(
            id = "enemy_${def.id}",
            card = def,
            hp = stats[Stat.HP] ?: 100.0,
            stats = stats,
            abilities = def.abilities.toList(),
            tags = def.tags
        )
    }

    private suspend fun resolveUnitOrReply(
        event: MessageCreateEvent,
        discordId: Long,
        isEnemy: Boolean
    ): Unit? {
        return when (val result = buildUnitHandler.executeByDiscordId(discordId)) {
            is BuildUnitHandler.Result.Success -> result.unit
            BuildUnitHandler.Result.UserNotFound -> {
                event.message.channel.createMessage(
                    if (isEnemy) {
                        "❌ Seu inimigo ainda não possui registro."
                    } else {
                        "❌ Você ainda não possui registro."
                    }
                )
                null
            }

            BuildUnitHandler.Result.NoActiveCard -> {
                event.message.channel.createMessage(
                    if (isEnemy) {
                        "❌ Seu inimigo ainda não selecionou um personagem ativo."
                    } else {
                        "❌ Você precisa selecionar um personagem ativo. Use: `setactive <instance_id>` (veja ids com `inventory char`)"
                    }
                )
                null
            }

            is BuildUnitHandler.Result.CharacterNotFound -> {
                event.message.channel.createMessage(
                    if (isEnemy) {
                        "❌ Não foi possível carregar o personagem ativo do seu inimigo (id ${result.activeCharacterId})."
                    } else {
                        "❌ Não foi possível carregar seu personagem ativo (id ${result.activeCharacterId}). Use `setactive <instance_id>` novamente."
                    }
                )
                null
            }
        }
    }

    /**
     * Roda o combate rodada a rodada (em vez de engine.run() de uma vez), pra
     * dar espaço aos prompts de ação. Se [isPvp] for true, os dois lados são
     * PlayerTurnController (cada um preso ao próprio Discord ID). Se for
     * false, o time "enemy" usa RandomAiTurnController (IA aleatória, sem
     * prompt) e [enemyDiscordId] pode ser null.
     */
    private suspend fun runCombat(
        event: MessageCreateEvent,
        player: Unit,
        enemy: Unit,
        playerOwnerName: String,
        enemyOwnerName: String,
        playerDiscordId: Long,
        enemyDiscordId: Long?,
        isPvp: Boolean,
        isAuto: Boolean
    ) {
        val (playerDisplayName, enemyDisplayName) = resolveCombatantDisplayNames(
            playerName = player.card.name,
            enemyName = enemy.card.name,
            playerOwnerName = playerOwnerName,
            enemyOwnerName = enemyOwnerName
        )

        val state = CombatState(
            teams = listOf(
                Team("player", mutableListOf(player)),
                Team("enemy", mutableListOf(enemy))
            ),
            rng = Random.Default
        )
        state.unitDisplayNamesById[player.id] = playerDisplayName
        state.unitDisplayNamesById[enemy.id] = enemyDisplayName

        val playerStartHp = player.hp
        val enemyStartHp = enemy.hp

        val combatId = "${event.message.channelId}-${event.message.id}-${System.currentTimeMillis()}"
        val continueButtonId = "$combatId-continue"
        val allowedContinueIds = if (isPvp && enemyDiscordId != null) {
            setOf(playerDiscordId, enemyDiscordId)
        } else {
            setOf(playerDiscordId)
        }

        // Junta o log de todas as rodadas pro diário de batalha final —
        // state.combatLog é limpo a cada rodada só pra mostrar o embed daquela rodada.
        val fullCombatLog = mutableListOf<String>()

        var msg = event.message.reply {
            embed {
                title = "⚔️ Combate iniciado"
                description = buildString {
                    appendLine("👤 **$playerDisplayName** (${playerStartHp.toInt()} HP)")
                    appendLine("👹 **$enemyDisplayName** (${enemyStartHp.toInt()} HP)")
                    appendLine()
                    appendLine("Clique em **Próxima rodada** para avançar o combate.")
                }
            }
            addComponent(createContinueRow(continueButtonId))
        }

        // Atribuído logo abaixo — só é lido de dentro dos prompts, que só
        // rodam durante engine.processNextTurnControlled(), ou seja, depois de pronto.
        lateinit var engine: CombatEngine

        fun displayNameOf(u: Unit) = state.unitDisplayNamesById[u.id] ?: u.card.name
        fun teamIdOf(u: Unit) = state.teams.first { it.units.contains(u) }.id
        fun ownerNameOf(u: Unit) = if (teamIdOf(u) == "player") playerOwnerName else enemyOwnerName
        fun ownerDiscordIdOf(u: Unit): Long? = if (teamIdOf(u) == "player") playerDiscordId else enemyDiscordId

        suspend fun promptTargetSelection(unit: Unit, ability: Ability, candidates: List<Unit>): Unit? {
            val ownerId = ownerDiscordIdOf(unit) ?: return null
            val targetBaseId = "$combatId-target-${System.currentTimeMillis()}"
            val targetButtonIds = candidates.indices.map { "$targetBaseId:$it" }

            msg = msg.edit {
                embed {
                    title = "🎯 ${ownerNameOf(unit)}, escolha o alvo de [${ability.name}]"
                    description = candidates.joinToString("\n") {
                        "• **${displayNameOf(it)}** (${it.hp.toInt()} HP) — time ${teamIdOf(it)}"
                    }
                }
                components = mutableListOf(ActionRowBuilder().apply {
                    candidates.forEachIndexed { index, candidate ->
                        interactionButton(ButtonStyle.Secondary, targetButtonIds[index]) {
                            label = displayNameOf(candidate).take(80)
                        }
                    }
                })
            }

            val result = event.kord.awaitFirstButtonInteraction(
                ids = targetButtonIds,
                allowedUserId = ownerId
            ) ?: run {
                state.combatLog += "⏱️ ${unit.card.name} não escolheu alvo a tempo, alvo automático será usado."
                return null
            }

            val (chosenId, buttonInteraction) = result
            val chosenTarget = candidates.getOrNull(targetButtonIds.indexOf(chosenId))

            buttonInteraction.interaction.respondEphemeral {
                content = if (chosenTarget != null) {
                    "🎯 Alvo: ${displayNameOf(chosenTarget)}"
                } else {
                    "⚠️ Alvo inválido, usando seleção automática."
                }
            }

            return chosenTarget
        }

        suspend fun promptPlayerAction(unit: Unit, availableAbilities: List<Ability>): CombatAction {
            val ownerId = ownerDiscordIdOf(unit) ?: return CombatAction.BasicAttack()
            val ownerName = ownerNameOf(unit)

            val actionBaseId = "$combatId-action-${System.currentTimeMillis()}"
            val attackButtonId = "$actionBaseId:attack"
            val abilityButtonIds = availableAbilities.indices.map { "$actionBaseId:ability:$it" }

            val maxHp = unit.stats[Stat.HP]?.toInt()
            val hpText = if (maxHp != null) "${unit.hp.toInt()}/$maxHp HP" else "${unit.hp.toInt()} HP"

            msg = msg.edit {
                embed {
                    title = "🎮 Vez de $ownerName"
                    description = buildString {
                        appendLine("Controlando **${unit.card.name}** — $hpText")
                        appendLine()
                        if (availableAbilities.isEmpty()) {
                            appendLine("Nenhuma habilidade ativa disponível — ataque ou aguarde novas cartas.")
                        } else {
                            appendLine("Habilidades disponíveis:")
                            availableAbilities.forEach { appendLine("• **${it.name}**") }
                        }
                    }
                }
                components = mutableListOf(createActionRow(attackButtonId, availableAbilities, abilityButtonIds))
            }

            val allIds = listOf(attackButtonId) + abilityButtonIds
            val result = event.kord.awaitFirstButtonInteraction(ids = allIds, allowedUserId = ownerId)

            if (result == null) {
                state.combatLog += "⏱️ ${unit.card.name} ($ownerName) não respondeu a tempo e atacou automaticamente."
                return CombatAction.BasicAttack()
            }

            val (chosenId, buttonInteraction) = result

            if (chosenId == attackButtonId) {
                buttonInteraction.interaction.respondEphemeral { content = "⚔️ Ataque básico escolhido." }
                return CombatAction.BasicAttack()
            }

            val ability = availableAbilities.getOrNull(abilityButtonIds.indexOf(chosenId))
            if (ability == null) {
                buttonInteraction.interaction.respondEphemeral { content = "⚠️ Ação inválida, atacando." }
                return CombatAction.BasicAttack()
            }

            buttonInteraction.interaction.respondEphemeral { content = "🌟 [${ability.name}] escolhida." }

            val manualKind = engine.manualTargetKind(ability)
            val target = if (manualKind != null) {
                val candidates = engine.manualTargetCandidates(unit, manualKind)
                if (candidates.size > 1) promptTargetSelection(unit, ability, candidates) else null
            } else {
                null
            }

            return CombatAction.UseAbility(ability, target)
        }

        val playerController: TurnController = if (isAuto) {
            RandomAiTurnController()
        } else {
            PlayerTurnController { u, abilities -> promptPlayerAction(u, abilities) }
        }

        val enemyController: TurnController = if (!isAuto && isPvp) {
            PlayerTurnController { u, abilities -> promptPlayerAction(u, abilities) }
        } else {
            RandomAiTurnController()
        }

        val controllersByUnitId: Map<String, TurnController> = mapOf(
            player.id to playerController,
            enemy.id to enemyController
        )

        engine = CombatEngine(state = state, controllersByUnitId = controllersByUnitId)

        while (!state.isFinished()) {
            if (isAuto) {
                state.combatLog.clear()
                engine.processNextTurnControlled()
                fullCombatLog += state.combatLog
                continue
            }
            val buttonInteraction = if (allowedContinueIds.size > 1) {
                awaitButtonFromAny(event.kord, continueButtonId, allowedContinueIds)
            } else {
                event.kord.awaitButtonInteraction(continueButtonId, allowedContinueIds.first())
            }

            if (buttonInteraction == null) {
                msg.edit {
                    components = mutableListOf(createContinueRow(continueButtonId, disabled = true))
                }
                return
            }

            buttonInteraction.interaction.respondEphemeral { content = "⚔️ Prosseguindo com a batalha" }

            state.combatLog.clear()
            // Dentro dessa chamada, promptPlayerAction() edita msg e suspende
            // pra cada unidade com controller — o engine só segue depois disso.
            engine.processNextTurnControlled()
            fullCombatLog += state.combatLog

            val isFinished = state.isFinished()
            if (!isFinished) {
                msg = msg.edit {
                    embed {
                        title = "⚔️ Turno ${state.turn - 1}"
                        description = if (state.combatLog.isEmpty()) {
                            "Nenhum evento foi registrado neste turno."
                        } else {
                            state.combatLog.joinToString("\n")
                        }
                    }
                    components = mutableListOf(createContinueRow(continueButtonId))
                }
            }
        }

        val playerAlive = player.hp > 0

        val summary = buildCombatSummaryEmbed(
            playerDisplayName = playerDisplayName,
            enemyDisplayName = enemyDisplayName,
            playerStartHp = playerStartHp,
            enemyStartHp = enemyStartHp,
            playerFinalHp = player.hp,
            enemyFinalHp = enemy.hp,
            playerAlive = playerAlive,
        )

        val logPages = buildCombatLogEmbeds(fullCombatLog)
        val logButtonId = "$combatId-log"

        msg = msg.edit {
            embed {
                title = summary.title
                description = summary.description
                summary.footer?.let { footerText -> footer { text = footerText } }
            }
            components = mutableListOf(createLogButton(logButtonId))
        }

        val logClick = event.kord.awaitButtonInteraction(
            customId = logButtonId,
            allowedUserId = playerDiscordId
        ) ?: run {
            msg.edit {
                components = mutableListOf(createLogButton(logButtonId, disabled = true))
            }
            return
        }

        val firstPage = logPages.first()

        val response = logClick.interaction.respondEphemeral {
            content = "📜 Diário de batalha"
            embed {
                title = firstPage.title
                description = firstPage.description
                firstPage.footer?.let { footerText -> footer { text = footerText } }
            }
        }

        logPages.subList(1, logPages.size).forEach { page ->
            response.createEphemeralFollowup {
                embed {
                    title = page.title
                    description = page.description
                    page.footer?.let { footerText -> footer { text = footerText } }
                }
            }
            delay(Duration.ofMillis(250)) // delay para não enviar tudo de uma vez
        }
    }

    /**
     * Igual ao awaitButtonInteraction que vocês já têm, mas aceita clique de
     * qualquer usuário dentro de [allowedUserIds] — usado no botão "Próxima
     * rodada" em lutas PvP, que os dois jogadores podem apertar.
     */
    private suspend fun awaitButtonFromAny(
        kord: dev.kord.core.Kord,
        customId: String,
        allowedUserIds: Set<Long>,
        timeout: KotlinDuration = 60.seconds
    ): ButtonInteractionCreateEvent? {
        val pressed = CompletableDeferred<ButtonInteractionCreateEvent>()
        val listener = kord.on<ButtonInteractionCreateEvent> {
            val interaction = this.interaction
            if (interaction.component.customId != customId) return@on
            if (interaction.user.id.value.toLong() !in allowedUserIds) return@on
            if (!pressed.isCompleted) pressed.complete(this)
        }
        return try {
            withTimeoutOrNull(timeout) { pressed.await() }
        } finally {
            listener.cancel()
        }
    }

    private fun createContinueRow(customId: String, disabled: Boolean = false) = ActionRowBuilder().apply {
        interactionButton(ButtonStyle.Primary, customId) {
            label = "Próxima rodada"
            this.disabled = disabled
        }
    }

    private fun createActionRow(
        attackButtonId: String,
        availableAbilities: List<Ability>,
        abilityButtonIds: List<String>
    ) = ActionRowBuilder().apply {
        interactionButton(ButtonStyle.Primary, attackButtonId) {
            label = "⚔️ Atacar"
        }

        // Discord permite no máximo 5 botões por linha (1 já usado pro ataque).
        // TODO: se algum personagem puder ter mais de 4 habilidades ativas
        // disponíveis ao mesmo tempo, quebrar em uma segunda ActionRow.
        availableAbilities.take(4).forEachIndexed { index, ability ->
            interactionButton(ButtonStyle.Secondary, abilityButtonIds[index]) {
                label = ability.name.take(80)
            }
        }
    }

    private fun buildCombatSummaryEmbed(
        playerDisplayName: String,
        enemyDisplayName: String,
        playerStartHp: Double,
        enemyStartHp: Double,
        playerFinalHp: Double,
        enemyFinalHp: Double,
        playerAlive: Boolean,
    ): CombatEmbedPage {
        val summaryDescription = buildString {
            appendLine("⚔️ **Combate iniciado!**")
            appendLine("👤 **Jogador:** $playerDisplayName (${playerStartHp.toInt()} HP)")
            appendLine("👹 **Inimigo:** $enemyDisplayName (${enemyStartHp.toInt()} HP)")
            appendLine()
            appendLine(if (playerAlive) "🏆 **Resultado:** Voce venceu!" else "💀 **Resultado:** Voce perdeu...")
            appendLine("❤️ **HP final Jogador:** ${playerFinalHp.coerceAtLeast(0.0).toInt()}")
            appendLine("💔 **HP final Inimigo:** ${enemyFinalHp.coerceAtLeast(0.0).toInt()}")
        }

        return CombatEmbedPage(
            title = "⚔️ Resultado do Combate",
            description = summaryDescription.trim(),
            footer = "Clique no botão abaixo para ver o diário em modo privado"
        )
    }

    private fun resolveCombatantDisplayNames(
        playerName: String,
        enemyName: String,
        playerOwnerName: String,
        enemyOwnerName: String
    ): Pair<String, String> {
        if (!playerName.equals(enemyName, ignoreCase = true)) {
            return playerName to enemyName
        }

        return "$playerName de ${playerOwnerName.trim()}" to "$enemyName de ${enemyOwnerName.trim()}"
    }

    private fun buildCombatLogEmbeds(eventLog: List<String>): List<CombatEmbedPage> {
        val eventPages = paginateEventLog(eventLog)
        val totalPages = eventPages.size

        return eventPages.mapIndexed { index, page ->
            CombatEmbedPage(
                title = "📜 Diario de Batalha",
                description = page,
                footer = "Pagina ${index + 1}/$totalPages"
            )
        }
    }

    private fun createLogButton(customButtonId: String, disabled: Boolean = false) = ActionRowBuilder().apply {
        interactionButton(ButtonStyle.Primary, customButtonId) {
            label = "Ver diário"
            this.disabled = disabled
        }
    }

    private fun paginateEventLog(eventLog: List<String>): List<String> {
        val lines = eventLog.ifEmpty {
            listOf("ℹ️ Nenhum evento foi registrado durante a luta.")
        }

        val pages = mutableListOf<String>()
        val current = StringBuilder()

        for (rawLine in lines) {
            val line = if (rawLine.length > EMBED_DESCRIPTION_LIMIT - 8) {
                "${rawLine.take(EMBED_DESCRIPTION_LIMIT - 11)}..."
            } else {
                rawLine
            }

            val formattedLine = "• $line"

            if (current.isNotEmpty() && current.length + formattedLine.length + 1 > EMBED_DESCRIPTION_LIMIT) {
                pages += current.toString()
                current.clear()
            }

            if (current.isNotEmpty()) current.append('\n')
            current.append(formattedLine)
        }

        if (current.isNotEmpty()) {
            pages += current.toString()
        }

        return pages
    }

    private data class CombatEmbedPage(
        val title: String,
        val description: String,
        val footer: String?
    )
}
