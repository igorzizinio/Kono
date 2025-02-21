package me.igorunderplayer.kono.commands.slash.lol

import dev.kord.core.Kord
import dev.kord.core.entity.application.GlobalChatInputCommand
import dev.kord.core.entity.interaction.SubCommand
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import dev.kord.rest.builder.interaction.subCommand
import me.igorunderplayer.kono.commands.KonoSlashCommand
import me.igorunderplayer.kono.commands.KonoSlashSubCommand
import me.igorunderplayer.kono.commands.slash.lol.subcommand.Points
import me.igorunderplayer.kono.commands.slash.lol.subcommand.Profile

class LoLCommands(): KonoSlashCommand {
    override val name = "lol"
    override val description = "comandos relacionados a league of legends"

    private val subCommands = listOf<KonoSlashSubCommand>(Profile(), Points())

    override suspend fun setup(kord: Kord): GlobalChatInputCommand {
        return kord.createGlobalChatInputCommand(this.name, this.description) {
            for (cmd in subCommands) {
                subCommand(cmd.name, cmd.description) {
                    cmd.options().invoke(this)
                }
            }
        }
    }

    override suspend fun run(event: ChatInputCommandInteractionCreateEvent) {
        val cmd = event.interaction.command as SubCommand

        subCommands.find {
            cmd.name == it.name
        }?.run(event)
    }
}