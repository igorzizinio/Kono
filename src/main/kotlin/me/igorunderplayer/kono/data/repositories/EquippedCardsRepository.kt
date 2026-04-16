package me.igorunderplayer.kono.data.repositories

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.igorunderplayer.kono.data.DatabaseManager
import me.igorunderplayer.kono.data.entities.CardDefinition
import me.igorunderplayer.kono.data.entities.CardDefinitions
import me.igorunderplayer.kono.data.entities.CardInstances
import me.igorunderplayer.kono.data.entities.EquippedCard
import me.igorunderplayer.kono.data.entities.EquippedCards
import org.ktorm.dsl.*

class EquippedCardsRepository(
    private val databaseManager: DatabaseManager
) {
    private val database
        get() = databaseManager.db

    suspend fun getEquippedCardsForCharacter(characterId: Int): List<EquippedCard> = withContext(Dispatchers.IO) {
        database.from(EquippedCards)
            .select()
            .where {
                EquippedCards.characterInstanceId eq characterId
            }.map {
                EquippedCards.createEntity(it)
            }
    }

    suspend fun getEquippedCardsForUser(userId: Int): List<EquippedCard> = withContext(Dispatchers.IO) {
        database.from(EquippedCards)
            .innerJoin(CardInstances, on = EquippedCards.characterInstanceId eq CardInstances.id)
            .select()
            .where {
                CardInstances.userId eq userId
            }
            .map { row -> EquippedCards.createEntity(row) }
            .sortedWith(compareBy({ it.characterInstanceId }, { it.slot }))
    }

    suspend fun existsByCardInstanceId(cardInstanceId: Int): Boolean = withContext(Dispatchers.IO) {
        database.from(EquippedCards)
            .select(EquippedCards.id)
            .where { EquippedCards.cardInstanceId eq cardInstanceId }
            .totalRecordsInAllPages > 0
    }

    suspend fun findCardInstanceIdByCharacterAndSlot(characterId: Int, slot: Int): Int? = withContext(Dispatchers.IO) {
        database.from(EquippedCards)
            .select(EquippedCards.cardInstanceId)
            .where {
                (EquippedCards.characterInstanceId eq characterId) and
                        (EquippedCards.slot eq slot)
            }
            .map { it[EquippedCards.cardInstanceId] }
            .firstOrNull()
    }

    suspend fun deleteByCharacterAndSlot(characterId: Int, slot: Int): Boolean = withContext(Dispatchers.IO) {
        database.delete(EquippedCards) {
            (it.characterInstanceId eq characterId) and (it.slot eq slot)
        } > 0
    }

    suspend fun insert(characterId: Int, cardInstanceId: Int, slot: Int): Boolean = withContext(Dispatchers.IO) {
        database.insert(EquippedCards) {
            set(it.characterInstanceId, characterId)
            set(it.cardInstanceId, cardInstanceId)
            set(it.slot, slot)
        } > 0
    }

    suspend fun getEquippedDefinitionsForCharacter(characterId: Int): List<CardDefinition> = withContext(Dispatchers.IO) {
        database.from(EquippedCards)
            .innerJoin(CardInstances, on = EquippedCards.cardInstanceId eq CardInstances.id)
            .innerJoin(CardDefinitions, on = CardInstances.definitionId eq CardDefinitions.id)
            .select()
            .where { EquippedCards.characterInstanceId eq characterId }
            .map { row ->
                CardDefinitions.createEntity(row) to (row[EquippedCards.slot] ?: Int.MAX_VALUE)
            }
            .sortedBy { it.second }
            .map { it.first }
    }
}
