package me.igorunderplayer.kono.commands.text.testing

import dev.kord.core.event.message.MessageCreateEvent
import me.igorunderplayer.kono.commands.BaseCommand
import me.igorunderplayer.kono.commands.CommandCategory
import me.igorunderplayer.kono.services.DailyResult
import me.igorunderplayer.kono.services.DailyService
import java.time.Duration
import java.time.ZoneId
import java.time.ZonedDateTime

class DailyCommand(
    private val dailyService: DailyService

): BaseCommand(
    name = "daily",
    description = "Get your daily reward!",
    category = CommandCategory.Misc
) {

    override suspend fun run(
        event: MessageCreateEvent,
        args: Array<String>
    ) {
        val userId = event.message.author?.id ?: return

        when (val result = dailyService.claimDaily(userId.value.toLong())) {

            is DailyResult.Success -> {
                val weeklyText = if (result.bonusApplied) {
                    "\n🔥 **Bônus semanal desbloqueado!**"
                } else ""

                event.message.channel.createMessage(
                    """
                💰 Você recebeu **${result.reward}** essences!
                
                📊 Streak: **${result.streak}/7**
                💳 Essences: **${result.balance}**$weeklyText
                """.trimIndent()
                )
            }

            is DailyResult.AlreadyClaimed -> {
                val now = ZonedDateTime.now(ZoneId.of("UTC"))
                val remaining = Duration.between(now, result.nextReset)

                val hours = remaining.toHours()
                val minutes = remaining.toMinutes() % 60

                event.message.channel.createMessage(
                    "⏳ Você já pegou seu daily.\nTente novamente em **${hours}h ${minutes}m**"
                )
            }

            is DailyResult.UserNotFound -> {
                event.message.channel.createMessage(
                    "❌ Você não está registrado.\nUse `register` para começar!"
                )
            }

            else -> {
                event.message.channel.createMessage(
                    "⚠️ Ocorreu um erro ao pegar o daily."
                )
            }
        }
    }
}
