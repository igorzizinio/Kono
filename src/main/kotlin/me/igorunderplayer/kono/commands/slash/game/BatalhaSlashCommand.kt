package me.igorunderplayer.kono.commands.slash.game

import dev.kord.common.entity.ApplicationCommandOption
import dev.kord.common.entity.ApplicationCommandOptionType
import dev.kord.common.entity.optional.Optional
import dev.kord.core.entity.interaction.SubCommand
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import me.igorunderplayer.kono.commands.KonoSlashCommand
import me.igorunderplayer.kono.commands.KonoSlashSubCommand
import me.igorunderplayer.kono.commands.slash.game.subcommand.BatalhaBot
import me.igorunderplayer.kono.commands.slash.game.subcommand.BatalhaJogador
import me.igorunderplayer.kono.services.TeamBattleService

class BatalhaSlashCommand(
    teamBattleService: TeamBattleService
) : KonoSlashCommand {
    override val name = "batalha"
    override val description = "Luta de times 3v3 — desafie bots ou outros jogadores"

    private val subCommands: List<KonoSlashSubCommand> = listOf(
        BatalhaBot(teamBattleService),
        BatalhaJogador(teamBattleService)
    )

    override val options: List<ApplicationCommandOption> = subCommands.map {
        ApplicationCommandOption(
            name = it.name,
            description = it.description,
            options = Optional(it.options),
            type = ApplicationCommandOptionType.SubCommand
        )
    }

    override suspend fun run(event: ChatInputCommandInteractionCreateEvent) {
        val cmd = event.interaction.command as SubCommand
        subCommands.find { it.name == cmd.name }?.run(event)
    }
}
