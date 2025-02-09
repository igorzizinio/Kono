package me.igorunderplayer.kono.commands.text.testing

import dev.kord.core.behavior.reply
import dev.kord.core.event.message.MessageCreateEvent
import io.ktor.client.request.forms.*
import io.ktor.utils.io.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.igorunderplayer.kono.commands.BaseCommand
import java.awt.Color
import java.awt.LinearGradientPaint
import java.awt.RenderingHints
import java.awt.geom.Point2D
import java.awt.geom.RoundRectangle2D
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.net.URI
import javax.imageio.ImageIO

class BorderGradient: BaseCommand(
    name = "bordergradient",
    description = "sim"
) {

    override suspend fun run(event: MessageCreateEvent, args: Array<String>) {
        val colors = args.map {
            Color.decode(it)
        }

        val attach = event.message.attachments.firstOrNull()
        if (attach == null) {
            event.message.reply {
                content = "\uD83D\uDD95"
            }
            return
        }

        val attachUri = URI.create(attach.url)
        val image = withContext(Dispatchers.IO) {
            ImageIO.read(attachUri.toURL())
        }

        val result = generateBorder(image, 12, colors)

        ByteArrayOutputStream().use {
            ImageIO.write(result, "png", it)
            val imageByteArray = it.toByteArray()
            event.message.reply {
                content = "viadao bonito"
                addFile("image.png", ChannelProvider { ByteReadChannel(imageByteArray) })
            }

            image.flush()
            result.flush()
        }

    }


    private fun generateBorder(image: BufferedImage, padding: Int, colors: List<Color>): BufferedImage {
        val width = image.width
        val height = image.height

        val start = Point2D.Float(0F, 0F)
        val end = Point2D.Float(width.toFloat(), height.toFloat())

        val dist = FloatArray(colors.size) { it.toFloat() / (colors.size - 1) }

        val output = BufferedImage(width + padding, height + padding, BufferedImage.TYPE_INT_ARGB)
        val g2 = output.createGraphics()
        g2.paint = LinearGradientPaint(start, end, dist, colors.toTypedArray())
        g2.fillRect(0, 0, width + padding, height + padding)

        val qualityHints = RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        qualityHints[RenderingHints.KEY_RENDERING] = RenderingHints.VALUE_RENDER_QUALITY

        g2.setRenderingHints(qualityHints)
        g2.clip = RoundRectangle2D.Float(
            (padding / 2).toFloat(), (padding / 2).toFloat(), width.toFloat(), height.toFloat(), (width).toFloat(),
            (height).toFloat()
        )
        g2.drawImage(image, padding / 2, padding / 2, null)

        g2.dispose()

        return output
    }
}