package me.igorunderplayer.kono.events

import dev.kord.core.behavior.reply
import dev.kord.core.event.message.MessageCreateEvent
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import me.igorunderplayer.kono.ai.ConversationBuilder
import me.igorunderplayer.kono.ai.KONO_SYSTEM_PROMPT
import me.igorunderplayer.kono.commands.CommandManager
import me.igorunderplayer.kono.commands.CommandResult
import me.igorunderplayer.kono.data.dto.ChatMessage
import me.igorunderplayer.kono.services.ai.AIService
import kotlin.random.Random

class MessageCreateHandler(
    private val commandManager: CommandManager,
    private val ai: AIService
) {

    suspend fun handle(event: MessageCreateEvent) {
        if (event.message.author?.isBot == true) return

        when (commandManager.handleCommand(event)) {
            is CommandResult.Success -> {}
            is CommandResult.Failure -> {}
            is CommandResult.CommandNotFound -> {
                if (event.message.mentionedUserIds.contains(event.kord.selfId)) {
                    handleConversation(event)
                    return
                }

                val content = event.message.content.lowercase()

                val lastMessages = event.message.channel.messages
                    .take(5)
                    .toList()

                val konoRecentlyTalked = lastMessages.any {
                    it.author?.id == event.kord.selfId
                }

                val chance = when {
                    "kono" in content -> 0.25
                    konoRecentlyTalked -> 0.08
                    else -> 0.01
                }

                if (Random.nextDouble() < chance) {
                    handleConversation(event)
                }
            }
        }
    }

    suspend fun handleConversation(
        event: MessageCreateEvent
    ) {

        event.message.channel.type()
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
