package me.igorunderplayer.kono.commands.text.`fun`

import dev.kord.core.behavior.reply
import dev.kord.core.event.message.MessageCreateEvent
import io.ktor.client.request.forms.*
import io.ktor.utils.io.jvm.javaio.*
import me.igorunderplayer.kono.commands.BaseCommand
import me.igorunderplayer.kono.commands.CommandCategory

class HCommand: BaseCommand(
    name = "h",
    description = "h",
    category = CommandCategory.Misc
) {

    private val hResponses = listOf(
        "h/h.mov",
        "h/h2.mov",
        "h/h.mp4",
        "h/h2.mp4"
    )

    override suspend fun run(event: MessageCreateEvent, args: Array<String>) {
        val path = hResponses.random()
        val stream = this::class.java.getResourceAsStream("/$path")

        if (stream == null) {
            event.message.reply { content = "erro ao carregar video 💀 ($path)" }
            return
        }

        val fileName = path.substringAfterLast("/")
        event.message.reply {
            content = "h"
            addFile(fileName, ChannelProvider {
                stream.toByteReadChannel()
            })
        }
    }
}
