package me.igorunderplayer.kono.commands.slash.image.subcommand

import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import dev.kord.rest.builder.interaction.SubCommandBuilder
import dev.kord.rest.builder.interaction.attachment
import dev.kord.rest.builder.interaction.number
import io.ktor.client.request.forms.*
import io.ktor.utils.io.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.igorunderplayer.kono.commands.KonoSlashSubCommand
import java.awt.image.BufferedImage
import java.awt.image.Raster
import java.io.ByteArrayOutputStream
import java.net.URI
import java.net.URL
import javax.imageio.ImageIO


class Pixelate: KonoSlashSubCommand {
    override val name = "pixelate"
    override val description = "deixa uma imagem pixelizada"

    override fun options(): SubCommandBuilder.() -> Unit {
        return {
            attachment("image", "imagem para ser usada") {
                required = true
            }

            number("pixel_size", "pixel size")
        }
    }

    override suspend fun run(event: ChatInputCommandInteractionCreateEvent) {
        val attachment = event.interaction.command.attachments["image"]
        val pixelSize = event.interaction.command.numbers["pixel_size"]?.toInt() ?: 10


        if (attachment == null) {
            event.interaction.respondEphemeral {
                content = "vc precisa anexar uma imagem"
            }
            return
        }

        if (pixelSize <= 0) {
            event.interaction.respondEphemeral {
                content = "pixel size precisa ser maior q 0"
            }
        }

        val response = event.interaction.deferPublicResponse()


        val uri = URI.create(attachment.url)
        val img = pixelateFromURL(uri.toURL(), pixelSize)

        ByteArrayOutputStream().use {
            ImageIO.write(img, "png", it)
            val imageByteArray = it.toByteArray()
            response.respond {
                content = "\uD83D\uDC7E pixels"
                addFile("image.png", ChannelProvider { ByteReadChannel(imageByteArray) })
            }

            img.flush()
        }
    }

    private suspend fun pixelateFromURL(url: URL, pixelSize: Int = 10): BufferedImage {
        val img = withContext(Dispatchers.IO) {
            ImageIO.read(url)
        }

        val src: Raster = img.getData()
        val dest = src.createCompatibleWritableRaster()

        var y = 0
        while (y < src.height) {
            var x = 0
            while (x < src.width) {
                var pixel: DoubleArray? = DoubleArray(4)
                pixel = src.getPixel(x, y, pixel)
                var yd = y
                while (yd < y + pixelSize && yd < dest.height) {
                    var xd = x
                    while (xd < x + pixelSize && xd < dest.width) {
                        dest.setPixel(xd, yd, pixel)
                        xd++
                    }
                    yd++
                }
                x += pixelSize
            }
            y += pixelSize
        }
        img.setData(dest)
        return img
    }
}
