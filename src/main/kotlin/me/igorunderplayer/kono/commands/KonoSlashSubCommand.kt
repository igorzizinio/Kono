package me.igorunderplayer.kono.commands

import dev.kord.common.entity.ApplicationCommandOption
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent

interface KonoSlashSubCommand {
    val name: String
    val description: String


    val options: List<ApplicationCommandOption>


    suspend fun run(event: ChatInputCommandInteractionCreateEvent)
}