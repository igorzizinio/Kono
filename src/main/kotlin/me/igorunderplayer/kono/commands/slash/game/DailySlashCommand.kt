package me.igorunderplayer.kono.commands.slash.game

import dev.kord.common.entity.ApplicationCommandOption
import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import me.igorunderplayer.kono.commands.KonoSlashCommand
import me.igorunderplayer.kono.services.DailyResult
import me.igorunderplayer.kono.services.DailyService
import java.time.Duration
import java.time.ZoneId
import java.time.ZonedDateTime

class DailySlashCommand(
    private val dailyService: DailyService
) : KonoSlashCommand {
    override val name = "daily"
    override val description = "Resgata sua recompensa diária de essence"
    override val options: List<ApplicationCommandOption> = listOf()

    override suspend fun run(event: ChatInputCommandInteractionCreateEvent) {
        val deferred = event.interaction.deferEphemeralResponse()
        val discordId = event.interaction.user.id.value.toLong()

        when (val result = dailyService.claimDaily(discordId)) {
            is DailyResult.Success -> {
                val bonusLine = if (result.bonusApplied) "\n🔥 **Bônus semanal desbloqueado!** (+180 💎 extra)" else ""
                deferred.respond {
                    content = buildString {
                        appendLine("✅ Você recebeu **${result.reward} 💎** essence!")
                        appendLine()
                        appendLine("📊 Streak: **${result.streak}/7**")
                        append("💎 Saldo atual: **${result.balance}**")
                        if (bonusLine.isNotBlank()) append(bonusLine)
                    }
                }
            }
            is DailyResult.AlreadyClaimed -> {
                val now = ZonedDateTime.now(ZoneId.of("UTC"))
                val remaining = Duration.between(now, result.nextReset)
                val hours = remaining.toHours()
                val minutes = remaining.toMinutes() % 60
                deferred.respond {
                    content = "⏳ Você já resgatou seu daily hoje.\nPróximo reset em **${hours}h ${minutes}m**."
                }
            }
            DailyResult.UserNotFound -> deferred.respond {
                content = "❌ Você não está registrado. Use `/register` para começar."
            }
            else -> deferred.respond { content = "⚠️ Erro ao resgatar o daily. Tente novamente." }
        }
    }
}
