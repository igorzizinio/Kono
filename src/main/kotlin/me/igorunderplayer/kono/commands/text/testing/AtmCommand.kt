package me.igorunderplayer.kono.commands.text.testing

import dev.kord.common.Color
import dev.kord.core.behavior.reply
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.rest.builder.message.embed
import me.igorunderplayer.kono.commands.BaseCommand
import me.igorunderplayer.kono.services.UserService

class AtmCommand(
    private val userService: UserService
) : BaseCommand(
    name = "atm",
    description = "Exibe seu saldo de Konos e Essence",
    aliases = listOf("saldo", "money", "essences", "konos")
) {
    override suspend fun run(event: MessageCreateEvent, args: Array<String>) {
        val discordUser = event.message.author ?: return
        val user = userService.getUserByDiscordId(discordUser.id.value.toLong()) ?: return
        event.message.reply {
            embed {
                title = "🏧 Saldo de ${discordUser.username}"
                color = Color(0, 255, 165)
                description = buildString {
                    appendLine("💸 Konos: ${user.konos}")
                    appendLine("💎 Essence: ${user.essence}")
                }
            }
        }
    }
}
