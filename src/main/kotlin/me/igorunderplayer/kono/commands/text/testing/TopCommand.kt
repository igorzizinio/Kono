package me.igorunderplayer.kono.commands.text.testing

import dev.kord.common.Color
import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.reply
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.rest.builder.message.embed
import me.igorunderplayer.kono.commands.BaseCommand
import me.igorunderplayer.kono.commands.CommandCategory
import me.igorunderplayer.kono.common.Colors
import me.igorunderplayer.kono.services.UserService

class TopCommand(
    private val userService: UserService
): BaseCommand(
    name = "top",
    description = "exibe diversos rankings relacionados a Kono",
    category = CommandCategory.Util,
    aliases = listOf("rank")
) {
    override suspend fun run(event: MessageCreateEvent, args: Array<String>) {
        val rankingType = args.firstOrNull()?.lowercase() ?: ""
        val page = args.getOrNull(1)?.toIntOrNull() ?: 1

        val ranking = when (rankingType) {
            "konos" -> {
                val ranking = userService.getTopMoney(10, (page - 1) * 10)
                ranking.mapIndexed { index, user ->
                    val discord = event.kord.getUser(Snowflake(user.discordId))
                    "**#${index + 1}** | ${discord?.globalName} - ₭${user.konos}"
                }.joinToString("\n")
            }
            else -> {
                null
            }
        }

        if (ranking == null) {
            event.message.reply {
                content = "você que é top!\n\n" +
                        "Tipos de ranking disponíveis:\n" +
                        "▸ `konos` - Ranking de KonoCoins"
            }
            return
        }

        event.message.reply {
            embed {
                title = "Ranking de: ${rankingType.uppercase()}"
                description = ranking
                color = Color(Colors.GOLD)
                footer {
                    text = "use `ranking {page}` para exibir outras páginas"
                }
            }
        }
    }
}
