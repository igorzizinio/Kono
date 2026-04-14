package me.igorunderplayer.kono.domain.team

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.igorunderplayer.kono.data.DatabaseManager
import me.igorunderplayer.kono.data.entities.EquippedCards
import me.igorunderplayer.kono.data.entities.Users
import org.ktorm.dsl.delete
import org.ktorm.dsl.and
import org.ktorm.dsl.eq
import org.ktorm.dsl.from
import org.ktorm.dsl.map
import org.ktorm.dsl.select
import org.ktorm.dsl.where

class UnequipItemHandler(
    private val databaseManager: DatabaseManager
) {

    private val database
        get() = databaseManager.db

    sealed class Result {
        data class Success(val itemInstanceId: Int, val slot: Int) : Result()
        data class InvalidSlot(val slot: Int) : Result()
        object NoActiveCharacter : Result()
        object EmptySlot : Result()
    }

    suspend fun execute(discordId: Long, slot: Int): Result = withContext(Dispatchers.IO) {
        if (slot !in 0..2) return@withContext Result.InvalidSlot(slot)

        val characterId = database
            .from(Users)
            .select(Users.activeCharacterInstanceId)
            .where { Users.discordId eq discordId }
            .map { it[Users.activeCharacterInstanceId] }
            .firstOrNull()
            ?: return@withContext Result.NoActiveCharacter

        val itemInstanceId = database
            .from(EquippedCards)
            .select(EquippedCards.cardInstanceId)
            .where {
                (EquippedCards.characterInstanceId eq characterId) and
                        (EquippedCards.slot eq slot)
            }
            .map { it[EquippedCards.cardInstanceId] }
            .firstOrNull()
            ?: return@withContext Result.EmptySlot

        database.delete(EquippedCards) {
            (it.characterInstanceId eq characterId) and (it.slot eq slot)
        }

        Result.Success(itemInstanceId, slot)
    }
}


