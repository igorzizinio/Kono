package me.igorunderplayer.kono.domain.team

import me.igorunderplayer.kono.data.repositories.CardInstanceRepository
import me.igorunderplayer.kono.data.repositories.EquippedCardsRepository
import me.igorunderplayer.kono.domain.card.EquipmentSlot

class UnequipItemHandler(
    private val cardInstanceRepository: CardInstanceRepository,
    private val equippedCardsRepository: EquippedCardsRepository
) {

    sealed class Result {
        data class Success(val itemInstanceId: Int, val slot: EquipmentSlot) : Result()
        data class InvalidSlot(val input: String) : Result()
        data object NoActiveCharacter : Result()
        data object EmptySlot : Result()
    }

    suspend fun execute(discordId: Long, slotInput: String): Result {
        val equipSlot = EquipmentSlot.fromInput(slotInput)
            ?: return Result.InvalidSlot(slotInput)

        val characterId = cardInstanceRepository.getActiveCharacterId(discordId)
            ?: return Result.NoActiveCharacter

        val itemInstanceId = equippedCardsRepository.findCardInstanceIdByCharacterAndSlot(characterId, equipSlot.index)
            ?: return Result.EmptySlot

        equippedCardsRepository.deleteByCharacterAndSlot(characterId, equipSlot.index)

        return Result.Success(itemInstanceId, equipSlot)
    }
}
