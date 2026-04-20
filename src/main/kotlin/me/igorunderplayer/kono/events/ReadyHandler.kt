package me.igorunderplayer.kono.events.handlers

import dev.kord.common.entity.PresenceStatus
import dev.kord.core.Kord
import dev.kord.core.event.gateway.ReadyEvent
import kotlinx.coroutines.delay
import me.igorunderplayer.kono.commands.CommandManager
import me.igorunderplayer.kono.data.repositories.CardInstanceRepository
import me.igorunderplayer.kono.data.repositories.UserRepository
import me.igorunderplayer.kono.services.EmojiService
import kotlin.time.Duration.Companion.seconds

class ReadyHandler(
    private val kord: Kord,
    private val commandManager: CommandManager,
    private val emojiService: EmojiService,
    private val userRepository: UserRepository,
    private val cardInstanceRepository: CardInstanceRepository
) {

    companion object {
        private const val KONO_DEFINITION_ID = "KONO"
    }

    suspend fun handle(event: ReadyEvent) {
        emojiService.loadEmojis()
        commandManager.registerCommands()

        ensureBotKonoLoadout(event)

        println("Ready as ${event.kord.getSelf().username}")
        startPresenceLoop()
    }

    private suspend fun ensureBotKonoLoadout(event: ReadyEvent) {
        val botDiscordId = event.kord.getSelf().id.value.toLong()

        val user = userRepository.getUserByDiscordId(botDiscordId)
            ?: userRepository.createUser(botDiscordId)
            ?: return

        var instances = cardInstanceRepository.getByDiscordId(botDiscordId)

        var konoInstance = instances.firstOrNull {
            it.definitionId.equals(KONO_DEFINITION_ID, ignoreCase = true)
        }

        if (konoInstance == null) {
            val inserted = cardInstanceRepository.insert(user.id, KONO_DEFINITION_ID)
            if (!inserted) return

            instances = cardInstanceRepository.getByDiscordId(botDiscordId)
            konoInstance = instances.firstOrNull {
                it.definitionId.equals(KONO_DEFINITION_ID, ignoreCase = true)
            } ?: return
        }

        if (user.activeCharacterInstanceId != konoInstance.id) {
            userRepository.updateActiveCharacter(user.id, konoInstance.id)
        }

        cardInstanceRepository.updateCharacterLevel(konoInstance.id, 20)
    }

    private suspend fun startPresenceLoop() {
        val presences = listOf<suspend () -> Unit>(
            {
                kord.editPresence {
                    status = PresenceStatus.Online
                    listening("seus / comandos")
                }
            },
            {
                kord.editPresence {
                    status = PresenceStatus.Idle
                    playing("Kono Bot")
                }
            },
            {
                kord.editPresence {
                    status = PresenceStatus.DoNotDisturb
                    watching("os servidores")
                }
            }
        )

        var index = 0

        while (true) {
            presences[index]()
            index = (index + 1) % presences.size
            delay(30.seconds)
        }
    }
}
