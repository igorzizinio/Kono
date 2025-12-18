package me.igorunderplayer.kono.events

import dev.kord.common.entity.PresenceStatus
import dev.kord.core.Kord
import dev.kord.core.entity.Emoji
import dev.kord.core.event.gateway.ReadyEvent
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.*
import me.igorunderplayer.kono.Kono
import kotlin.time.Duration.Companion.seconds

suspend fun onReady(event: ReadyEvent) {
    val purple = "[0;35m"
    val reset = "\u001B[0m"

    val emojiGuilds = listOf("931300984242724864", "978482978143498280", "1124789207982931998", "1124821720784699442")

    val konoEmojis = mutableListOf<Emoji>()

    Kono.kord.guilds.filter {
        emojiGuilds.contains(it.id.toString())
    }.collect { guild ->
        guild.emojis.collect {
            konoEmojis.add(it)
        }
    }

    Kono.emojis = konoEmojis

    Kono.commands.registerCommands()


    println("$purple Ready as ${event.kord.getSelf().username} (${event.kord.selfId}) $reset")


    startPresenceLoop(event.kord)
}


fun startPresenceLoop(kord: Kord) = CoroutineScope(Dispatchers.Default).launch {
    CoroutineScope(Dispatchers.Default).launch {
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

        while (isActive) {
            presences[index]()
            index = (index + 1) % presences.size
            delay(30.seconds)
        }
    }
}