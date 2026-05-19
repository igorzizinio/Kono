package me.igorunderplayer.kono.commands.slash.game

import dev.kord.common.entity.ApplicationCommandOption
import dev.kord.common.entity.ApplicationCommandOptionType
import dev.kord.common.entity.optional.Optional
import dev.kord.core.entity.interaction.SubCommand
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import me.igorunderplayer.kono.commands.KonoSlashCommand
import me.igorunderplayer.kono.commands.KonoSlashSubCommand
import me.igorunderplayer.kono.commands.slash.game.subcommand.PersonagemDefinir
import me.igorunderplayer.kono.commands.slash.game.subcommand.PersonagemInfo
import me.igorunderplayer.kono.commands.slash.game.subcommand.PersonagemUpgrade
import me.igorunderplayer.kono.domain.team.BuildUnitHandler
import me.igorunderplayer.kono.domain.team.SetActiveCharacterHandler
import me.igorunderplayer.kono.domain.team.UpgradeCharacterHandler

class PersonagemSlashCommand(
    buildUnitHandler: BuildUnitHandler,
    setActiveCharacterHandler: SetActiveCharacterHandler,
    upgradeCharacterHandler: UpgradeCharacterHandler
) : KonoSlashCommand {
    override val name = "personagem"
    override val description = "Gerencia seu personagem ativo — veja stats, defina e faça upgrade"

    private val subCommands: List<KonoSlashSubCommand> = listOf(
        PersonagemInfo(buildUnitHandler, upgradeCharacterHandler),
        PersonagemDefinir(setActiveCharacterHandler),
        PersonagemUpgrade(upgradeCharacterHandler)
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
