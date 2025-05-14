package me.igorunderplayer.kono.commands.slash.testing

import dev.kord.common.Color
import dev.kord.common.entity.ApplicationCommandOption
import dev.kord.common.entity.ApplicationCommandOptionType
import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import dev.kord.rest.Image
import dev.kord.rest.builder.message.embed
import me.igorunderplayer.kono.commands.KonoSlashCommand

class Avatar: KonoSlashCommand {
    override val name = "avatar"
    override val description = "Retorna avatar de alguem"

    override val options: List<ApplicationCommandOption> = listOf(
        ApplicationCommandOption(
            name = "usuario",
            description = "usuario de quem vai ser o avatar",
            type = ApplicationCommandOptionType.User
        )
    )


    override suspend fun run(event: ChatInputCommandInteractionCreateEvent) {
        val response = event.interaction.deferPublicResponse()
        val user = event.interaction.command.users["usuario"] ?: event.interaction.user

        val avatar = user.avatar

        val avatarFormat = if (avatar?.isAnimated == true) Image.Format.GIF else Image.Format.PNG

        val fullAvatarUrl = avatar?.cdnUrl?.toUrl {
            size = Image.Size.Size4096
            format = avatarFormat
        }

        val avatarUrl = avatar?.cdnUrl?.toUrl {
            size = Image.Size.Size1024
            format = avatarFormat
        }

        response.respond {
            embed {
                description = "[Download]($fullAvatarUrl)"
                image = avatarUrl
                color = Color(2895667)
            }
        }
    }
}