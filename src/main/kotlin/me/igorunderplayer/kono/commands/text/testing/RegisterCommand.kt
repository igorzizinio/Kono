package me.igorunderplayer.kono.commands.text.testing


import dev.kord.core.behavior.reply
import dev.kord.core.event.message.MessageCreateEvent
import me.igorunderplayer.kono.commands.BaseCommand

class RegisterCommand(
    private val userService: me.igorunderplayer.kono.services.UserService,
): BaseCommand(
    name = "register",
    description = "se registre"
) {
    override suspend fun run(event: MessageCreateEvent, args: Array<String>) {
        val discordId = event.message.author?.id?.value?.toLong() ?: return
        var user = userService.getUserByDiscordId(discordId)

        if (user != null) {
            event.message.reply {
                content = "vc ja tem conta taligado"
            }
        } else {
            user = userService.createUser(discordId)
            event.message.reply {
                content = if (user == null) "não foi possivel criar sua conta :(" else "sua conta foi criada com sucesso!!"
            }
        }
    }
}
