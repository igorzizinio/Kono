package me.igorunderplayer.kono.commands.slash.image.subcommand

import dev.kord.common.entity.ApplicationCommandOption
import dev.kord.common.entity.ApplicationCommandOptionType
import dev.kord.common.entity.optional.Optional
import dev.kord.common.entity.optional.OptionalBoolean
import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import dev.kord.rest.Image
import io.ktor.client.request.forms.*
import io.ktor.utils.io.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonPrimitive
import me.igorunderplayer.kono.commands.KonoSlashSubCommand
import java.awt.Color
import java.awt.GradientPaint
import java.awt.RenderingHints
import java.awt.geom.RoundRectangle2D
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.net.URI
import javax.imageio.ImageIO

class Border: KonoSlashSubCommand {
    override val name = "border"
    override val description = "cria uma bordinha para sua foto de perfil"
    override val options: List<ApplicationCommandOption> = listOf(
        ApplicationCommandOption(
            name = "user",
            description =  "usuario",
            required = OptionalBoolean.Value(true),
            type = ApplicationCommandOptionType.User
        ),

        ApplicationCommandOption(
            name = "color1",
            description =  "cor hexadecimal (padrão: #FFFFFF)",
            type = ApplicationCommandOptionType.String
        ),
        ApplicationCommandOption(
            name = "color2",
            description =  "cor hexadecimal (padrão: #FFFFFF)",
            type = ApplicationCommandOptionType.String
        ),
        ApplicationCommandOption(
            name = "padding",
            description = "espaçamento (padrão: 48)",
            minValue = Optional(JsonPrimitive(0.0)),
            maxValue = Optional(JsonPrimitive(255.0)),
            type = ApplicationCommandOptionType.Number
        )
    )


    override suspend fun run(event: ChatInputCommandInteractionCreateEvent) {
        val user = event.interaction.command.users["user"]
        val color = event.interaction.command.strings["color"] ?: "#FFFFFF"
        val color2 = event.interaction.command.strings["color2"] ?: color
        val padding = event.interaction.command.numbers["padding"]?.toInt() ?: 48

        val avatar = user?.avatar?.cdnUrl?.toUrl {
            format = Image.Format.JPEG
            size = Image.Size.Size2048
        }

        if (avatar == null) {
            event.interaction.respondEphemeral {
                content = "ué (usuário precisa ter um avatar né)"
            }
            return
        }

        val response = event.interaction.deferPublicResponse()

        val uri = URI.create(avatar)
        val file = withContext(Dispatchers.IO) {
            ImageIO.read(uri.toURL())
        }

        val bgColor = Color.decode(color)
        val bgColor2 = Color.decode(color2)
        val rounded = generateAvatarBorder(file, bgColor, bgColor2, padding)

        ByteArrayOutputStream().use {
            ImageIO.write(rounded, "png", it)
            val imageByteArray = it.toByteArray()
            response.respond {
                content = "borda ne"
                addFile("image.png", ChannelProvider { ByteReadChannel(imageByteArray) })
            }

            file.flush()
            rounded.flush()
        }
    }

    private fun generateAvatarBorder(avatar: BufferedImage, color: Color, color2: Color, padding: Int): BufferedImage {
        val width = avatar.width
        val height = avatar.height
        val output = BufferedImage(width + padding, height + padding, BufferedImage.TYPE_INT_ARGB)
        val g2 = output.createGraphics()

        g2.paint = GradientPaint(0F, 0F, color, 0F, height.toFloat(), color2)
        g2.fillRect(0, 0, width + padding, height + padding)

        val qualityHints = RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        qualityHints[RenderingHints.KEY_RENDERING] = RenderingHints.VALUE_RENDER_QUALITY

        g2.setRenderingHints(qualityHints)
        g2.clip = RoundRectangle2D.Float(
            (padding / 2).toFloat(), (padding / 2).toFloat(), width.toFloat(), height.toFloat(), (width).toFloat(),
            (height).toFloat()
        )
        g2.drawImage(avatar, padding / 2, padding / 2, null)

        g2.dispose()

        return output
    }
}