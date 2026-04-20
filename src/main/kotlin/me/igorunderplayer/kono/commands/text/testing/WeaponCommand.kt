package me.igorunderplayer.kono.commands.text.testing

import dev.kord.common.entity.ButtonStyle
import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.behavior.reply
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.rest.builder.component.ActionRowBuilder
import me.igorunderplayer.kono.commands.BaseCommand
import me.igorunderplayer.kono.commands.CommandCategory
import me.igorunderplayer.kono.domain.team.UpgradeEquipmentHandler
import me.igorunderplayer.kono.utils.interaction.awaitButtonInteraction

class WeaponCommand(
    private val upgradeEquipmentHandler: UpgradeEquipmentHandler
) : BaseCommand(
    name = "weapon",
    description = "comandos de upgrade para armas/equipamentos",
    aliases = listOf("arma", "wp"),
    category = CommandCategory.Game
) {

    override suspend fun run(event: MessageCreateEvent, args: Array<String>) {
        val userId = event.message.author?.id?.value?.toLong() ?: return

        val args = args.toMutableList()
        if (args.isEmpty()) {
            event.message.reply {
                content = "Use: `weapon upgrade <instance_id>`"
            }
            return
        }

        when (args.removeFirst().lowercase()) {
            "upgrade", "up" -> {
                val instanceId = args.getOrNull(0)?.toIntOrNull()
                if (instanceId == null || instanceId <= 0) {
                    event.message.reply {
                        content = "Instance ID invalido. Use: `weapon upgrade <instance_id>`."
                    }
                    return
                }

                val preview = upgradeEquipmentHandler.preview(userId, instanceId)
                when (preview) {
                    is UpgradeEquipmentHandler.PreviewResult.Ready -> {
                        val buttonId = "weapon-upgrade-${event.message.id}-${System.currentTimeMillis()}"

                        event.message.reply {
                            content = buildString {
                                appendLine("⚠️ **Confirmar upgrade** de **${preview.equipmentName}** (#${preview.instanceId})")
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
                            allowedUserId = userId
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

                        when (val result = upgradeEquipmentHandler.execute(userId, instanceId)) {
                            is UpgradeEquipmentHandler.Result.Success -> {
                                event.message.reply {
                                    content = "✅ **${result.equipmentName}** upou para **Lv.${result.newLevel}**! " +
                                        "(gasto: ${result.konosSpent} konos, ${result.copiesSpent} copias)"
                                }
                            }

                            is UpgradeEquipmentHandler.Result.UserNotFound -> {
                                event.message.reply { content = "Usuario nao encontrado. Use `register`." }
                            }

                            is UpgradeEquipmentHandler.Result.EquipmentNotFound -> {
                                event.message.reply { content = "Equipamento nao encontrado na sua conta." }
                            }

                            is UpgradeEquipmentHandler.Result.InvalidCardType -> {
                                event.message.reply { content = "A carta informada nao e um equipamento valido." }
                            }

                            is UpgradeEquipmentHandler.Result.MaxLevelReached -> {
                                event.message.reply {
                                    content = "Seu equipamento ja esta no nivel maximo (${result.currentLevel}/${result.levelCap})."
                                }
                            }

                            is UpgradeEquipmentHandler.Result.NotEnoughKonos -> {
                                event.message.reply {
                                    content = "Konos insuficientes: precisa de ${result.required}, voce tem ${result.current}."
                                }
                            }

                            is UpgradeEquipmentHandler.Result.NotEnoughCopies -> {
                                event.message.reply {
                                    content = "Copias insuficientes: precisa de ${result.required}, voce tem ${result.current}."
                                }
                            }

                            is UpgradeEquipmentHandler.Result.PersistFailed -> {
                                event.message.reply {
                                    content = "Erro ao persistir upgrade. Tente novamente."
                                }
                            }
                        }
                    }

                    is UpgradeEquipmentHandler.PreviewResult.UserNotFound -> {
                        event.message.reply { content = "Usuario nao encontrado. Use `register`." }
                    }

                    is UpgradeEquipmentHandler.PreviewResult.EquipmentNotFound -> {
                        event.message.reply { content = "Equipamento nao encontrado na sua conta." }
                    }

                    is UpgradeEquipmentHandler.PreviewResult.InvalidCardType -> {
                        event.message.reply { content = "A carta informada nao e um equipamento." }
                    }

                    is UpgradeEquipmentHandler.PreviewResult.MaxLevelReached -> {
                        event.message.reply {
                            content = "Seu equipamento ja esta no nivel maximo (${preview.currentLevel}/${preview.levelCap})."
                        }
                    }

                    is UpgradeEquipmentHandler.PreviewResult.NotEnoughKonos -> {
                        event.message.reply {
                            content = "Konos insuficientes: precisa de ${preview.required}, voce tem ${preview.current}."
                        }
                    }

                    is UpgradeEquipmentHandler.PreviewResult.NotEnoughCopies -> {
                        event.message.reply {
                            content = "Copias insuficientes: precisa de ${preview.required}, voce tem ${preview.current}."
                        }
                    }
                }
            }

            else -> {
                event.message.reply {
                    content = "Opcao invalida. Use: `weapon upgrade <instance_id>`."
                }
            }
        }
    }
}

