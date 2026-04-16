package me.igorunderplayer.kono.domain.team

import me.igorunderplayer.kono.data.repositories.CardInstanceRepository
import me.igorunderplayer.kono.data.repositories.EquippedCardsRepository
import me.igorunderplayer.kono.data.repositories.UserRepository

class EquipItemHandler(
    private val userRepository: UserRepository,
    private val cardInstanceRepository: CardInstanceRepository,
    private val equippedCardsRepository: EquippedCardsRepository
) {

    sealed class Result {
        data class Success(val slot: Int) : Result()
        data class InvalidSlot(val slot: Int) : Result()
        object NoActiveCharacter : Result()
        object InvalidItem : Result()
        object ItemAlreadyEquipped : Result()
    }

    suspend fun execute(
        discordId: Long,
        itemInstanceId: Int,
        slot: Int
    ): Result {
        if (slot !in 0..2) return Result.InvalidSlot(slot)

        val user = userRepository.getUserByDiscordId(discordId)
            ?: return Result.NoActiveCharacter

        val activeCharacterId = user.activeCharacterInstanceId
            ?: return Result.NoActiveCharacter

        val isValidItem = cardInstanceRepository.isOwnedEquipmentInstance(user.id, itemInstanceId)
        if (!isValidItem) return Result.InvalidItem

        val alreadyEquipped = equippedCardsRepository.existsByCardInstanceId(itemInstanceId)
        if (alreadyEquipped) return Result.ItemAlreadyEquipped

        equippedCardsRepository.deleteByCharacterAndSlot(activeCharacterId, slot)
        equippedCardsRepository.insert(activeCharacterId, itemInstanceId, slot)

        return Result.Success(slot)
    }
}
