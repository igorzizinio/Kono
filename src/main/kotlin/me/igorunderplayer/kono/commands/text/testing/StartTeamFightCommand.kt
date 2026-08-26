package me.igorunderplayer.kono.commands.text.testing

import dev.kord.common.entity.ButtonStyle
import dev.kord.core.Kord
import dev.kord.core.behavior.edit
import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.behavior.reply
import dev.kord.core.entity.Message
import dev.kord.core.entity.User
import dev.kord.core.entity.effectiveName
import dev.kord.core.event.interaction.ButtonInteractionCreateEvent
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.core.on
import dev.kord.rest.builder.component.ActionRowBuilder
import dev.kord.rest.builder.message.embed
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.withTimeoutOrNull
import me.igorunderplayer.kono.commands.BaseCommand
import me.igorunderplayer.kono.commands.CommandCategory
import me.igorunderplayer.kono.domain.card.Stat
import me.igorunderplayer.kono.domain.card.ability.Ability
import me.igorunderplayer.kono.domain.gameplay.CombatState
import me.igorunderplayer.kono.domain.gameplay.Team
import me.igorunderplayer.kono.engine.combat.CombatAction
import me.igorunderplayer.kono.engine.combat.CombatEngine
import me.igorunderplayer.kono.engine.combat.PlayerTurnController
import me.igorunderplayer.kono.engine.combat.RandomAiTurnController
import me.igorunderplayer.kono.engine.combat.TurnController
import me.igorunderplayer.kono.services.TeamBattleService
import me.igorunderplayer.kono.utils.getMentionedUser
import me.igorunderplayer.kono.utils.interaction.awaitFirstButtonInteraction
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import me.igorunderplayer.kono.domain.gameplay.Unit as CombatUnit


class StartTeamFightCommand(
    private val teamBattleService: TeamBattleService,
) : BaseCommand(
    "teamfight",
    description = "inicia uma batalha em equipe (3x3+), cada jogador controlando seu time",
    category = CommandCategory.Game
) {

    override suspend fun run(event: MessageCreateEvent, args: Array<String>) {
        val playerUser = event.message.author ?: return

        val enemyUser = getMentionedUser(event.message, args)
        if (enemyUser == null || enemyUser.id == event.message.author?.id) return

        val isAuto = args.contains("auto")

        val playerUnits = resolveTeamOrReply(event, playerUser, isEnemy = false) ?: return
        val enemyUnits = resolveTeamOrReply(event, enemyUser, isEnemy = true) ?: return

        val combatState = CombatState(
            teams = listOf(
                Team("player", playerUnits.toMutableList()),
                Team("enemy", enemyUnits.toMutableList())
            )
        )

        val combatId = "${event.message.channelId}-${event.message.id}"
        val continueButtonId = "$combatId-continue"
        val allowedUserIds = setOf(playerUser.id.value.toLong(), enemyUser.id.value.toLong())

        var actionMsg: Message = event.message.reply {
            embed {
                title = "⚔️ Batalha em equipe iniciada"
                description = buildString {
                    appendLine("**${playerUser.username}** vs **${enemyUser.username}**")
                    appendLine()
                    appendLine("Time de ${playerUser.username}: ${playerUnits.joinToString(", ") { it.card.name }}")
                    appendLine("Time de ${enemyUser.username}: ${enemyUnits.joinToString(", ") { it.card.name }}")
                    appendLine()
                    appendLine("Clique em **Continuar** para começar.")
                }
            }
            addComponent(createContinueRow(continueButtonId))
        }

        lateinit var engine: CombatEngine

        fun displayNameOf(u: CombatUnit) = combatState.unitDisplayNamesById[u.id] ?: u.card.name
        fun teamIdOf(u: CombatUnit) = combatState.teams.first { it.units.contains(u) }.id

        suspend fun promptTargetSelection(
            owner: User,
            unit: CombatUnit,
            ability: Ability,
            candidates: List<CombatUnit>
        ): CombatUnit? {
            val targetBaseId = "$combatId-target-${System.currentTimeMillis()}"
            val targetButtonIds = candidates.indices.map { "$targetBaseId:$it" }

            actionMsg = actionMsg.edit {
                embed {
                    title = "🎯 ${owner.username}, escolha o alvo de [${ability.name}]"
                    description = candidates.joinToString("\n") {
                        "• **${displayNameOf(it)}** (${it.hp.toInt()} HP) — time ${teamIdOf(it)}"
                    }
                }
                addComponent(ActionRowBuilder().apply {
                    candidates.forEachIndexed { index, candidate ->
                        interactionButton(ButtonStyle.Secondary, targetButtonIds[index]) {
                            label = displayNameOf(candidate).take(80)
                        }
                    }
                })
            }

            val result = event.kord.awaitFirstButtonInteraction(
                ids = targetButtonIds,
                allowedUserId = owner.id.value.toLong()
            ) ?: run {
                combatState.combatLog += "⏱️ ${unit.card.name} não escolheu alvo a tempo, alvo automático será usado."
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

        suspend fun promptPlayerAction(owner: User, unit: CombatUnit, availableAbilities: List<Ability>): CombatAction {
            val actionBaseId = "$combatId-action-${System.currentTimeMillis()}"
            val attackButtonId = "$actionBaseId:attack"
            val abilityButtonIds = availableAbilities.indices.map { "$actionBaseId:ability:$it" }

            val maxHp = unit.stats[Stat.HP]?.toInt()
            val hpText = if (maxHp != null) "${unit.hp.toInt()}/$maxHp HP" else "${unit.hp.toInt()} HP"

            actionMsg = actionMsg.edit {
                embed {
                    title = "🎮 Vez de ${owner.username}"
                    description = buildString {
                        appendLine("Controlando **${unit.card.name}** (time ${teamIdOf(unit)}) — $hpText")
                        appendLine()
                        if (availableAbilities.isEmpty()) {
                            appendLine("Nenhuma habilidade ativa disponível — ataque ou aguarde novas cartas.")
                        } else {
                            appendLine("Habilidades disponíveis:")
                            availableAbilities.forEach { appendLine("• **${it.name}**") }
                        }
                    }
                }
                addComponent(createActionRow(attackButtonId, availableAbilities, abilityButtonIds))
            }

            val allIds = listOf(attackButtonId) + abilityButtonIds
            val result = event.kord.awaitFirstButtonInteraction(
                ids = allIds,
                allowedUserId = owner.id.value.toLong()
            )

            if (result == null) {
                combatState.combatLog += "⏱️ ${unit.card.name} (${owner.username}) não respondeu a tempo e atacou automaticamente."
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
                if (candidates.size > 1) promptTargetSelection(owner, unit, ability, candidates) else null
            } else {
                null
            }

            return CombatAction.UseAbility(ability, target)
        }

        val controllersByUnitId: Map<String, TurnController> = buildMap {
            playerUnits.forEach { unit ->
                put(unit.id, if (isAuto) RandomAiTurnController() else PlayerTurnController { u, abilities -> promptPlayerAction(playerUser, u, abilities) })
            }
            enemyUnits.forEach { unit ->
                put(unit.id, if (isAuto) RandomAiTurnController() else PlayerTurnController { u, abilities -> promptPlayerAction(enemyUser, u, abilities) })
            }
        }

        engine = CombatEngine(state = combatState, controllersByUnitId = controllersByUnitId)

        while (!combatState.isFinished()) {
            val interaction = awaitButtonFromAny(
                kord = event.kord,
                customId = continueButtonId,
                allowedUserIds = allowedUserIds
            )

            if (interaction == null) {
                actionMsg.edit {
                    components = mutableListOf(createContinueRow(continueButtonId, disabled = true))
                }
                return
            }

            interaction.interaction.respondEphemeral { content = "⚔️ Prosseguindo com a batalha" }

            combatState.combatLog.clear()
            // Dentro dessa chamada, pra cada unidade com controller (ambos os
            // times, agora), promptPlayerAction() edita actionMsg e suspende
            // até o dono clicar — o engine só segue depois disso.
            engine.processNextTurnControlled()

            val isFinished = combatState.isFinished()
            val title = if (isFinished) "🏁 Combate finalizado" else "⚔️ Turno ${combatState.turn - 1}"

            val description = if (isFinished) {
                buildFinalCombatDescription(
                    combatState = combatState,
                    turnLog = combatState.combatLog,
                    playerName = playerUser.username,
                    enemyName = enemyUser.username
                )
            } else if (combatState.combatLog.isEmpty()) {
                "Nenhum evento foi registrado neste turno."
            } else {
                combatState.combatLog.joinToString("\n")
            }

            actionMsg = actionMsg.edit {
                embed {
                    this.title = title
                    this.description = description
                }
                if (!isFinished) addComponent(createContinueRow(continueButtonId))
            }
        }
    }

    /**
     * Igual ao teu awaitButtonInteraction, mas aceitando clique de qualquer
     * usuário dentro de [allowedUserIds] — os dois jogadores da luta, no caso
     * do botão "Continuar". Se quiser, dá pra mover isso pro teu arquivo de
     * utils de interação como um overload.
     */
    private suspend fun awaitButtonFromAny(
        kord: Kord,
        customId: String,
        allowedUserIds: Set<Long>,
        timeout: Duration = 60.seconds
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
            label = "Continuar"
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

    /**
     * Chama o resolver de time injetado no construtor. Troque a lambda na
     * hora de instanciar o comando pra apontar pro seu sistema de times já
     * existente (o mesmo que o modo automático usa) — ex:
     *
     * ```
     * StartTeamFightCommand { discordId ->
     *     when (val result = buildTeamHandler.executeByDiscordId(discordId)) {
     *         is BuildTeamHandler.Result.Success -> TeamResolution.Success(result.units)
     *         BuildTeamHandler.Result.UserNotFound -> TeamResolution.Failure("você ainda não possui registro.")
     *         BuildTeamHandler.Result.NoTeamBuilt -> TeamResolution.Failure("você ainda não montou um time.")
     *     }
     * }
     * ```
     */
    private suspend fun resolveTeamOrReply(
        event: MessageCreateEvent,
        discordUser: User,
        isEnemy: Boolean
    ): List<CombatUnit>? {
        return when (val result =
            teamBattleService.buildPlayerRoster(discordUser.id.value.toLong(), discordUser.effectiveName)) {
            is TeamBattleService.RosterResult.Success -> result.units

            is TeamBattleService.RosterResult.Failure -> {
                event.message.reply {
                    content = if (isEnemy) "[Oponente] " else " " + result.message
                }
                null
            }
        }
    }

    private fun buildFinalCombatDescription(
        combatState: CombatState,
        turnLog: List<String>,
        playerName: String,
        enemyName: String
    ): String {
        val logText = if (turnLog.isEmpty()) {
            "Nenhum evento foi registrado neste turno."
        } else {
            turnLog.joinToString("\n")
        }

        val playerAlive = combatState.teams.firstOrNull { it.id == "player" }?.units?.any { it.hp > 0 } == true
        val enemyAlive = combatState.teams.firstOrNull { it.id == "enemy" }?.units?.any { it.hp > 0 } == true

        val resultText = when {
            playerAlive && !enemyAlive -> "🏆 **Vencedor:** $playerName\n💀 **Perdedor:** $enemyName"
            enemyAlive && !playerAlive -> "🏆 **Vencedor:** $enemyName\n💀 **Perdedor:** $playerName"
            else -> "🤝 **Resultado:** empate"
        }

        return buildString {
            appendLine(logText)
            appendLine()
            appendLine(resultText)
        }.trim()
    }
}
