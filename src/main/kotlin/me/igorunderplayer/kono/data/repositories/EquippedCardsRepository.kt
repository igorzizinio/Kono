package me.igorunderplayer.kono.data.repositories

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.igorunderplayer.kono.data.DatabaseManager
import me.igorunderplayer.kono.data.entities.CardInstances
import me.igorunderplayer.kono.data.entities.EquippedCard
import me.igorunderplayer.kono.data.entities.EquippedCards
import me.igorunderplayer.kono.domain.card.CardCatalog
import me.igorunderplayer.kono.domain.card.CardDefinition
import org.ktorm.dsl.*

data class EquippedDefinitionWithLevel(
    val definition: CardDefinition,
    val level: Int,
    val slot: Int
)

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
            .select(EquippedCards.slot, CardInstances.definitionId)
            .where { EquippedCards.characterInstanceId eq characterId }
            .mapNotNull { row ->
                val definitionId = row[CardInstances.definitionId] ?: return@mapNotNull null
                val definition = CardCatalog.getById(definitionId) ?: return@mapNotNull null
                definition to (row[EquippedCards.slot] ?: Int.MAX_VALUE)
            }
            .sortedBy { it.second }
            .map { it.first }
    }

    suspend fun getEquippedDefinitionsWithLevelForCharacter(characterId: Int): List<EquippedDefinitionWithLevel> = withContext(Dispatchers.IO) {
        database.from(EquippedCards)
            .innerJoin(CardInstances, on = EquippedCards.cardInstanceId eq CardInstances.id)
            .select(EquippedCards.slot, CardInstances.definitionId, CardInstances.level)
            .where { EquippedCards.characterInstanceId eq characterId }
            .mapNotNull { row ->
                val definitionId = row[CardInstances.definitionId] ?: return@mapNotNull null
                val definition = CardCatalog.getById(definitionId) ?: return@mapNotNull null
                EquippedDefinitionWithLevel(
                    definition = definition,
                    level = row[CardInstances.level] ?: 1,
                    slot = row[EquippedCards.slot] ?: Int.MAX_VALUE
                )
            }
            .sortedBy { it.slot }
    }
}
