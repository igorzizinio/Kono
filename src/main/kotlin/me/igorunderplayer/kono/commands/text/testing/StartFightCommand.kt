package me.igorunderplayer.kono.commands.text.testing

import dev.kord.common.entity.ButtonStyle
import dev.kord.core.behavior.edit
import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.behavior.reply
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.rest.builder.component.ActionRowBuilder
import dev.kord.rest.builder.message.embed
import me.igorunderplayer.kono.commands.BaseCommand
import me.igorunderplayer.kono.commands.CommandCategory
import me.igorunderplayer.kono.engine.combat.CombatEngine
import me.igorunderplayer.kono.domain.gameplay.CombatState
import me.igorunderplayer.kono.domain.gameplay.Team
import me.igorunderplayer.kono.domain.gameplay.Unit as CombatUnit
import me.igorunderplayer.kono.domain.team.BuildUnitHandler
import me.igorunderplayer.kono.utils.getMentionedUser
import me.igorunderplayer.kono.utils.interaction.awaitButtonInteraction

class StartFightCommand(
    private val buildUnitHandler: BuildUnitHandler,
): BaseCommand(
    "startfight",
    description = "inicia uma batalha contra um oponente",
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
        val engine = CombatEngine(combatState)

        val customButtonId = "${event.message.channelId}-${event.message.id}-${System.currentTimeMillis()}"

        var msg = event.message.reply {
            embed {
                title = "⚔️ Combate iniciado"
                description = "${playerUser.username} vs ${enemyUser.username}\n\nClique em **Próxima rodada** para avançar o combate."
            }

            addComponent(createActionRow(customButtonId))
        }

        while (!combatState.isFinished()) {
            val buttonInteraction = event.kord.awaitButtonInteraction(
                customId = customButtonId,
                allowedUserId = playerUser.id.value.toLong()
            )

            if (buttonInteraction == null) {
                msg.edit {
                    embed {
                        title = "⌛ Combate encerrado"
                        description = "Tempo esgotado. A batalha foi finalizada sem novas ações."
                    }
                }
                return
            }

            buttonInteraction.interaction.respondEphemeral {
                content = "⚔️ Prosseguindo com a batalha"
            }

            combatState.combatLog.clear()
            engine.processNextTurn()

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

            msg = msg.edit {
                embed {
                    this.title = title
                    this.description = description
                }

                if (!isFinished) {
                    addComponent(createActionRow(customButtonId))
                }
            }
        }
    }

    private fun createActionRow(customButtonId: String) = ActionRowBuilder().apply {
        interactionButton(
            ButtonStyle.Primary,
            customButtonId,
        ) {
            label = "Próxima rodada"
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
