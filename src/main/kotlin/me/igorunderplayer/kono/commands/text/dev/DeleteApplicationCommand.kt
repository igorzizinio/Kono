package me.igorunderplayer.kono.commands.text.dev

import dev.kord.core.behavior.reply
import dev.kord.core.event.message.MessageCreateEvent
import kotlinx.coroutines.flow.firstOrNull
import me.igorunderplayer.kono.commands.BaseCommand
import me.igorunderplayer.kono.commands.CommandCategory

class DeleteApplicationCommand: BaseCommand(
    name = "deletecommand",
    description = "deleta um slash command",
    category = CommandCategory.Developer
) {
    override suspend fun run(event: MessageCreateEvent, args: Array<String>) {
        val commands = event.kord.getGlobalApplicationCommands()


        val queryCommand = args.firstOrNull()

        if (queryCommand == null) {
            event.message.reply {
                content = "no command argument"
            }
            return
        }


        val cmd = commands.firstOrNull {
            it.name == queryCommand
        }

        if (cmd == null) {
            event.message.reply { content = "command not found!" }
            return
        }

        cmd.delete()

        event.message.reply { content = "command ${cmd.name} deleted!" }
    }
}