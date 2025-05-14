package me.igorunderplayer.kono.commands.slash.testing

import dev.kord.common.Color
import dev.kord.common.entity.ApplicationCommandOption
import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import dev.kord.rest.builder.message.embed
import kotlinx.coroutines.flow.count
import kotlinx.datetime.Clock
import me.igorunderplayer.kono.Kono
import me.igorunderplayer.kono.commands.KonoSlashCommand
import me.igorunderplayer.kono.utils.humanizeDuration
import java.lang.management.ManagementFactory

class Info: KonoSlashCommand {
    override val name = "info"
    override val description = "shows info"

    override val options: List<ApplicationCommandOption> = listOf()


    override suspend fun run(event: ChatInputCommandInteractionCreateEvent) {
        val response = event.interaction.deferPublicResponse()
        val descriptionBuilder = StringBuilder()
        val runtime = Runtime.getRuntime()
        val mb = 1024 * 1024


        val usedMemory = (runtime.totalMemory() - runtime.freeMemory()) / mb
        val freeMemory = runtime.freeMemory() / mb
        val maxMemory = runtime.maxMemory() / mb
        val totalMemory = runtime.totalMemory() / mb

        val uptime = Clock.System.now() - Kono.startupAt

        descriptionBuilder.appendLine("\uD83C\uDF10 Em ${event.kord.guilds.count()} servidores")
        descriptionBuilder.appendLine()
        descriptionBuilder.appendLine("☕ Versão do Java: ${System.getProperty("java.version")}")
        descriptionBuilder.appendLine("\uD83D\uDCDC Versão do Kotlin: ${KotlinVersion.CURRENT}")
        descriptionBuilder.appendLine()
        descriptionBuilder.appendLine("\uD83D\uDDA5\uFE0F Sistema operacional: ${System.getProperty("os.name")}")
        descriptionBuilder.appendLine()
        descriptionBuilder.appendLine("\uD83D\uDCBB Usando ${usedMemory}MB de ram \uD83D\uDC38")
        descriptionBuilder.appendLine("\uD83D\uDCBB Memoria disponivel: ${freeMemory}MB")
        descriptionBuilder.appendLine("\uD83D\uDCBB Total alocado: ${totalMemory}MB")
        descriptionBuilder.appendLine("\uD83D\uDCBB E memoria no total: ${maxMemory}MB")
        descriptionBuilder.appendLine()
        descriptionBuilder.appendLine("#️⃣ Threads: ${ManagementFactory.getThreadMXBean().threadCount}")
        descriptionBuilder.appendLine("\uD83D\uDD50 Tempo ativo: ${humanizeDuration(uptime)}")

        response.respond {
            embed {
                description = descriptionBuilder.toString()
                color = Color(2895667)
            }
        }
    }
}