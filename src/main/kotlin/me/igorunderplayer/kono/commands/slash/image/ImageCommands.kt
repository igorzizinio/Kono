package me.igorunderplayer.kono.commands.slash.image

import dev.kord.common.entity.ApplicationCommandOption
import dev.kord.common.entity.ApplicationCommandOptionType
import dev.kord.core.entity.interaction.SubCommand
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import me.igorunderplayer.kono.commands.KonoSlashCommand
import me.igorunderplayer.kono.commands.slash.image.subcommand.Border
import me.igorunderplayer.kono.commands.slash.image.subcommand.Pixelate

class ImageCommands: KonoSlashCommand {
    override val name = "image"
    override val description = "comandos relacionados a manipulação de imagem"

    private val subCommands = listOf(
        Border(),
        Pixelate()
    )

    override val options = this.subCommands.map {
            ApplicationCommandOption(
                name = it.name,
                description = it.description,
                type = ApplicationCommandOptionType.SubCommand
            )
    }

    override suspend fun run(event: ChatInputCommandInteractionCreateEvent) {
        val cmd = event.interaction.command as SubCommand

        subCommands.find {
            cmd.name == it.name
        }?.run(event)
    }
}