package me.igorunderplayer.kono.commands.text.testing

import dev.kord.common.Color
import dev.kord.core.behavior.channel.createEmbed
import dev.kord.core.event.message.MessageCreateEvent
import kotlinx.coroutines.flow.count
import me.igorunderplayer.kono.commands.BaseCommand
import me.igorunderplayer.kono.commands.CommandCategory
import java.lang.management.ManagementFactory

class Info : BaseCommand(
    "info",
    "mostra info",
    category = CommandCategory.Util,
    aliases = listOf("botinfo", "stats")
) {
    override suspend fun run(event: MessageCreateEvent, args: Array<String>) {
        val descriptionBuilder = StringBuilder()
        val runtime = Runtime.getRuntime()
        val mb = 1024 * 1024

        val usedMemory = (runtime.totalMemory() - runtime.freeMemory()) / mb
        val freeMemory = runtime.freeMemory() / mb
        val maxMemory = runtime.maxMemory() / mb
        val totalMemory = runtime.totalMemory() / mb

        descriptionBuilder.appendLine("\uD83C\uDF10 Em ${event.kord.guilds.count()} servidores")
        descriptionBuilder.appendLine()
        descriptionBuilder.appendLine("☕ Versão do java: ${System.getProperty("java.version")}")
        descriptionBuilder.appendLine("☕ Versão do kotlin: ${KotlinVersion.CURRENT}")
        descriptionBuilder.appendLine()
        descriptionBuilder.appendLine("\uD83D\uDCBB Usando ${usedMemory}MB de ram \uD83D\uDC38")
        descriptionBuilder.appendLine("\uD83D\uDCBB Memoria disponivel: ${freeMemory}MB")
        descriptionBuilder.appendLine("\uD83D\uDCBB Total alocado: ${totalMemory}MB")
        descriptionBuilder.appendLine("\uD83D\uDCBB E memoria no total: ${maxMemory}MB")
        descriptionBuilder.appendLine()
        descriptionBuilder.appendLine("#️⃣ Threads: ${ManagementFactory.getThreadMXBean().threadCount}")

        event.message.channel.createEmbed {
            description = descriptionBuilder.toString()
            color = Color(2895667)
        }
    }
}
