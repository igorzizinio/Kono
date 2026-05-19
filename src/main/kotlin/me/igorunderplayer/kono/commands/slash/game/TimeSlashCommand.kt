package me.igorunderplayer.kono.commands.slash.game

import dev.kord.common.entity.ApplicationCommandOption
import dev.kord.common.entity.ApplicationCommandOptionType
import dev.kord.common.entity.optional.Optional
import dev.kord.core.entity.interaction.SubCommand
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import me.igorunderplayer.kono.commands.KonoSlashCommand
import me.igorunderplayer.kono.commands.KonoSlashSubCommand
import me.igorunderplayer.kono.commands.slash.game.subcommand.TimeDefinir
import me.igorunderplayer.kono.commands.slash.game.subcommand.TimeRemover
import me.igorunderplayer.kono.commands.slash.game.subcommand.TimeVer
import me.igorunderplayer.kono.data.repositories.BattleTeamRepository
import me.igorunderplayer.kono.data.repositories.CardInstanceRepository
import me.igorunderplayer.kono.data.repositories.UserRepository

class TimeSlashCommand(
    userRepository: UserRepository,
    cardInstanceRepository: CardInstanceRepository,
    battleTeamRepository: BattleTeamRepository
) : KonoSlashCommand {
    override val name = "time"
    override val description = "Gerencia seu time de batalha — monte os 3 slots e veja sua formação"

    private val subCommands: List<KonoSlashSubCommand> = listOf(
        TimeVer(userRepository, cardInstanceRepository, battleTeamRepository),
        TimeDefinir(userRepository, cardInstanceRepository, battleTeamRepository),
        TimeRemover(userRepository, battleTeamRepository)
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
