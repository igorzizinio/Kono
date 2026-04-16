package me.igorunderplayer.kono.domain.team

import me.igorunderplayer.kono.data.repositories.CardInstanceRepository
import me.igorunderplayer.kono.data.repositories.EquippedCardsRepository

class UnequipItemHandler(
    private val cardInstanceRepository: CardInstanceRepository,
    private val equippedCardsRepository: EquippedCardsRepository
) {

    sealed class Result {
        data class Success(val itemInstanceId: Int, val slot: Int) : Result()
        data class InvalidSlot(val slot: Int) : Result()
        object NoActiveCharacter : Result()
        object EmptySlot : Result()
    }

    suspend fun execute(discordId: Long, slot: Int): Result {
        if (slot !in 0..2) return Result.InvalidSlot(slot)

        val characterId = cardInstanceRepository.getActiveCharacterId(discordId)
            ?: return Result.NoActiveCharacter

        val itemInstanceId = equippedCardsRepository.findCardInstanceIdByCharacterAndSlot(characterId, slot)
            ?: return Result.EmptySlot

        equippedCardsRepository.deleteByCharacterAndSlot(characterId, slot)

        return Result.Success(itemInstanceId, slot)
    }
}
