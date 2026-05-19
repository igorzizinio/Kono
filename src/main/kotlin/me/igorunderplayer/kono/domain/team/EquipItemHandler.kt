package me.igorunderplayer.kono.domain.team

import me.igorunderplayer.kono.data.repositories.CardInstanceRepository
import me.igorunderplayer.kono.data.repositories.EquippedCardsRepository
import me.igorunderplayer.kono.data.repositories.UserRepository
import me.igorunderplayer.kono.domain.card.EquipmentSlot

class EquipItemHandler(
    private val userRepository: UserRepository,
    private val cardInstanceRepository: CardInstanceRepository,
    private val equippedCardsRepository: EquippedCardsRepository
) {

    sealed class Result {
        data class Success(val slot: EquipmentSlot, val replaced: Boolean) : Result()
        data object InvalidSlot : Result()
        data object NoActiveCharacter : Result()
        data object InvalidItem : Result()
        data object ItemAlreadyEquipped : Result()
    }

    suspend fun execute(discordId: Long, itemInstanceId: Int): Result {
        val user = userRepository.getUserByDiscordId(discordId)
            ?: return Result.NoActiveCharacter

        val activeCharacterId = user.activeCharacterInstanceId
            ?: return Result.NoActiveCharacter

        val (_, definition) = cardInstanceRepository.getOwnedEquipmentWithDefinition(user.id, itemInstanceId)
            ?: return Result.InvalidItem

        val equipSlot = definition.slot ?: return Result.InvalidSlot

        val alreadyEquipped = equippedCardsRepository.existsByCardInstanceId(itemInstanceId)
        if (alreadyEquipped) return Result.ItemAlreadyEquipped

        val replaced = equippedCardsRepository.deleteByCharacterAndSlot(activeCharacterId, equipSlot.index)
        equippedCardsRepository.insert(activeCharacterId, itemInstanceId, equipSlot.index)

        return Result.Success(slot = equipSlot, replaced = replaced)
    }
}
