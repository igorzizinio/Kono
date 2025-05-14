package me.igorunderplayer.kono.commands.text.`fun`

import dev.kord.common.Color
import dev.kord.core.behavior.reply
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.rest.builder.message.embed
import kotlinx.coroutines.flow.toList
import me.igorunderplayer.kono.commands.BaseCommand
import me.igorunderplayer.kono.utils.getMentionedUser

class TinderCommand: BaseCommand(
    name = "tinder",
    description = "dê match com alguem rs"
) {
    override suspend fun run(event: MessageCreateEvent, args: Array<String>) {
        val guild = event.getGuildOrNull()

        if (guild == null) {
            event.message.reply { content = "vc só pode usar isso num servidor" }
            return
        }

        val user = getMentionedUser(event.message, args) ?: guild.members.toList().random()

        val chance = (0..100).random()

        event.message.reply {
            embed {
                title = "\uD83D\uDC96 O amor está no ar!"
                description = "Você deu match com ${user.mention}\n" +
                        "\nos pombinhos tem ${chance}% de chance de dar certo \uD83E\uDD2D"
                color = Color(255, 41, 255)
            }
        }
    }
}