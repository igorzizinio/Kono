package me.igorunderplayer.kono.events

import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import me.igorunderplayer.kono.commands.CommandManager

class ChatInputCommandInteractionCreateHandler(
    private val commandManager: CommandManager
) {

    suspend fun handle(event: ChatInputCommandInteractionCreateEvent) {
        commandManager.handleChatInputCommand(event)
    }
}
