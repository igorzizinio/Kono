package me.igorunderplayer.kono.commands

import dev.kord.common.entity.Permission
import dev.kord.core.Kord
import dev.kord.core.behavior.reply
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import dev.kord.core.event.message.MessageCreateEvent
import me.igorunderplayer.kono.commands.slash.testing.DestinoCommand
import me.igorunderplayer.kono.commands.text.lol.LoLChampion
import me.igorunderplayer.kono.commands.text.lol.LoLMatches
import me.igorunderplayer.kono.commands.text.testing.*
import org.slf4j.LoggerFactory

enum class CommandCategory {
    Util,
    Misc,
    Other,
    Management,
    LoL
}

class CommandManager(private val kord: Kord)  {
    private val logger = LoggerFactory.getLogger(this::class.java)
    val commandList = mutableListOf<BaseCommand>()
    private val applicationCommandList = mutableListOf<KonoSlashCommand>()

    suspend fun start() {
        registerCommand(Avatar())
        registerCommand(Info())
        registerCommand(Help())

        registerCommand(Profile())
        registerCommand(BorderGradient())
        registerCommand(Clear())

        registerCommand(LoLChampion())
        registerCommand(LoLMatches())

        registerCommand(DestinoTextCommand())


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

    private suspend fun registerSlashCommand(command: KonoSlashCommand) {
        val commandFound = applicationCommandList.any {
            it.name.lowercase() == command.name.lowercase()
        }

        if (commandFound) {
            val red = "\u001b[31m"
            val reset = "\u001b[0m"

            logger.error("$red ${command.name} slash command is already registered, skipped! $reset")
            return
        }

        command.setup(kord)
        applicationCommandList.add(command)
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