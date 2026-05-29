package me.igorunderplayer.kono.commands.slash.game.subcommand

import dev.kord.common.entity.ApplicationCommandOption
import dev.kord.common.entity.ButtonStyle
import dev.kord.core.behavior.interaction.response.edit
import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import dev.kord.rest.builder.component.ActionRowBuilder
import me.igorunderplayer.kono.commands.KonoSlashSubCommand
import me.igorunderplayer.kono.domain.team.UpgradeCharacterHandler
import me.igorunderplayer.kono.utils.interaction.awaitButtonInteraction

class PersonagemUpgrade(
    private val upgradeCharacterHandler: UpgradeCharacterHandler
) : KonoSlashSubCommand {
    override val name = "upgrade"
    override val description = "Sobe o nível do seu personagem ativo (consome cópias e KonoCoins)"
    override val options: List<ApplicationCommandOption> = listOf()

    override suspend fun run(event: ChatInputCommandInteractionCreateEvent) {
        val deferred = event.interaction.deferEphemeralResponse()
        val discordId = event.interaction.user.id.value.toLong()

        when (val preview = upgradeCharacterHandler.previewActiveCharacter(discordId)) {
            is UpgradeCharacterHandler.PreviewResult.Ready -> {
                val buttonId = "char-upgrade-${discordId}-${System.currentTimeMillis()}"

                val response = deferred.respond {
                    content = buildString {
                        appendLine("⚠️ **Confirmar upgrade** de **${preview.characterName}** (#${preview.instanceId})")
                        appendLine("• Nível: **${preview.cost.currentLevel} → ${preview.cost.nextLevel}**")
                        appendLine("• Custo: **${preview.cost.konosCost} ₭**")
                        appendLine("• Cópias necessárias: **${preview.cost.copiesRequired}** (você tem **${preview.availableCopies}**)")
                        appendLine()
                        append("Clique no botão abaixo para confirmar.")
                    }
                    addComponent(ActionRowBuilder().apply {
                        interactionButton(ButtonStyle.Success, buttonId) {
                            label = "✅ Confirmar upgrade"
                        }
                    })
                }

                val click = event.kord.awaitButtonInteraction(buttonId, discordId)
                if (click == null) {
                    response.edit {
                        components = mutableListOf(ActionRowBuilder().apply {
                            interactionButton(ButtonStyle.Success, buttonId) {
                                label = "✅ Confirmar upgrade"
                                disabled = true
                            }
                        })
                    }
                    return
                }

                val update = click.interaction.deferEphemeralMessageUpdate()

                when (val upgradeResult = upgradeCharacterHandler.executeActiveCharacter(discordId)) {
                    is UpgradeCharacterHandler.Result.Success ->
                        update.edit {
                            content =
                                "✅ **${upgradeResult.characterName}** chegou ao **Lv.${upgradeResult.newLevel}**! " +
                                        "(gasto: ${upgradeResult.konosSpent} ₭, ${upgradeResult.copiesSpent} cópia(s))"
                            components = mutableListOf()
                        }

                    is UpgradeCharacterHandler.Result.UserNotFound ->
                        update.edit { content = "❌ Usuário não encontrado."; components = mutableListOf() }

                    is UpgradeCharacterHandler.Result.NoActiveCharacter ->
                        update.edit { content = "❌ Nenhum personagem ativo."; components = mutableListOf() }

                    is UpgradeCharacterHandler.Result.CharacterNotFound ->
                        update.edit { content = "❌ Personagem ativo não encontrado."; components = mutableListOf() }

                    is UpgradeCharacterHandler.Result.InvalidCardType ->
                        update.edit {
                            content = "❌ A carta ativa não é um personagem válido."; components = mutableListOf()
                        }

                    is UpgradeCharacterHandler.Result.MaxLevelReached ->
                        update.edit {
                            content =
                                "⛔ Seu personagem já está no nível máximo (${upgradeResult.currentLevel}/${upgradeResult.levelCap})."
                            components = mutableListOf()
                        }

                    is UpgradeCharacterHandler.Result.NotEnoughKonos ->
                        update.edit {
                            content =
                                "❌ KonoCoins insuficientes: precisa de **${upgradeResult.required} ₭**, você tem **${upgradeResult.current} ₭**."
                            components = mutableListOf()
                        }

                    is UpgradeCharacterHandler.Result.NotEnoughCopies ->
                        update.edit {
                            content =
                                "❌ Cópias insuficientes: precisa de **${upgradeResult.required}**, você tem **${upgradeResult.current}**."
                            components = mutableListOf()
                        }

                    is UpgradeCharacterHandler.Result.PersistFailed ->
                        update.edit {
                            content = "⚠️ Erro ao salvar o upgrade. Tente novamente."; components = mutableListOf()
                        }
                }
            }

            is UpgradeCharacterHandler.PreviewResult.UserNotFound ->
                deferred.respond { content = "❌ Você não está registrado. Use `/register`." }

            is UpgradeCharacterHandler.PreviewResult.NoActiveCharacter ->
                deferred.respond { content = "❌ Nenhum personagem ativo. Use `/personagem definir`." }

            is UpgradeCharacterHandler.PreviewResult.CharacterNotFound ->
                deferred.respond { content = "❌ Personagem ativo não encontrado." }

            is UpgradeCharacterHandler.PreviewResult.InvalidCardType ->
                deferred.respond { content = "❌ A carta ativa não é um personagem." }

            is UpgradeCharacterHandler.PreviewResult.MaxLevelReached ->
                deferred.respond {
                    content = "⛔ Seu personagem já está no nível máximo (${preview.currentLevel}/${preview.levelCap})."
                }

            is UpgradeCharacterHandler.PreviewResult.NotEnoughKonos ->
                deferred.respond {
                    content =
                        "❌ KonoCoins insuficientes: precisa de **${preview.required} ₭**, você tem **${preview.current} ₭**."
                }

            is UpgradeCharacterHandler.PreviewResult.NotEnoughCopies ->
                deferred.respond {
                    content =
                        "❌ Cópias insuficientes: precisa de **${preview.required}**, você tem **${preview.current}**."
                }
        }
    }
}
