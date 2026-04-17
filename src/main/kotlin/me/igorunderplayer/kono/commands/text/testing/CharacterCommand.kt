package me.igorunderplayer.kono.commands.text.testing

import dev.kord.common.entity.ButtonStyle
import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.behavior.reply
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.rest.builder.component.ActionRowBuilder
import dev.kord.rest.builder.message.embed
import me.igorunderplayer.kono.commands.BaseCommand
import me.igorunderplayer.kono.domain.card.prettyName
import me.igorunderplayer.kono.domain.card.prettyValue
import me.igorunderplayer.kono.domain.card.toDisplayEmoji
import me.igorunderplayer.kono.domain.team.BuildUnitHandler
import me.igorunderplayer.kono.domain.team.SetActiveCharacterHandler
import me.igorunderplayer.kono.domain.team.UpgradeCharacterHandler
import me.igorunderplayer.kono.utils.interaction.awaitButtonInteraction

class CharacterCommand(
    private val setActiveCharacterHandler: SetActiveCharacterHandler,
    private val buildUnitHandler: BuildUnitHandler,
    private val upgradeCharacterHandler: UpgradeCharacterHandler
): BaseCommand(
    name = "character",
    description = "comandos relacionados ao personagem ativo em campo",
    aliases = listOf("char", "personagem", "perso")
) {
    override suspend fun run(event: MessageCreateEvent, args: Array<String>) {
        val userId = event.message.author?.id?.value ?: return
        val args = args.toMutableList()
        if (args.isEmpty()) {
            event.message.reply {
                content = "Use: `character <info | set | upgrade>`"
            }
            return
        }

        val option = args.removeFirst().lowercase()

        when (option) {
            "set" -> {
                if (args.isEmpty()) {
                    event.message.reply {
                        content = "Use `character set <id>` para definir seu personagem ativo. Exemplo: `character set 42`."
                    }
                    return
                }

                val instanceId = args[0].toIntOrNull()
                if (instanceId == null || instanceId <= 0) {
                    event.message.reply {
                        content = "Instance ID inválido. Use um número inteiro positivo, exemplo: `setactive 42`."
                    }
                    return
                }

                when (val result = setActiveCharacterHandler.execute(userId.toLong(), instanceId)) {
                    is SetActiveCharacterHandler.Result.Success -> {
                        event.message.reply {
                            content = "Personagem '${result.characterName}' (#${result.instanceId}) definido como ativo com sucesso!"
                        }
                    }

                    is SetActiveCharacterHandler.Result.CharacterNotFound -> {
                        event.message.reply {
                            content = "Não encontrei um personagem com instance ID #${result.instanceId} na sua conta."
                        }
                    }

                    is SetActiveCharacterHandler.Result.UserNotFound -> {
                        event.message.reply {
                            content = "Usuário não encontrado. Por favor, registre-se usando o comando 'register'."
                        }
                    }
                }

            }

            "info" -> {
                when (val result = buildUnitHandler.executeByDiscordId(userId.toLong())) {
                    is BuildUnitHandler.Result.Success -> {
                        val upgradeHint = buildUpgradeHint(userId.toLong())

                        val descriptionBuilder = buildString {
                            appendLine(result.unit.card.description)
                            appendLine()

                            if (upgradeHint != null) {
                                appendLine(upgradeHint)
                                appendLine()
                            }

                            appendLine("**Equipamentos**:")
                            if (result.unit.equipments.isEmpty()) {
                                appendLine("- Nenhum equipamento")
                            } else {
                                result.unit.equipments.forEach {
                                    appendLine("${it.rarity.toDisplayEmoji()} ${it.name}")
                                }
                            }

                            appendLine()
                            appendLine("**Status:**")
                            result.unit.stats.forEach { (stat, value) ->
                                appendLine("- **${stat.prettyName()}**: ${prettyValue(stat, value)}")
                            }
                        }

                        event.message.reply {
                            content = "informações sobre seu personagem ativo!!"
                            embed {
                                title = "${result.unit.card.rarity.toDisplayEmoji()} ${result.unit.card.name}"
                                description = descriptionBuilder
                            }
                        }
                    }

                    is BuildUnitHandler.Result.UserNotFound -> {
                        event.message.reply {
                            content = "Usuário não encontrado"
                        }
                    }

                    is BuildUnitHandler.Result.NoActiveCard -> {
                        event.message.reply {
                            content = "Nenhum personagem ativo selecionado. Use: setactive <instance_id>"
                        }
                    }

                    is BuildUnitHandler.Result.CharacterNotFound -> {
                        event.message.reply{
                            content = "Personagem ativo (#${result.activeCharacterId}) não encontrado no banco de dados"
                        }
                    }
                }
            }

            "upgrade" -> {
                val preview = upgradeCharacterHandler.previewActiveCharacter(userId.toLong())

                when (preview) {
                    is UpgradeCharacterHandler.PreviewResult.Ready -> {
                        val buttonId = "char-upgrade-${event.message.id}-${System.currentTimeMillis()}"

                        event.message.reply {
                            content = buildString {
                                appendLine("⚠️ **Confirmar upgrade** de **${preview.characterName}** (#${preview.instanceId})")
                                appendLine("- Nivel: **${preview.cost.currentLevel} -> ${preview.cost.nextLevel}**")
                                appendLine("- Custo: **${preview.cost.konosCost} konos**")
                                appendLine("- Copias: **${preview.cost.copiesRequired}** (voce tem ${preview.availableCopies})")
                                appendLine()
                                append("Clique no botao para confirmar.")
                            }

                            addComponent(ActionRowBuilder().apply {
                                interactionButton(ButtonStyle.Success, buttonId) {
                                    label = "Confirmar upgrade"
                                }
                            })
                        }

                        val click = event.kord.awaitButtonInteraction(
                            customId = buttonId,
                            allowedUserId = userId.toLong()
                        )

                        if (click == null) {
                            event.message.reply {
                                content = "⌛ Upgrade cancelado por tempo esgotado."
                            }
                            return
                        }

                        click.interaction.respondEphemeral {
                            content = "🔧 Processando upgrade..."
                        }

                        when (val upgradeResult = upgradeCharacterHandler.executeActiveCharacter(userId.toLong())) {
                            is UpgradeCharacterHandler.Result.Success -> {
                                event.message.reply {
                                    content = "✅ **${upgradeResult.characterName}** upou para **Lv.${upgradeResult.newLevel}**! " +
                                        "(gasto: ${upgradeResult.konosSpent} konos, ${upgradeResult.copiesSpent} copias)"
                                }
                            }

                            is UpgradeCharacterHandler.Result.UserNotFound -> {
                                event.message.reply { content = "Usuario nao encontrado." }
                            }

                            is UpgradeCharacterHandler.Result.NoActiveCharacter -> {
                                event.message.reply { content = "Nenhum personagem ativo selecionado." }
                            }

                            is UpgradeCharacterHandler.Result.CharacterNotFound -> {
                                event.message.reply { content = "Personagem ativo nao encontrado." }
                            }

                            is UpgradeCharacterHandler.Result.InvalidCardType -> {
                                event.message.reply { content = "A carta ativa nao e um personagem valido para upgrade." }
                            }

                            is UpgradeCharacterHandler.Result.MaxLevelReached -> {
                                event.message.reply {
                                    content = "Seu personagem ja esta no nivel maximo (${upgradeResult.currentLevel}/${upgradeResult.levelCap})."
                                }
                            }

                            is UpgradeCharacterHandler.Result.NotEnoughKonos -> {
                                event.message.reply {
                                    content = "Konos insuficientes: precisa de ${upgradeResult.required}, voce tem ${upgradeResult.current}."
                                }
                            }

                            is UpgradeCharacterHandler.Result.NotEnoughCopies -> {
                                event.message.reply {
                                    content = "Copias insuficientes: precisa de ${upgradeResult.required}, voce tem ${upgradeResult.current}."
                                }
                            }

                            is UpgradeCharacterHandler.Result.PersistFailed -> {
                                event.message.reply {
                                    content = "Erro ao persistir upgrade. Tente novamente."
                                }
                            }
                        }
                    }

                    is UpgradeCharacterHandler.PreviewResult.UserNotFound -> {
                        event.message.reply { content = "Usuario nao encontrado. Use `register`." }
                    }

                    is UpgradeCharacterHandler.PreviewResult.NoActiveCharacter -> {
                        event.message.reply { content = "Nenhum personagem ativo selecionado. Use `char set <id>`." }
                    }

                    is UpgradeCharacterHandler.PreviewResult.CharacterNotFound -> {
                        event.message.reply { content = "Personagem ativo nao encontrado." }
                    }

                    is UpgradeCharacterHandler.PreviewResult.InvalidCardType -> {
                        event.message.reply { content = "A carta ativa nao e um personagem." }
                    }

                    is UpgradeCharacterHandler.PreviewResult.MaxLevelReached -> {
                        event.message.reply {
                            content = "Seu personagem ja esta no nivel maximo (${preview.currentLevel}/${preview.levelCap})."
                        }
                    }

                    is UpgradeCharacterHandler.PreviewResult.NotEnoughKonos -> {
                        event.message.reply {
                            content = "Konos insuficientes: precisa de ${preview.required}, voce tem ${preview.current}."
                        }
                    }

                    is UpgradeCharacterHandler.PreviewResult.NotEnoughCopies -> {
                        event.message.reply {
                            content = "Copias insuficientes: precisa de ${preview.required}, voce tem ${preview.current}."
                        }
                    }
                }
            }
        }
    }

    private suspend fun buildUpgradeHint(discordId: Long): String? {
        return when (val preview = upgradeCharacterHandler.previewActiveCharacter(discordId)) {
            is UpgradeCharacterHandler.PreviewResult.Ready -> {
                "🆙 **Upgrade disponivel!** Use `char upgrade` (custo: ${preview.cost.konosCost} konos, ${preview.cost.copiesRequired} copias)."
            }

            else -> null
        }
    }
}
