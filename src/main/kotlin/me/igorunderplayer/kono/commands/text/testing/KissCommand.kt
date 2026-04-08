package me.igorunderplayer.kono.commands.text.testing

import dev.kord.common.Color
import dev.kord.core.behavior.reply
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.rest.builder.message.embed
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import me.igorunderplayer.kono.commands.BaseCommand
import me.igorunderplayer.kono.commands.CommandCategory
import me.igorunderplayer.kono.common.Colors
import me.igorunderplayer.kono.data.dto.WaifuPicsMediaDTO
import me.igorunderplayer.kono.utils.getMentionedUser


class KissCommand: BaseCommand(
    name = "kiss",
    description = "beija alguém",
    category = CommandCategory.Misc
) {

    private val client = HttpClient() {
        followRedirects = true
        install(ContentNegotiation) {
            json()
        }

    }
    override suspend fun run(event: MessageCreateEvent, args: Array<String>) {
        val user = getMentionedUser(event.message, args) ?: event.kord.getSelf()

        if (user.id == event.message.author?.id) {
            event.message.reply { content = "o amor próprio é lindo... mas não nesse sentido" }
            return
        }

        if (user.id == event.kord.getSelf().id) {
            event.message.reply { content = "**SAI FORA TARADO**" }
            return
        }

        val response = client.get("https://api.waifu.pics/sfw/kiss").body<WaifuPicsMediaDTO>()

        event.message.reply {
            embed {
                title = "meodeos ele beijou"
                description = "${event.message.author?.mention} beijou ${user.mention} \uD83D\uDE0D !!"
                color = Color(Colors.PINK)
                image = response.url
            }
        }
    }
}
