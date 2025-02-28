package me.igorunderplayer.kono.commands.text.dev

import dev.kord.core.behavior.reply
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.rest.builder.message.embed
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import me.igorunderplayer.kono.commands.BaseCommand
import me.igorunderplayer.kono.commands.CommandCategory

class GuildsCommand: BaseCommand(
    name = "guilds",
    description = "mostra guilds q estou",
    category = CommandCategory.Developer
) {
    override suspend fun run(event: MessageCreateEvent, args: Array<String>) {
        event.message.reply {
            embed {
                title = "Guilds em que estou!"
                description = event.kord.guilds.map {
                    "• ${it.name}"
                }.toList().joinToString("\n")
            }
        }
    }
}