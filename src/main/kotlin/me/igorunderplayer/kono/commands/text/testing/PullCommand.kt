package me.igorunderplayer.kono.commands.text


import dev.kord.core.behavior.reply
import dev.kord.core.event.message.MessageCreateEvent
import me.igorunderplayer.kono.commands.BaseCommand
import me.igorunderplayer.kono.commands.CommandCategory
import me.igorunderplayer.kono.services.GachaResult
import me.igorunderplayer.kono.services.GachaService

class PullCommand(
    private val gachaService: GachaService
) : BaseCommand(
    name = "pull",
    description = "Pull a random card using essence!",
    category = CommandCategory.Misc
) {

    override suspend fun run(event: MessageCreateEvent, args: Array<String>) {
        val userId = event.message.author?.id?.value?.toLong() ?: return

        println(args.joinToString(" "))
        val multiple = args.getOrNull(0) == "10"

        when (val result = gachaService.pull(userId, multiple)) {
            is GachaResult.Success -> {
                event.message.reply {
                    content = "🎰 Você puxou: **${result.cardName}** (${result.rarity})\n" +
                            "💎 Essence restante: ${result.remainingEssence}"
                }
            }

            is GachaResult.MultipePullSuccess -> {
                val pulledCards = result.pulledCards.joinToString("\n") { "🎰 **${it.cardName}** (${it.rarity})" }
                event.message.reply {
                    content = "Você puxou:\n$pulledCards\n" +
                            "💎 Essence restante: ${result.remainingEssence}"
                }
            }

            GachaResult.NotEnoughEssence -> {
                event.message.reply { content = "Você não tem essence suficiente 💎" }
            }

            GachaResult.UserNotFound -> {
                event.message.reply { content = "Você precisa se registrar primeiro." }
            }

            GachaResult.NoCardsAvailable -> {
                event.message.reply { content = "Nenhuma carta disponível nessa raridade." }
            }

            else -> {
                event.message.reply { content = "Erro ao fazer pull." }
            }
        }
    }
}

