package me.igorunderplayer.kono.commands

import dev.kord.common.entity.ApplicationCommandType
import dev.kord.common.entity.Permission
import dev.kord.common.entity.optional.Optional
import dev.kord.core.Kord
import dev.kord.core.behavior.reply
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.rest.json.request.ApplicationCommandCreateRequest
import org.slf4j.LoggerFactory

enum class CommandCategory {
    Util,
    Misc,
    Management,
    LoL,
    Developer,
    Other
}

class CommandManager(
    private val kord: Kord,
    private val commands: List<BaseCommand>,
    private val slashCommands: List<KonoSlashCommand>
) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    val commandList = mutableListOf<BaseCommand>()
    private val applicationCommandList = mutableListOf<KonoSlashCommand>()

    fun start() {
        commands.forEach { registerCommand(it) }
        slashCommands.forEach { registerSlashCommand(it) }
    }

    private fun registerCommand(command: BaseCommand) {
        val exists = commandList.any {
            it.name.equals(command.name, ignoreCase = true)
        }

        if (exists) {
            logger.error("\u001b[31m ${command.name} already registered \u001b[0m")
            return
        }

        commandList.add(command)
    }

    private fun registerSlashCommand(command: KonoSlashCommand) {
        val exists = applicationCommandList.any {
            it.name.equals(command.name, ignoreCase = true)
        }

        if (exists) {
            logger.error("\u001b[31m ${command.name} already registered \u001b[0m")
            return
        }

        applicationCommandList.add(command)
    }

    suspend fun registerCommands() {
        kord.rest.interaction.createGlobalApplicationCommands(
            kord.selfId,
            applicationCommandList.map {
                ApplicationCommandCreateRequest(
                    name = it.name,
                    description = Optional(it.description),
                    type = ApplicationCommandType.ChatInput,
                    options = Optional(it.options)
                )
            }
        )
    }

    fun searchCommand(search: String): BaseCommand? {
        val lower = search.lowercase()

        return commandList.find {
            it.name == lower || it.aliases.contains(lower)
        }
    }

    suspend fun handleChatInputCommand(event: ChatInputCommandInteractionCreateEvent) {
        val cmd = applicationCommandList.find {
            it.name == event.interaction.command.rootName
        }

        cmd?.run(event)
    }

    suspend fun handleCommand(event: MessageCreateEvent) {
        val mentionRegex = Regex("^<@!?${event.kord.selfId}>$")

        try {
            val args = event.message.content
                .trim()
                .split(' ')
                .toMutableList()

            val mention = args.removeAt(0)
            if (mentionRegex.matches(mention)) {

                val command = searchCommand(args.removeAt(0)) ?: return

                // DEV check
                if (
                    command.category == CommandCategory.Developer &&
                    event.message.author?.id?.value != 477534823011844120u
                ) return

                // PERMISSION check
                if (command.category == CommandCategory.Management) {
                    val member = event.message.getAuthorAsMemberOrNull() ?: return
                    val permissions = member.getPermissions()

                    if (
                        !permissions.contains(Permission.ManageGuild) &&
                        !permissions.contains(Permission.Administrator)
                    ) {
                        event.message.reply {
                            content = "Você não tem permissão"
                        }
                        return
                    }
                }
                command.run(event, args.toTypedArray())
            }

        } catch (exception: Exception) {
            logger.error("Error while handling command", exception)
            event.message.reply {
                content = "\uD83D\uDE2D algo de errado aconteceu ao executar o comando"
            }
        }
    }
}
