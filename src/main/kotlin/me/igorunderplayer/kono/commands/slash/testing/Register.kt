package me.igorunderplayer.kono.commands.slash.testing

import dev.kord.core.Kord
import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.entity.application.GlobalChatInputCommand
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import me.igorunderplayer.kono.commands.KonoSlashCommand
import me.igorunderplayer.kono.services.UserService
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class Register: KonoSlashCommand, KoinComponent {
    override val name = "register"
    override val description = "registra vc no database!"

    private val userService: UserService by inject()

    override suspend fun setup(kord: Kord): GlobalChatInputCommand {
        return kord.createGlobalChatInputCommand(
            this.name,
            this.description
        )
    }

    override suspend fun run(event: ChatInputCommandInteractionCreateEvent) {
        val response = event.interaction.deferEphemeralResponse()
        val discordId = event.interaction.user.id.value.toLong()
        var user = userService.getUserByDiscordId(discordId)

        if (user != null) {
            response.respond {
                content = "vc ja tem conta taligado"
            }
        } else {
            user = userService.createUser(discordId)

            response.respond {
                content = if (user == null) "não foi possivel criar sua conta :(" else "sua conta foi criada com sucesso!!"
            }

        }

    }
}