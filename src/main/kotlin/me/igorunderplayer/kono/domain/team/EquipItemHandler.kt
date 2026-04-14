package me.igorunderplayer.kono.domain.team

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.igorunderplayer.kono.data.DatabaseManager
import me.igorunderplayer.kono.data.entities.CardDefinitions
import me.igorunderplayer.kono.data.entities.CardInstances
import me.igorunderplayer.kono.data.entities.EquippedCards
import me.igorunderplayer.kono.data.entities.Users
import me.igorunderplayer.kono.domain.card.CardType
import org.ktorm.dsl.and
import org.ktorm.dsl.delete
import org.ktorm.dsl.eq
import org.ktorm.dsl.from
import org.ktorm.dsl.innerJoin
import org.ktorm.dsl.insert
import org.ktorm.dsl.map
import org.ktorm.dsl.select
import org.ktorm.dsl.where

class EquipItemHandler(
    private val databaseManager: DatabaseManager
) {

    private val database
        get() = databaseManager.db

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
    ): Result = withContext(Dispatchers.IO) {
        if (slot !in 0..2) return@withContext Result.InvalidSlot(slot)

        val user = database
            .from(Users)
            .select(Users.id, Users.activeCharacterInstanceId)
            .where { Users.discordId eq discordId }
            .map { row ->
                Pair(
                    row[Users.id]!!,
                    row[Users.activeCharacterInstanceId]
                )
            }
            .firstOrNull()
            ?: return@withContext Result.NoActiveCharacter

        val (userId, characterId) = user
        val activeCharacterId = characterId ?: return@withContext Result.NoActiveCharacter

        // valida item
        val isValidItem = database.from(CardInstances)
            .innerJoin(CardDefinitions, on = CardInstances.definitionId eq CardDefinitions.id)
            .select()
            .where {
                (CardInstances.id eq itemInstanceId) and
                        (CardInstances.userId eq userId) and
                        (CardDefinitions.type eq CardType.EQUIPMENT)
            }
            .totalRecordsInAllPages > 0

        if (!isValidItem) return@withContext Result.InvalidItem

        val alreadyEquipped = database.from(EquippedCards)
            .select(EquippedCards.id)
            .where { EquippedCards.cardInstanceId eq itemInstanceId }
            .totalRecordsInAllPages > 0

        if (alreadyEquipped) return@withContext Result.ItemAlreadyEquipped

        // remove item existente no slot
        database.delete(EquippedCards) {
            (it.characterInstanceId eq activeCharacterId) and (it.slot eq slot)
        }

        // adiciona novo
        database.insert(EquippedCards) {
            set(it.characterInstanceId, activeCharacterId)
            set(it.cardInstanceId, itemInstanceId)
            set(it.slot, slot)
        }

        Result.Success(slot)
    }
}
