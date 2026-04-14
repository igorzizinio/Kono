package me.igorunderplayer.kono.commands.text.testing

import dev.kord.core.behavior.channel.createEmbed
import dev.kord.core.behavior.reply
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.rest.builder.message.embed
import me.igorunderplayer.kono.commands.BaseCommand
import me.igorunderplayer.kono.commands.CommandCategory
import me.igorunderplayer.kono.commands.CommandManager
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class Help: KoinComponent, BaseCommand(
    "help",
    "ajuda um necessitado",
    category = CommandCategory.Util,
    aliases = listOf("ajuda", "mimajude")
) {

    private val commandManager: CommandManager by inject()

    override suspend fun run(event: MessageCreateEvent, args: Array<String>) {

        val cmd = commandManager.searchCommand(args.firstOrNull() ?: "")
            ?: return displayAllCommands(event)

        val aliasesString = if (cmd.aliases.isEmpty())
            "⚠️ Nenhum alias registrado"
        else
            cmd.aliases.joinToString(", ")

        event.message.reply {
            embed {
                title = cmd.name.uppercase()
                description = cmd.description

                field {
                    name = "Categoria"
                    value = cmd.category.name
                }
                field {
                    name = "Aliases (apelidos)"
                    value = aliasesString
                }
            }
        }
    }

    private suspend fun displayAllCommands(event: MessageCreateEvent) {
        val grouped = commandManager.commandList
            .groupBy { it.category }

        fun format(category: CommandCategory) =
            grouped[category]
                ?.joinToString("\n")
                    { "▸ ${it.name} - `${it.description}`" }
                ?: "Nenhum comando"

        event.message.channel.createEmbed {
            field {
                name = "\uD83E\uDDD0 Utilidade"
                value = format(CommandCategory.Util)
            }
            field {
                name = "\uD83E\uDD73 Miscelânea"
                value = format(CommandCategory.Misc)
            }
            field {
                name = "\uD83D\uDEE0 Gerenciamento"
                value = format(CommandCategory.Management)
            }
            field {
                name = "\uD83C\uDFAE Jogos"
                value = format(CommandCategory.Game)
            }
            field {
                name = "\uD83C\uDF08 League of Legends"
                value = format(CommandCategory.LoL)
            }
            field {
                name = "\uD83E\uDD14 Outros"
                value = format(CommandCategory.Other)
            }
        }
    }
}
