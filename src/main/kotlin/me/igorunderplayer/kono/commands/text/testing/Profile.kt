package me.igorunderplayer.kono.commands.text.testing

import dev.kord.core.behavior.reply
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.rest.Image
import io.ktor.client.request.forms.*
import io.ktor.utils.io.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.igorunderplayer.kono.commands.BaseCommand
import me.igorunderplayer.kono.commands.CommandCategory
import me.igorunderplayer.kono.services.UserService
import me.igorunderplayer.kono.utils.getMentionedUser
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.awt.Color
import java.awt.Font
import java.awt.RenderingHints
import java.awt.geom.Ellipse2D
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.net.URI
import javax.imageio.ImageIO

class Profile : BaseCommand(
    "profile",
    "exibe o perfil de alguem",
    category = CommandCategory.Misc,
    aliases = listOf("perfil")
), KoinComponent {

    private val userService: UserService by inject()

    override suspend fun run(event: MessageCreateEvent, args: Array<String>) {
        val width = 800
        val height = 600

        if (event.guildId == null) return

        val user = getMentionedUser(event.message, args)

        if (user == null) {
            event.message.reply {
                content = "Usuario não encontrado"
            }

            return
        }

        val dbUser = userService.getOrCreateUserByDiscordId(user.id.value.toLong())

        if (dbUser == null) {
            event.message.reply {
                content = "Usuario não encontrado"
            }

            return
        }

        val profileImageBuffer = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
        val g2 = profileImageBuffer.createGraphics()
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        g2.color = Color(49, 51, 56)
        g2.fillRect(0, 0, width, height)


        // Draw info
        val nameFont = Font("Arial", Font.ITALIC, 32)
        val subInfoFont = Font("Arial", Font.ITALIC, 24)
        g2.font = nameFont
        g2.paint = Color.WHITE
        g2.drawString(user.username, 152, 46)
        val fontMetrics = g2.fontMetrics
        val stringWidth = fontMetrics.stringWidth(user.username)
        g2.font = subInfoFont
        g2.paint = Color.LIGHT_GRAY
        g2.drawString('#' + user.username, 152 + stringWidth + 4, 46)
        g2.drawString("${dbUser.money} Koins", 152, 78)
        g2.drawString("Level: ??", 152, 106)
        g2.drawString("XP: ??/???", 152, 134)

        // Draw avatar
        val avatar = (user.avatar ?: user.defaultAvatar).cdnUrl.toUrl {
            format = Image.Format.PNG
            size = Image.Size.Size128
        }

        val avatarImage = withContext(Dispatchers.IO) {
            ImageIO.read(URI(avatar).toURL())
        }

        // Border
        val center = 76f
        val borderRadius = 70f
        g2.color = Color(82, 201, 224)
        g2.fill(Ellipse2D.Float(center - borderRadius, center - borderRadius, 2f * borderRadius, 2f * borderRadius))

        // Avatar image
        val imageRadius = 64
        g2.clip = Ellipse2D.Float(12f, 12f, 128f, 128f)
        g2.drawImage(
            avatarImage,
            center.toInt() - imageRadius,
            center.toInt() - imageRadius,
            (2 * imageRadius),
            (2 * imageRadius),
            null
        )


        ByteArrayOutputStream().use {
            ImageIO.write(profileImageBuffer, "png", it)
            val imageByteArray = it.toByteArray()
            event.message.reply {
                addFile("profile.png", ChannelProvider { ByteReadChannel(imageByteArray) })
            }

            g2.dispose()
            profileImageBuffer.flush()
            avatarImage.flush()
            it.flush()
        }
    }
}