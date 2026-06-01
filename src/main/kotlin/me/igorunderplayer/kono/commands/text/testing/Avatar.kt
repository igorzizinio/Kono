package me.igorunderplayer.kono.commands.text.testing

import dev.kord.common.Color
import dev.kord.core.behavior.channel.createEmbed
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.rest.Image
import me.igorunderplayer.kono.commands.BaseCommand
import me.igorunderplayer.kono.commands.CommandCategory
import me.igorunderplayer.kono.utils.getMentionedUser

class Avatar : BaseCommand(
    "avatar",
    "mostra o avatar de alguem",
    aliases = listOf("icon"),
    category = CommandCategory.Util
) {
    override suspend fun run(event: MessageCreateEvent, args: Array<String>) {
        val user = getMentionedUser(event.message, args) ?: event.kord.getSelf()

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

        event.message.channel.createEmbed {
            description = "[Download]($fullAvatarUrl)"
            image = avatarUrl
            color = Color(2895667)
        }
    }
}

