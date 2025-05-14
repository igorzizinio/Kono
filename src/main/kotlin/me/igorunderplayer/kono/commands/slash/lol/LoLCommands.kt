package me.igorunderplayer.kono.commands.slash.lol

import dev.kord.common.entity.ApplicationCommandOption
import dev.kord.common.entity.ApplicationCommandOptionType
import dev.kord.common.entity.optional.Optional
import dev.kord.core.entity.interaction.SubCommand
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import me.igorunderplayer.kono.commands.KonoSlashCommand
import me.igorunderplayer.kono.commands.KonoSlashSubCommand
import me.igorunderplayer.kono.commands.slash.lol.subcommand.Assign
import me.igorunderplayer.kono.commands.slash.lol.subcommand.Points
import me.igorunderplayer.kono.commands.slash.lol.subcommand.Profile

class LoLCommands(): KonoSlashCommand {
    override val name = "lol"
    override val description = "comandos relacionados a league of legends"

    private val subCommands = listOf<KonoSlashSubCommand>(Profile(), Points(), Assign())

    override val options: List<ApplicationCommandOption> = this.subCommands.map {
        ApplicationCommandOption(
            name = it.name,
            description = it.description,
            options = Optional(it.options),
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