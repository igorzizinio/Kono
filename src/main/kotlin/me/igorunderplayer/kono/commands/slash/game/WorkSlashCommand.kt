package me.igorunderplayer.kono.commands.slash.game

import dev.kord.common.entity.ApplicationCommandOption
import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import me.igorunderplayer.kono.commands.KonoSlashCommand
import me.igorunderplayer.kono.services.WorkResult
import me.igorunderplayer.kono.services.WorkService

class WorkSlashCommand(
    private val workService: WorkService
) : KonoSlashCommand {
    override val name = "work"
    override val description = "Trabalha para ganhar KonoCoins (₭)"
    override val options: List<ApplicationCommandOption> = listOf()

    override suspend fun run(event: ChatInputCommandInteractionCreateEvent) {
        val deferred = event.interaction.deferEphemeralResponse()
        val discordId = event.interaction.user.id.value.toLong()

        when (val result = workService.work(discordId)) {
            is WorkResult.Success -> deferred.respond {
                content = "💼 Você trabalhou e ganhou **₭${result.amount}**!\n💰 Saldo: **₭${result.balance}**"
            }

            is WorkResult.OnCooldown -> {
                val minutes = result.remaining.toMinutes()
                val seconds = result.remaining.seconds % 60
                val timeStr = if (minutes > 0) "${minutes}m ${seconds}s" else "${seconds}s"
                deferred.respond {
                    content = "⏳ Aguarde **$timeStr** para trabalhar novamente."
                }
            }

            WorkResult.UserNotFound -> deferred.respond {
                content = "❌ Você não está registrado. Use `/register` para começar."
            }

            else -> deferred.respond { content = "⚠️ Erro ao trabalhar. Tente novamente." }
        }
    }
}
