package me.igorunderplayer.kono.commands.text.testing

import dev.kord.core.behavior.reply
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.rest.builder.message.embed
import me.igorunderplayer.kono.commands.BaseCommand
import me.igorunderplayer.kono.domain.card.prettyName
import me.igorunderplayer.kono.domain.card.toDisplayEmoji
import me.igorunderplayer.kono.domain.team.BuildUnitHandler
import me.igorunderplayer.kono.domain.team.SetActiveCharacterHandler

class CharacterCommand(
    private val setActiveCharacterHandler: SetActiveCharacterHandler,
    private val buildUnitHandler: BuildUnitHandler
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
                content = "Use: `character <info | set>"
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
                        val descriptionBuilder = buildString {
                            appendLine(result.unit.card.description)
                            appendLine()
                            appendLine("**Equipamentos**:")
                            result.unit.equipments.forEach {
                                appendLine("${it.rarity.toDisplayEmoji()} ${it.name}")
                            }
                            appendLine()
                            appendLine("**Status:**")
                            result.unit.stats.forEach { (stat, value) ->
                                appendLine("- **${stat.prettyName()}**: $value")
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
        }
    }
}
