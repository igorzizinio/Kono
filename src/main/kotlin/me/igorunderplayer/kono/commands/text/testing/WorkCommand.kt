package me.igorunderplayer.kono.commands.text.testing

import dev.kord.core.behavior.reply
import dev.kord.core.event.message.MessageCreateEvent
import me.igorunderplayer.kono.commands.BaseCommand
import me.igorunderplayer.kono.services.WorkResult
import me.igorunderplayer.kono.services.WorkService

class WorkCommand(
    private val workService: WorkService
): BaseCommand(
    name = "work",
    description = "trabalhe"
) {
    override suspend fun run(event: MessageCreateEvent, args: Array<String>) {
        val userId = event.message.author?.id?.value ?: return
        when (val result = workService.work(userId.toLong())) {

            is WorkResult.Success -> {
                event.message.reply {
                    content = "💼 Você trabalhou e ganhou ₭${result.amount}!\n" +
                            "Saldo: ₭${result.balance}"
                }


            }

            is WorkResult.OnCooldown -> {
                val minutes = result.remaining.toMinutes()
                event.message.reply {
                    content = "⏳ Você precisa esperar $minutes minutos para trabalhar novamente."
                }
            }

            is WorkResult.UserNotFound -> {
                event.message.reply {
                    content = "❌ Você não está registrado.\nUse `register` para começar!"
                }
            }

            else -> {
                event.message.reply {
                    content = "Ocorreu um erro ao tentar trabalhar. Tente novamente mais tarde."
                }
            }
        }
    }
}
