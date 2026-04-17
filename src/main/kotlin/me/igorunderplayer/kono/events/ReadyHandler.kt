package me.igorunderplayer.kono.events.handlers

import dev.kord.common.entity.PresenceStatus
import dev.kord.core.Kord
import dev.kord.core.event.gateway.ReadyEvent
import kotlinx.coroutines.delay
import me.igorunderplayer.kono.commands.CommandManager
import me.igorunderplayer.kono.data.repositories.CardInstanceRepository
import me.igorunderplayer.kono.data.repositories.EquippedCardsRepository
import me.igorunderplayer.kono.data.repositories.UserRepository
import me.igorunderplayer.kono.services.EmojiService
import kotlin.time.Duration.Companion.seconds

class ReadyHandler(
    private val kord: Kord,
    private val commandManager: CommandManager,
    private val emojiService: EmojiService,
    private val userRepository: UserRepository,
    private val cardInstanceRepository: CardInstanceRepository,
    private val equippedCardsRepository: EquippedCardsRepository
) {

    companion object {
        private const val KONO_DEFINITION_ID = "KONO"
        private const val KONO_SIGNATURE_ITEM_DEFINITION_ID = "UNDEFINED"
        private const val SIGNATURE_ITEM_SLOT = 0
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

        // Optional bot flavor: keep KONO with her signature item in slot 0 when available.
        var signatureItem = instances.firstOrNull {
            it.definitionId.equals(KONO_SIGNATURE_ITEM_DEFINITION_ID, ignoreCase = true)
        }

        if (signatureItem == null) {
            val inserted = cardInstanceRepository.insert(user.id, KONO_SIGNATURE_ITEM_DEFINITION_ID)
            if (!inserted) return

            instances = cardInstanceRepository.getByDiscordId(botDiscordId)
            signatureItem = instances.firstOrNull {
                it.definitionId.equals(KONO_SIGNATURE_ITEM_DEFINITION_ID, ignoreCase = true)
            } ?: return
        }

        val currentSlotItemId = equippedCardsRepository.findCardInstanceIdByCharacterAndSlot(
            characterId = konoInstance.id,
            slot = SIGNATURE_ITEM_SLOT
        )

        if (currentSlotItemId != signatureItem.id && !equippedCardsRepository.existsByCardInstanceId(signatureItem.id)) {
            equippedCardsRepository.deleteByCharacterAndSlot(konoInstance.id, SIGNATURE_ITEM_SLOT)
            equippedCardsRepository.insert(konoInstance.id, signatureItem.id, SIGNATURE_ITEM_SLOT)
        }
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
