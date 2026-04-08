package me.igorunderplayer.kono.events

import dev.kord.core.Kord
import dev.kord.core.event.gateway.ReadyEvent
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.core.on
import me.igorunderplayer.kono.events.handlers.ReadyHandler

class EventManager(
    private val kord: Kord,
    private val readyHandler: ReadyHandler,
    private val messageCreateHandler: MessageCreateHandler,
    private val chatInputCommandInteractionCreateHandler: ChatInputCommandInteractionCreateHandler
) {

    fun start() {
        kord.on<ReadyEvent> {
            readyHandler.handle(this)
        }

        kord.on<MessageCreateEvent> {
            messageCreateHandler.handle(this)
        }

        kord.on<ChatInputCommandInteractionCreateEvent> {
            chatInputCommandInteractionCreateHandler.handle(this)
        }
    }
}
