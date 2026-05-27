package me.igorunderplayer.kono.domain.team

import me.igorunderplayer.kono.data.repositories.CardInstanceRepository
import me.igorunderplayer.kono.data.repositories.EquippedCardsRepository
import me.igorunderplayer.kono.data.repositories.EquippedItemView
import me.igorunderplayer.kono.data.repositories.UserRepository
import me.igorunderplayer.kono.domain.gameplay.Unit

class BuildUnitHandler(
    private val userRepository: UserRepository,
    private val cardInstanceRepository: CardInstanceRepository,
    private val equippedCardsRepository: EquippedCardsRepository
) {

    sealed class Result {
        data class Success(
            val unit: Unit,
            val level: Int,
            val characterInstanceId: Int,
            val equippedItems: List<EquippedItemView>
        ) : Result()
        object UserNotFound : Result()
        object NoActiveCard : Result()
        data class CharacterNotFound(val activeCharacterId: Int) : Result()
    }

    suspend fun executeByDiscordId(userId: Long): Result {
        val userRow = userRepository.getUserByDiscordId(userId)
            ?: return Result.UserNotFound

        val activeCharacterId = userRow.activeCharacterInstanceId
            ?: return Result.NoActiveCard

        val character = cardInstanceRepository.getOwnedCharacterWithDefinition(userRow.id, activeCharacterId)
            ?: return Result.CharacterNotFound(activeCharacterId)

        val (charInst, charDef) = character

        val equips = equippedCardsRepository.getEquippedDefinitionsForCharacter(charInst.id)
        val equippedItems = cardInstanceRepository.getEquippedItemsForActiveCharacter(userId)

        return Result.Success(
            unit = CombatUnitFactory.createUnit(
                unitId = charInst.id.toString(),
                definition = charDef,
                level = charInst.level,
                equips = equips
            ),
            level = charInst.level,
            characterInstanceId = charInst.id,
            equippedItems = equippedItems
        )
    }
}
