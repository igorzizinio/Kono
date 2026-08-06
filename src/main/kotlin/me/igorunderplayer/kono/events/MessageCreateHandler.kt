package me.igorunderplayer.kono.events

import dev.kord.core.behavior.reply
import dev.kord.core.event.message.MessageCreateEvent
import me.igorunderplayer.kono.ai.ConversationBuilder
import me.igorunderplayer.kono.ai.KONO_SYSTEM_PROMPT
import me.igorunderplayer.kono.commands.CommandManager
import me.igorunderplayer.kono.data.dto.ChatMessage
import me.igorunderplayer.kono.services.ai.AIService

class MessageCreateHandler(
    private val commandManager: CommandManager,
    private val ai: AIService
) {

    suspend fun handle(event: MessageCreateEvent) {
        if (event.message.author?.isBot == true) return

        if (event.message.mentionedUserIds.contains(event.kord.selfId)) {
            handleConversation(event)
            return
        }

        commandManager.handleCommand(event)
    }

    suspend fun handleConversation(
        event: MessageCreateEvent
    ) {

        val messages = mutableListOf<ChatMessage>()

        messages += ChatMessage(
            role = "system",
            content = KONO_SYSTEM_PROMPT
        )

        messages += ConversationBuilder.build(event.message)

        val response = ai.generate(messages)

        event.message.reply {
            content = response
        }
    }
}
