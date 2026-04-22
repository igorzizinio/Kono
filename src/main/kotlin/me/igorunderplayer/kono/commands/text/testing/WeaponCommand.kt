package me.igorunderplayer.kono.commands.text.testing

import dev.kord.common.entity.ButtonStyle
import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.behavior.reply
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.rest.builder.component.ActionRowBuilder
import dev.kord.rest.builder.component.option
import me.igorunderplayer.kono.commands.BaseCommand
import me.igorunderplayer.kono.commands.CommandCategory
import me.igorunderplayer.kono.domain.card.Rarity
import me.igorunderplayer.kono.domain.card.toDisplayName
import me.igorunderplayer.kono.domain.team.DismantleEquipmentHandler
import me.igorunderplayer.kono.domain.team.UpgradeEquipmentHandler
import me.igorunderplayer.kono.utils.interaction.awaitButtonInteraction
import me.igorunderplayer.kono.utils.interaction.awaitStringSelectInteraction

class WeaponCommand(
    private val upgradeEquipmentHandler: UpgradeEquipmentHandler,
    private val dismantleEquipmentHandler: DismantleEquipmentHandler
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
                content = "Use: `weapon upgrade <instance_id>` ou `weapon dismantle <instance_id>`."
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
                                appendLine("- Smithing Stones: **${preview.cost.smithingStonesRequired}** (voce tem ${preview.currentSmithingStones})")
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
                                        "(gasto: ${result.konosSpent} konos, ${result.smithingStonesSpent} smithing stones)"
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

                            is UpgradeEquipmentHandler.Result.NotEnoughSmithingStones -> {
                                event.message.reply {
                                    content = "Smithing Stones insuficientes: precisa de ${result.required}, voce tem ${result.current}."
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

                    is UpgradeEquipmentHandler.PreviewResult.NotEnoughSmithingStones -> {
                        event.message.reply {
                            content = "Smithing Stones insuficientes: precisa de ${preview.required}, voce tem ${preview.current}."
                        }
                    }
                }
            }

            "dismantle", "desmontar" -> {
                val input = args.getOrNull(0)
                if (input.isNullOrBlank()) {
                    val selectId = "weapon-dismantle-select-${event.message.id}-${System.currentTimeMillis()}"

                    event.message.reply {
                        content = "Selecione ate qual raridade voce quer desmontar (inclui a raridade escolhida e abaixo)."

                        addComponent(ActionRowBuilder().apply {
                            stringSelect(selectId) {
                                option("Comum", "COMMON")
                                option("Rara", "RARE")
                                option("Epica", "EPIC")
                                option("Lendaria", "LEGENDARY")
                                option("Mitica", "MYTHIC")
                            }
                        })
                    }

                    val selection = event.kord.awaitStringSelectInteraction(
                        customId = selectId,
                        allowedUserId = userId
                    )

                    if (selection == null) {
                        event.message.reply {
                            content = "⌛ Seleção de desmontagem cancelada por tempo esgotado."
                        }
                        return
                    }

                    selection.interaction.respondEphemeral {
                        content = "♻️ Selecao recebida. Processando preview..."
                    }

                    val selectedValue = selection.interaction.values.firstOrNull()
                    val selectedRarity = runCatching { selectedValue?.let { Rarity.valueOf(it) } }.getOrNull()

                    if (selectedRarity == null) {
                        event.message.reply {
                            content = "Não foi possivel interpretar a raridade selecionada. Tente novamente."
                        }
                        return
                    }

                    handleBulkDismantle(event, userId, selectedRarity)
                    return
                }

                val instanceId = input.toIntOrNull()
                val maxRarity = parseRarityInput(input)

                if (instanceId == null && maxRarity == null) {
                    event.message.reply {
                        content = "Entrada invalida. Use um `instance_id` ou raridade (`common`, `rare`, `epic`, `legendary`, `mythic`)."
                    }
                    return
                }

                if (instanceId != null) {
                    if (instanceId <= 0) {
                        event.message.reply {
                            content = "Instance ID invalido. Use: `weapon dismantle <instance_id>`."
                        }
                        return
                    }

                    val preview = dismantleEquipmentHandler.preview(userId, instanceId)
                    when (preview) {
                        is DismantleEquipmentHandler.PreviewResult.Ready -> {
                            val buttonId = "weapon-dismantle-${event.message.id}-${System.currentTimeMillis()}"

                            event.message.reply {
                                content = buildString {
                                    appendLine("♻️ **Confirmar desmontagem** de **${preview.equipmentName}** (#${preview.instanceId})")
                                    appendLine("- Recompensa: **${preview.reward.smithingStones} Smithing Stones** (${preview.reward.rarity.toDisplayName()})")
                                    appendLine("- Sua banca atual: **${preview.currentSmithingStones} Smithing Stones**")
                                    appendLine()
                                    append("Clique no botao para confirmar.")
                                }

                                addComponent(ActionRowBuilder().apply {
                                    interactionButton(ButtonStyle.Danger, buttonId) {
                                        label = "Confirmar desmontagem"
                                    }
                                })
                            }

                            val click = event.kord.awaitButtonInteraction(
                                customId = buttonId,
                                allowedUserId = userId
                            )

                            if (click == null) {
                                event.message.reply {
                                    content = "⌛ Desmontagem cancelada por tempo esgotado."
                                }
                                return
                            }

                            click.interaction.respondEphemeral {
                                content = "♻️ Processando desmontagem..."
                            }

                            when (val result = dismantleEquipmentHandler.execute(userId, instanceId)) {
                                is DismantleEquipmentHandler.Result.Success -> {
                                    event.message.reply {
                                        content = "✅ **${result.equipmentName}** foi desmontado! " +
                                            "(+${result.reward.smithingStones} smithing stones, saldo: ${result.newSmithingStonesBalance})"
                                    }
                                }

                                is DismantleEquipmentHandler.Result.UserNotFound -> {
                                    event.message.reply { content = "Usuario nao encontrado. Use `register`." }
                                }

                                is DismantleEquipmentHandler.Result.EquipmentNotFound -> {
                                    event.message.reply { content = "Equipamento nao encontrado na sua conta." }
                                }

                                is DismantleEquipmentHandler.Result.InvalidCardType -> {
                                    event.message.reply { content = "A carta informada nao e um equipamento valido." }
                                }

                                is DismantleEquipmentHandler.Result.EquipmentEquipped -> {
                                    event.message.reply {
                                        content = "Nao e possivel desmontar um equipamento equipado. Desequipe primeiro."
                                    }
                                }

                                is DismantleEquipmentHandler.Result.PersistFailed -> {
                                    event.message.reply {
                                        content = "Erro ao persistir desmontagem. Tente novamente."
                                    }
                                }
                            }
                        }

                        is DismantleEquipmentHandler.PreviewResult.UserNotFound -> {
                            event.message.reply { content = "Usuario nao encontrado. Use `register`." }
                        }

                        is DismantleEquipmentHandler.PreviewResult.EquipmentNotFound -> {
                            event.message.reply { content = "Equipamento nao encontrado na sua conta." }
                        }

                        is DismantleEquipmentHandler.PreviewResult.InvalidCardType -> {
                            event.message.reply { content = "A carta informada nao e um equipamento." }
                        }

                        is DismantleEquipmentHandler.PreviewResult.EquipmentEquipped -> {
                            event.message.reply { content = "Nao e possivel desmontar um equipamento equipado. Desequipe primeiro." }
                        }
                    }
                    return
                }

                val bulkRarity = maxRarity ?: return
                handleBulkDismantle(event, userId, bulkRarity)
            }

            else -> {
                event.message.reply {
                    content = "Opcao invalida. Use: `weapon upgrade <instance_id>` ou `weapon dismantle <instance_id>`."
                }
            }
        }
    }

    private fun parseRarityInput(input: String): Rarity? {
        return when (input.lowercase()) {
            "common", "comum" -> Rarity.COMMON
            "rare", "rara" -> Rarity.RARE
            "epic", "epica", "épica" -> Rarity.EPIC
            "legendary", "lendaria", "lendária" -> Rarity.LEGENDARY
            "mythic", "mitica", "mitíca" -> Rarity.MYTHIC
            else -> null
        }
    }

    private suspend fun handleBulkDismantle(event: MessageCreateEvent, userId: Long, bulkRarity: Rarity) {
        val bulkPreview = dismantleEquipmentHandler.previewByRarity(userId, bulkRarity)
        when (bulkPreview) {
            is DismantleEquipmentHandler.BulkPreviewResult.Ready -> {
                val buttonId = "weapon-dismantle-bulk-${event.message.id}-${System.currentTimeMillis()}"

                event.message.reply {
                    content = buildString {
                        appendLine("♻️ **Confirmar desmontagem em massa** (ate ${bulkPreview.maxRarity.toDisplayName()})")
                        appendLine("- Equipamentos afetados: **${bulkPreview.dismantleCount}**")
                        appendLine("- Recompensa total: **${bulkPreview.totalSmithingStonesReward} Smithing Stones**")
                        appendLine("- Sua banca atual: **${bulkPreview.currentSmithingStones} Smithing Stones**")
                        appendLine()
                        append("Clique no botao para confirmar.")
                    }

                    addComponent(ActionRowBuilder().apply {
                        interactionButton(ButtonStyle.Danger, buttonId) {
                            label = "Confirmar desmontagem em massa"
                        }
                    })
                }

                val click = event.kord.awaitButtonInteraction(
                    customId = buttonId,
                    allowedUserId = userId
                )

                if (click == null) {
                    event.message.reply {
                        content = "⌛ Desmontagem em massa cancelada por tempo esgotado."
                    }
                    return
                }

                click.interaction.respondEphemeral {
                    content = "♻️ Processando desmontagem em massa..."
                }

                when (val result = dismantleEquipmentHandler.executeByRarity(userId, bulkRarity)) {
                    is DismantleEquipmentHandler.BulkResult.Success -> {
                        event.message.reply {
                            content = "✅ Desmontagem concluida: **${result.dismantledCount}** equipamento(s) ate " +
                                "**${result.maxRarity.toDisplayName()}**. " +
                                "(+${result.totalSmithingStonesReward} smithing stones, saldo: ${result.newSmithingStonesBalance})"
                        }
                    }

                    is DismantleEquipmentHandler.BulkResult.UserNotFound -> {
                        event.message.reply { content = "Usuario nao encontrado. Use `register`." }
                    }

                    is DismantleEquipmentHandler.BulkResult.NoEligibleEquipment -> {
                        event.message.reply {
                            content = "Nenhum equipamento elegivel para desmontar ate ${result.maxRarity.toDisplayName()}."
                        }
                    }

                    is DismantleEquipmentHandler.BulkResult.PersistFailed -> {
                        event.message.reply {
                            content = "Erro ao persistir desmontagem em massa. Tente novamente."
                        }
                    }
                }
            }

            is DismantleEquipmentHandler.BulkPreviewResult.UserNotFound -> {
                event.message.reply { content = "Usuario nao encontrado. Use `register`." }
            }

            is DismantleEquipmentHandler.BulkPreviewResult.NoEligibleEquipment -> {
                event.message.reply {
                    content = "Nenhum equipamento elegivel para desmontar ate ${bulkPreview.maxRarity.toDisplayName()}."
                }
            }
        }
    }
}

