package me.igorunderplayer.kono.commands.text.dev

import dev.kord.core.behavior.reply
import dev.kord.core.event.message.MessageCreateEvent
import me.igorunderplayer.kono.commands.BaseCommand
import me.igorunderplayer.kono.commands.CommandCategory
import me.igorunderplayer.kono.data.repositories.CardInstanceRepository
import me.igorunderplayer.kono.data.repositories.CardRepository
import me.igorunderplayer.kono.data.repositories.UserRepository
import me.igorunderplayer.kono.domain.card.CardType
import me.igorunderplayer.kono.services.UserService
import me.igorunderplayer.kono.utils.getMentionedUser

class GiveCommand(
    private val userService: UserService,
    private val userRepository: UserRepository,
    private val cardRepository: CardRepository,
    private val cardInstanceRepository: CardInstanceRepository
) : BaseCommand(
    name = "give",
    description = "give konos, essence or items to a user",
    category = CommandCategory.Developer
) {

    override suspend fun run(event: MessageCreateEvent, args: Array<String>) {
        if (args.size < 3) {
            event.message.reply {
                content = "use: give <@user|id> <konos|essence|item> <amount|item_id> [amount]"
            }
            return
        }

        val targetDiscordUser = getMentionedUser(event.message, args)
        if (targetDiscordUser == null) {
            event.message.reply { content = "invalid target user. use a mention or discord id." }
            return
        }

        val targetDiscordId = targetDiscordUser.id.value.toLong()

        val targetUser = userService.getOrCreateUserByDiscordId(targetDiscordId)
        if (targetUser == null) {
            event.message.reply { content = "failed to load target user." }
            return
        }

        when (args[1].lowercase()) {
            "kono", "konos" -> giveKonos(event, targetUser.id, targetDiscordId, targetUser.konos, args[2])
            "essence", "essences" -> giveEssence(event, targetUser.id, targetDiscordId, targetUser.essence, args[2])
            "item", "items", "equipment", "equip" -> giveItem(event, targetUser.id, targetDiscordId, args)
            else -> {
                event.message.reply {
                    content = "invalid type. use konos, essence or item."
                }
            }
        }
    }

    private suspend fun giveKonos(
        event: MessageCreateEvent,
        targetUserId: Int,
        targetDiscordId: Long,
        currentKonos: Int,
        amountRaw: String
    ) {
        val amount = amountRaw.toIntOrNull()
        if (amount == null || amount <= 0) {
            event.message.reply { content = "amount must be a positive number." }
            return
        }

        val newBalance = currentKonos + amount
        val updated = userRepository.updateKonos(targetUserId, newBalance)

        if (!updated) {
            event.message.reply { content = "failed to give konos." }
            return
        }

        event.message.reply {
            content = "gave $amount konos to <@$targetDiscordId>. new balance: $newBalance"
        }
    }

    private suspend fun giveEssence(
        event: MessageCreateEvent,
        targetUserId: Int,
        targetDiscordId: Long,
        currentEssence: Int,
        amountRaw: String
    ) {
        val amount = amountRaw.toIntOrNull()
        if (amount == null || amount <= 0) {
            event.message.reply { content = "amount must be a positive number." }
            return
        }

        val newBalance = currentEssence + amount
        val updated = userRepository.updateEssence(targetUserId, newBalance)

        if (!updated) {
            event.message.reply { content = "failed to give essence." }
            return
        }

        event.message.reply {
            content = "gave $amount essence to <@$targetDiscordId>. new balance: $newBalance"
        }
    }

    private suspend fun giveItem(
        event: MessageCreateEvent,
        targetUserId: Int,
        targetDiscordId: Long,
        args: Array<String>
    ) {
        val definitionId = args[2]
        val quantity = args.getOrNull(3)?.toIntOrNull() ?: 1

        if (quantity <= 0) {
            event.message.reply { content = "amount must be a positive number." }
            return
        }

        val definition = cardRepository.getDefinition(definitionId)
            ?: cardRepository.getDefinition(definitionId.uppercase())

        if (definition == null) {
            event.message.reply { content = "item '$definitionId' not found." }
            return
        }

        if (definition.type != CardType.EQUIPMENT) {
            event.message.reply { content = "'$definitionId' is not an item/equipment card." }
            return
        }

        repeat(quantity) {
            val inserted = cardInstanceRepository.insert(
                userId = targetUserId,
                definitionId = definition.id
            )

            if (!inserted) {
                event.message.reply { content = "failed while giving items." }
                return
            }
        }

        event.message.reply {
            content = "gave $quantity x ${definition.name} (${definition.id}) to <@$targetDiscordId>."
        }
    }

}

