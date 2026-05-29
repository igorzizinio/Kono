package me.igorunderplayer.kono.commands.slash.game.subcommand

import dev.kord.common.entity.ApplicationCommandOption
import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import dev.kord.rest.builder.message.embed
import me.igorunderplayer.kono.commands.KonoSlashSubCommand
import me.igorunderplayer.kono.domain.card.*
import me.igorunderplayer.kono.domain.team.BuildUnitHandler
import me.igorunderplayer.kono.domain.team.UpgradeCharacterHandler

class PersonagemInfo(
    private val buildUnitHandler: BuildUnitHandler,
    private val upgradeCharacterHandler: UpgradeCharacterHandler
) : KonoSlashSubCommand {
    override val name = "info"
    override val description = "Exibe as informações e status do seu personagem ativo"
    override val options: List<ApplicationCommandOption> = listOf()

    private val statOrder = listOf(
        Stat.HP, Stat.ATK, Stat.INT, Stat.DEF,
        Stat.SPEED, Stat.CRIT_CHANCE, Stat.CRIT_DAMAGE, Stat.LIFESTEAL
    )

    override suspend fun run(event: ChatInputCommandInteractionCreateEvent) {
        val deferred = event.interaction.deferEphemeralResponse()
        val discordId = event.interaction.user.id.value.toLong()

        when (val result = buildUnitHandler.executeByDiscordId(discordId)) {
            is BuildUnitHandler.Result.Success -> {
                val unit = result.unit
                val upgradeHint = buildUpgradeHint(discordId)

                deferred.respond {
                    embed {
                        title = "${unit.card.rarity.toDisplayEmoji()} ${unit.card.name}"
                        color = unit.card.rarity.colorDefinition()
                        description = unit.card.description

                        field {
                            name = "📋 Info"
                            value = buildString {
                                appendLine("**Nível:** ${result.level}")
                                append("**ID:** `#${result.characterInstanceId}`")
                                if (upgradeHint != null) {
                                    appendLine()
                                    append(upgradeHint)
                                }
                            }
                            inline = true
                        }

                        field {
                            name = "🎒 Equipamentos"
                            value = if (result.equippedItems.isEmpty()) {
                                "*Nenhum equipamento*"
                            } else {
                                result.equippedItems.joinToString("\n") { item ->
                                    val slot = EquipmentSlot.fromIndex(item.slot)
                                    "${slot?.icon ?: "?"} **${item.name}** Lv.${item.level} `#${item.cardInstanceId}`"
                                }
                            }
                            inline = true
                        }

                        field {
                            name = "📊 Status"
                            value = buildString {
                                statOrder.forEach { stat ->
                                    val value = unit.stats[stat] ?: if (stat == Stat.INT) 0.0 else return@forEach
                                    appendLine("${stat.prettyName()}: **${prettyValue(stat, value)}**")
                                }
                            }.trimEnd()
                            inline = false
                        }

                        footer { text = unit.card.id }
                    }
                }
            }

            is BuildUnitHandler.Result.UserNotFound ->
                deferred.respond { content = "❌ Você não está registrado. Use `/register` para começar." }

            is BuildUnitHandler.Result.NoActiveCard ->
                deferred.respond {
                    content = "❌ Nenhum personagem ativo. Use `/personagem definir` para selecionar um."
                }

            is BuildUnitHandler.Result.CharacterNotFound ->
                deferred.respond { content = "❌ Personagem ativo (#${result.activeCharacterId}) não encontrado." }
        }
    }

    private suspend fun buildUpgradeHint(discordId: Long): String? {
        return when (val preview = upgradeCharacterHandler.previewActiveCharacter(discordId)) {
            is UpgradeCharacterHandler.PreviewResult.Ready ->
                "🆙 Upgrade disponível! Lv.${preview.cost.currentLevel} → Lv.${preview.cost.nextLevel} (${preview.cost.konosCost} ₭, ${preview.cost.copiesRequired} cópia(s))"

            else -> null
        }
    }
}
