package me.igorunderplayer.kono.events.handlers

import dev.kord.common.entity.PresenceStatus
import dev.kord.core.Kord
import dev.kord.core.event.gateway.ReadyEvent
import kotlinx.coroutines.delay
import me.igorunderplayer.kono.commands.CommandManager
import me.igorunderplayer.kono.services.EmojiService
import kotlin.time.Duration.Companion.seconds

class ReadyHandler(
    private val kord: Kord,
    private val commandManager: CommandManager,
    private val emojiService: EmojiService
) {

    suspend fun handle(event: ReadyEvent) {
        emojiService.loadEmojis()
        commandManager.registerCommands()
        println("Ready as ${event.kord.getSelf().username}")
        startPresenceLoop()
    }

    private suspend fun startPresenceLoop() {
        val presences = listOf<suspend () -> Unit>(
            {
                kord.editPresence {
                    status = PresenceStatus.Online
                    listening("seus / comandos")
                }
            },
            {
                kord.editPresence {
                    status = PresenceStatus.Idle
                    playing("Kono Bot")
                }
            },
            {
                kord.editPresence {
                    status = PresenceStatus.DoNotDisturb
                    watching("os servidores")
                }
            }
        )

        var index = 0

        while (true) {
            presences[index]()
            index = (index + 1) % presences.size
            delay(30.seconds)
        }
    }
}
