package me.igorunderplayer.kono.events

import dev.kord.core.event.message.MessageCreateEvent
import me.igorunderplayer.kono.commands.CommandManager

class MessageCreateHandler(
    private val commandManager: CommandManager
) {
    suspend fun handle(event: MessageCreateEvent) {
        if (event.message.author?.isBot == true) return

        commandManager.handleCommand(event)
    }
}
