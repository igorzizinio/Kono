package me.igorunderplayer.kono.commands.text.testing

import dev.kord.common.entity.ButtonStyle
import dev.kord.core.behavior.edit
import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.behavior.reply
import dev.kord.core.entity.Message
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.rest.builder.component.ActionRowBuilder
import dev.kord.rest.builder.message.embed
import me.igorunderplayer.kono.commands.BaseCommand
import me.igorunderplayer.kono.commands.CommandCategory
import me.igorunderplayer.kono.domain.card.ability.Ability
import me.igorunderplayer.kono.domain.gameplay.CombatState
import me.igorunderplayer.kono.domain.gameplay.Team
import me.igorunderplayer.kono.domain.team.BuildUnitHandler
import me.igorunderplayer.kono.engine.combat.CombatAction
import me.igorunderplayer.kono.engine.combat.CombatEngine
import me.igorunderplayer.kono.engine.combat.PlayerTurnController
import me.igorunderplayer.kono.utils.getMentionedUser
import me.igorunderplayer.kono.utils.interaction.awaitButtonInteraction
import me.igorunderplayer.kono.utils.interaction.awaitFirstButtonInteraction
import me.igorunderplayer.kono.domain.gameplay.Unit as CombatUnit

/**
 * Igual ao StartFightCommand, mas o time do player é controlado ativamente:
 * a cada "hit" do turno da unidade dele, o bot pergunta se quer atacar ou usar
 * uma habilidade ativa, ao invés de resolver tudo automaticamente.
 *
 * O time inimigo continua 100% automático (não registramos controller pra ele).
 */
class StartFightActiveCommand(
    private val buildUnitHandler: BuildUnitHandler,
) : BaseCommand(
    "startfightactive",
    description = "inicia uma batalha contra um oponente, com controle de habilidades ativas",
    category = CommandCategory.Game
) {

    override suspend fun run(event: MessageCreateEvent, args: Array<String>) {
        val playerUser = event.message.author ?: return

        val enemyUser = getMentionedUser(event.message, args)
        if (enemyUser == null || enemyUser.id == event.message.author?.id) return

        val playerUnit = resolveUnitOrReply(
            event = event,
            discordId = playerUser.id.value.toLong(),
            isEnemy = false
        ) ?: return

        val enemyUnit = resolveUnitOrReply(
            event = event,
            discordId = enemyUser.id.value.toLong(),
            isEnemy = true
        ) ?: return

        val combatState = CombatState(
            teams = listOf(
                Team("player", mutableListOf(playerUnit)),
                Team("enemy", mutableListOf(enemyUnit))
            )
        )

        val combatId = "${event.message.channelId}-${event.message.id}"
        val nextRoundButtonId = "$combatId-next"

        var actionMsg: Message = event.message.reply {
            embed {
                title = "⚔️ Combate iniciado"
                description =
                    "${playerUser.username} vs ${enemyUser.username}\n\nClique em **Próxima rodada** para avançar o combate."
            }
            addComponent(createNextRoundRow(nextRoundButtonId))
        }

        // Local suspend fun (não lambda solta) pra poder capturar e reatribuir `actionMsg`
        // diretamente, sem precisar de callback/holder.
        suspend fun promptPlayerAction(unit: CombatUnit, availableAbilities: List<Ability>): CombatAction {
            val actionBaseId = "$combatId-action-${System.currentTimeMillis()}"
            val attackButtonId = "$actionBaseId:attack"
            val abilityButtonIds = availableAbilities.indices.map { "$actionBaseId:ability:$it" }

            actionMsg = actionMsg.edit {
                embed {
                    title = "🎮 Sua vez, ${playerUser.username}"
                    description = "**${unit.card.name}** — escolha uma ação para este golpe."
                }
                addComponent(createActionRow(attackButtonId, unit, availableAbilities, abilityButtonIds))
            }

            val allIds = listOf(attackButtonId) + abilityButtonIds
            val result = event.kord.awaitFirstButtonInteraction(
                ids = allIds,
                allowedUserId = playerUser.id.value.toLong()
            )

            if (result == null) {
                combatState.combatLog += "⏱️ ${unit.card.name} não respondeu a tempo e atacou automaticamente."
                return CombatAction.BasicAttack()
            }

            val (chosenId, buttonInteraction) = result

            return if (chosenId == attackButtonId) {
                buttonInteraction.interaction.respondEphemeral { content = "⚔️ Ataque básico escolhido." }
                CombatAction.BasicAttack()
            } else {
                val abilityIndex = abilityButtonIds.indexOf(chosenId)
                val ability = availableAbilities.getOrNull(abilityIndex)

                if (ability == null) {
                    buttonInteraction.interaction.respondEphemeral { content = "⚠️ Ação inválida, atacando." }
                    CombatAction.BasicAttack()
                } else {
                    buttonInteraction.interaction.respondEphemeral { content = "🌟 [${ability.name}] escolhida." }
                    CombatAction.UseAbility(ability)
                }
            }
        }

        val playerController = PlayerTurnController { unit, availableAbilities ->
            promptPlayerAction(unit, availableAbilities)
        }

        val engine = CombatEngine(
            state = combatState,
            controllersByUnitId = mapOf(playerUnit.id to playerController)
        )

        while (!combatState.isFinished()) {
            val buttonInteraction = event.kord.awaitButtonInteraction(
                customId = nextRoundButtonId,
                allowedUserId = playerUser.id.value.toLong()
            )

            if (buttonInteraction == null) {
                actionMsg.edit {
                    components = mutableListOf(createNextRoundRow(nextRoundButtonId, disabled = true))
                }
                return
            }

            buttonInteraction.interaction.respondEphemeral {
                content = "⚔️ Prosseguindo com a batalha"
            }

            combatState.combatLog.clear()
            // aqui dentro, sempre que for a vez da unidade do player, promptPlayerAction()
            // vai editar actionMsg e suspender até o clique — o engine só continua depois disso.
            engine.processNextTurnControlled()

            val isFinished = combatState.isFinished()
            val title = if (isFinished) {
                "🏁 Combate finalizado"
            } else {
                "⚔️ Turno ${combatState.turn - 1}"
            }

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
                if (!isFinished) {
                    addComponent(createNextRoundRow(nextRoundButtonId))
                }
            }
        }
    }

    private fun createNextRoundRow(customId: String, disabled: Boolean = false) = ActionRowBuilder().apply {
        interactionButton(ButtonStyle.Primary, customId) {
            label = "Próxima rodada"
            this.disabled = disabled
        }
    }

    private fun createActionRow(
        attackButtonId: String,
        unit: CombatUnit,
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

    private suspend fun resolveUnitOrReply(
        event: MessageCreateEvent,
        discordId: Long,
        isEnemy: Boolean
    ): CombatUnit? {
        return when (val result = buildUnitHandler.executeByDiscordId(discordId)) {
            is BuildUnitHandler.Result.Success -> result.unit
            BuildUnitHandler.Result.UserNotFound -> {
                event.message.reply {
                    content = if (isEnemy) {
                        "❌ Seu inimigo ainda não possui registro."
                    } else {
                        "❌ Você ainda não possui registro."
                    }
                }
                null
            }

            BuildUnitHandler.Result.NoActiveCard -> {
                event.message.reply {
                    content = if (isEnemy) {
                        "❌ Seu inimigo ainda não selecionou um personagem ativo."
                    } else {
                        "❌ Você precisa selecionar um personagem ativo."
                    }
                }
                null
            }

            is BuildUnitHandler.Result.CharacterNotFound -> {
                event.message.reply {
                    content = if (isEnemy) {
                        "❌ Não foi possível carregar o personagem ativo do seu inimigo (id ${result.activeCharacterId})."
                    } else {
                        "❌ Não foi possível carregar seu personagem ativo (id ${result.activeCharacterId})."
                    }
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
