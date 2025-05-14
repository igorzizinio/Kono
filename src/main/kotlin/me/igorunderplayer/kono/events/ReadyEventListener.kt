package me.igorunderplayer.kono.events

import dev.kord.common.entity.PresenceStatus
import dev.kord.core.entity.Emoji
import dev.kord.core.event.gateway.ReadyEvent
import kotlinx.coroutines.flow.filter
import me.igorunderplayer.kono.Kono

suspend fun onReady(event: ReadyEvent) {
    val purple = "[0;35m"
    val reset = "\u001B[0m"

    val EMOJI_GUILDS = listOf("931300984242724864", "978482978143498280", "1124789207982931998", "1124821720784699442")

    val konoEmojis = mutableListOf<Emoji>()

    Kono.kord.guilds.filter {
        EMOJI_GUILDS.contains(it.id.toString())
    }.collect { guild ->
        guild.emojis.collect {
            konoEmojis.add(it)
        }
    }

    Kono.emojis = konoEmojis

    Kono.commands.registerCommands()


    println("$purple Ready as ${event.kord.getSelf().username} (${event.kord.selfId}) $reset")

    event.kord.editPresence {
        status = PresenceStatus.Idle
        listening("seus / comandos")
    }
}