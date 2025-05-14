package me.igorunderplayer.kono.commands

import dev.kord.common.entity.ApplicationCommandType
import dev.kord.common.entity.Permission
import dev.kord.common.entity.optional.Optional
import dev.kord.core.Kord
import dev.kord.core.behavior.reply
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.rest.json.request.ApplicationCommandCreateRequest
import me.igorunderplayer.kono.commands.slash.testing.DestinoCommand
import me.igorunderplayer.kono.commands.text.dev.DeleteApplicationCommand
import me.igorunderplayer.kono.commands.text.dev.GuildsCommand
import me.igorunderplayer.kono.commands.text.`fun`.TinderCommand
import me.igorunderplayer.kono.commands.text.lol.LoLChampion
import me.igorunderplayer.kono.commands.text.lol.LoLMatches
import me.igorunderplayer.kono.commands.text.testing.*
import org.slf4j.LoggerFactory

enum class CommandCategory {
    Util,
    Misc,
    Management,
    LoL,
    Developer,
    Other
}

class CommandManager(private val kord: Kord)  {
    private val logger = LoggerFactory.getLogger(this::class.java)
    val commandList = mutableListOf<BaseCommand>()
    private val applicationCommandList = mutableListOf<KonoSlashCommand>()

    fun start() {
        registerCommand(Avatar())
        registerCommand(Info())
        registerCommand(Help())

        registerCommand(Profile())
        registerCommand(DestinoTextCommand())
        registerCommand(BorderGradient())
        registerCommand(TinderCommand())

        registerCommand(Clear())

        registerCommand(LoLChampion())
        registerCommand(LoLMatches())


        // Developer Comamands
        registerCommand(GuildsCommand())
        registerCommand(DeleteApplicationCommand())



        // Register slash commands

        registerSlashCommand(me.igorunderplayer.kono.commands.slash.testing.Info())
        registerSlashCommand(me.igorunderplayer.kono.commands.slash.testing.Avatar())
        registerSlashCommand(me.igorunderplayer.kono.commands.slash.testing.Register())
        registerSlashCommand(me.igorunderplayer.kono.commands.slash.lol.LoLCommands())
        registerSlashCommand(me.igorunderplayer.kono.commands.slash.image.ImageCommands())
        registerSlashCommand(DestinoCommand())

    }

    private fun registerCommand(command: BaseCommand) {
        val commandFound = commandList.any {
            it.name.lowercase() == command.name.lowercase()
        }

        if (commandFound) {
            val red = "\u001b[31m"
            val reset = "\u001b[0m"

            logger.error("$red ${command.name} text command is already registered, skipped! $reset")
            return
        }

        commandList.add(command)
    }

    private fun registerSlashCommand(command: KonoSlashCommand) {
        val commandFound = applicationCommandList.any {
            it.name.lowercase() == command.name.lowercase()
        }

        if (commandFound) {
            val red = "\u001b[31m"
            val reset = "\u001b[0m"

            logger.error("$red ${command.name} slash command is already registered, skipped! $reset")
            return
        }

        applicationCommandList.add(command)
    }

    suspend fun registerCommands() {
        kord.rest.interaction.createGlobalApplicationCommands(kord.selfId, this.applicationCommandList.map {
            ApplicationCommandCreateRequest(
                name = it.name,
                description = Optional(it.description),
                type = ApplicationCommandType.ChatInput,
                options = Optional(it.options)
            )
        })
    }


    fun searchCommand (search: String): BaseCommand? {
        val lowerCase = search.lowercase()

        return commandList.find {
            it.name == lowerCase || it.aliases.contains(lowerCase)
        }
    }

    suspend fun handleChatInputCommand(event: ChatInputCommandInteractionCreateEvent) {
        val cmd = this.applicationCommandList.find { it.name == event.interaction.command.rootName }

        cmd?.run(event)
    }

    suspend fun handleCommand(event: MessageCreateEvent) {
        val mentionRegExp = Regex("^<@!?${event.kord.selfId}>$")

        try {
            val args = event.message.content
                .trim()
                .split(' ')
                .toMutableList()

            val mention = args.removeAt(0)
            if (mentionRegExp.matches(mention)) {
                val command = searchCommand(args.removeAt(0)) ?: return

                if (command.category == CommandCategory.Developer && event.message.author?.id?.value?.toLong() != 477534823011844120L) return

                if (command.category == CommandCategory.Management) {
                    val member = event.message.getAuthorAsMemberOrNull() ?: return
                    val permissions = member.getPermissions()

                    if (
                        !permissions.contains(Permission.ManageGuild) &&
                        !permissions.contains(Permission.Administrator)
                    ) {
                        event.message.reply {
                            content = "Você não tem permissão para executar este comando"
                        }
                        return
                    }
                }

                command.run(event, args.toTypedArray())
            }
        } catch (_: Exception) {} // TODO
    }
}