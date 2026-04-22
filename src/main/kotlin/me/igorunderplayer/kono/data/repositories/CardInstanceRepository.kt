@file:Suppress("unused")

package me.igorunderplayer.kono.data.repositories

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.igorunderplayer.kono.data.DatabaseManager
import me.igorunderplayer.kono.data.entities.CardInstance
import me.igorunderplayer.kono.data.entities.CardInstances
import me.igorunderplayer.kono.data.entities.EquippedCards
import me.igorunderplayer.kono.data.entities.Users
import me.igorunderplayer.kono.domain.card.CardCatalog
import me.igorunderplayer.kono.domain.card.CardDefinition
import me.igorunderplayer.kono.domain.card.CardType
import me.igorunderplayer.kono.domain.card.Rarity
import org.ktorm.database.Database
import org.ktorm.dsl.*
import org.ktorm.entity.filter
import org.ktorm.entity.find
import org.ktorm.entity.sequenceOf
import org.ktorm.entity.toList

data class EquippedItemView(
    val slot: Int,
    val cardInstanceId: Int,
    val definitionId: String,
    val name: String,
    val rarity: Rarity,
    val type: CardType
)

class CardInstanceRepository(
    private val databaseManager: DatabaseManager
) {

    private val database: Database
        get() = databaseManager.db

    fun insert(userId: Int, definitionId: String): Boolean {
        val inserted = database.insert(CardInstances) {
            set(it.userId, userId)
            set(it.definitionId, definitionId)
            set(it.level, 1)
            set(it.upgraded, false)
        }

        return inserted > 0
    }

    suspend fun getByDiscordId(discordId: Long): List<CardInstance> = withContext(Dispatchers.IO) {
        val user = database.sequenceOf(Users)
            .find { it.discordId eq discordId }
            ?: return@withContext emptyList()

        database.sequenceOf(CardInstances)
            .filter { it.userId eq user.id }
            .toList()
    }

    suspend fun getActiveCharacterId(discordId: Long): Int? = withContext(Dispatchers.IO) {
        val user = database.sequenceOf(Users)
            .find { it.discordId eq discordId }
            ?: return@withContext null

        user.activeCharacterInstanceId
    }

    suspend fun getEquippedSlotsForActiveCharacter(discordId: Long): Set<Int> = withContext(Dispatchers.IO) {
        val characterId = getActiveCharacterId(discordId)
            ?: return@withContext emptySet()

        database.from(EquippedCards)
            .select(EquippedCards.slot)
            .where { EquippedCards.characterInstanceId eq characterId }
            .map { it[EquippedCards.slot]!! }
            .toSet()
    }

    suspend fun getEquippedItemsForActiveCharacter(discordId: Long): List<EquippedItemView> =
        withContext(Dispatchers.IO) {
            val characterId = getActiveCharacterId(discordId)
                ?: return@withContext emptyList()

            database.from(EquippedCards)
                .innerJoin(CardInstances, on = EquippedCards.cardInstanceId eq CardInstances.id)
                .select(
                    EquippedCards.slot,
                    EquippedCards.cardInstanceId,
                    CardInstances.definitionId
                )
                .where { EquippedCards.characterInstanceId eq characterId }
                .mapNotNull { row ->
                    val definitionId = row[CardInstances.definitionId] ?: return@mapNotNull null
                    val definition = CardCatalog.getById(definitionId) ?: return@mapNotNull null

                    EquippedItemView(
                        slot = row[EquippedCards.slot]!!,
                        cardInstanceId = row[EquippedCards.cardInstanceId]!!,
                        definitionId = definition.id,
                        name = definition.name,
                        rarity = definition.rarity,
                        type = definition.type
                    )
                }
                .sortedBy { it.slot }
        }

    suspend fun isOwnedEquipmentInstance(userId: Int, instanceId: Int): Boolean = withContext(Dispatchers.IO) {
        val instance = database.sequenceOf(CardInstances)
            .find {
                (it.id eq instanceId) and (it.userId eq userId)
            } ?: return@withContext false

        val definition = CardCatalog.getById(instance.definitionId) ?: return@withContext false
        definition.type == CardType.EQUIPMENT
    }

    suspend fun getOwnedCharacterWithDefinition(userId: Int, instanceId: Int): Pair<CardInstance, CardDefinition>? =
        withContext(Dispatchers.IO) {
            val instance = database.sequenceOf(CardInstances)
                .find {
                    (it.userId eq userId) and (it.id eq instanceId)
                } ?: return@withContext null

            val definition = CardCatalog.getById(instance.definitionId)
                ?: return@withContext null

            if (definition.type != CardType.CHARACTER) return@withContext null

            instance to definition
        }

    suspend fun getOwnedEquipmentWithDefinition(userId: Int, instanceId: Int): Pair<CardInstance, CardDefinition>? =
        withContext(Dispatchers.IO) {
            val instance = database.sequenceOf(CardInstances)
                .find {
                    (it.userId eq userId) and (it.id eq instanceId)
                } ?: return@withContext null

            val definition = CardCatalog.getById(instance.definitionId)
                ?: return@withContext null

            if (definition.type != CardType.EQUIPMENT) return@withContext null

            instance to definition
        }

    suspend fun getOwnedEquipmentsWithDefinition(userId: Int): List<Pair<CardInstance, CardDefinition>> =
        withContext(Dispatchers.IO) {
            database.sequenceOf(CardInstances)
                .filter { it.userId eq userId }
                .toList()
                .mapNotNull { instance ->
                    val definition = CardCatalog.getById(instance.definitionId) ?: return@mapNotNull null
                    if (definition.type != CardType.EQUIPMENT) return@mapNotNull null
                    instance to definition
                }
        }

    suspend fun countOwnedDefinitionInstances(userId: Int, definitionId: String): Int = withContext(Dispatchers.IO) {
        database.from(CardInstances)
            .select(CardInstances.id)
            .where {
                (CardInstances.userId eq userId) and
                        (CardInstances.definitionId eq definitionId)
            }
            .totalRecordsInAllPages
    }

    suspend fun consumeDefinitionCopies(
        userId: Int,
        definitionId: String,
        exceptInstanceId: Int,
        amount: Int
    ): Int = withContext(Dispatchers.IO) {
        if (amount <= 0) return@withContext 0

        val idsToDelete = database.from(CardInstances)
            .select(CardInstances.id)
            .where {
                (CardInstances.userId eq userId) and
                        (CardInstances.definitionId eq definitionId) and
                        (CardInstances.id notEq exceptInstanceId)
            }
            .orderBy(CardInstances.id.asc())
            .limit(amount)
            .mapNotNull { it[CardInstances.id] }

        if (idsToDelete.size < amount) return@withContext 0

        var deleted = 0
        idsToDelete.forEach { id ->
            deleted += database.delete(CardInstances) { it.id eq id }
        }

        deleted
    }

    suspend fun countUnequippedDefinitionCopiesForUpgrade(
        userId: Int,
        definitionId: String,
        exceptInstanceId: Int
    ): Int = withContext(Dispatchers.IO) {
        database.from(CardInstances)
            .leftJoin(EquippedCards, on = CardInstances.id eq EquippedCards.cardInstanceId)
            .select(CardInstances.id)
            .where {
                (CardInstances.userId eq userId) and
                        (CardInstances.definitionId eq definitionId) and
                        (CardInstances.id notEq exceptInstanceId) and
                        (EquippedCards.id.isNull())
            }
            .totalRecordsInAllPages
    }

    suspend fun consumeUnequippedDefinitionCopies(
        userId: Int,
        definitionId: String,
        exceptInstanceId: Int,
        amount: Int
    ): Int = withContext(Dispatchers.IO) {
        if (amount <= 0) return@withContext 0

        val idsToDelete = database.from(CardInstances)
            .leftJoin(EquippedCards, on = CardInstances.id eq EquippedCards.cardInstanceId)
            .select(CardInstances.id)
            .where {
                (CardInstances.userId eq userId) and
                        (CardInstances.definitionId eq definitionId) and
                        (CardInstances.id notEq exceptInstanceId) and
                        (EquippedCards.id.isNull())
            }
            .orderBy(CardInstances.id.asc())
            .limit(amount)
            .mapNotNull { it[CardInstances.id] }

        if (idsToDelete.size < amount) return@withContext 0

        var deleted = 0
        idsToDelete.forEach { id ->
            deleted += database.delete(CardInstances) { it.id eq id }
        }

        deleted
    }

    suspend fun updateCharacterLevel(instanceId: Int, newLevel: Int): Boolean = withContext(Dispatchers.IO) {
        database.update(CardInstances) {
            set(it.level, newLevel)
            set(it.upgraded, newLevel > 1)
            where { it.id eq instanceId }
        } > 0
    }

    suspend fun updateEquipmentLevel(instanceId: Int, newLevel: Int): Boolean = withContext(Dispatchers.IO) {
        database.update(CardInstances) {
            set(it.level, newLevel)
            set(it.upgraded, newLevel > 1)
            where { it.id eq instanceId }
        } > 0
    }

    suspend fun deleteOwnedEquipmentInstance(userId: Int, instanceId: Int): Boolean = withContext(Dispatchers.IO) {
        val equipment = getOwnedEquipmentWithDefinition(userId, instanceId)
            ?: return@withContext false

        database.delete(CardInstances) {
            (it.id eq equipment.first.id) and (it.userId eq userId)
        } > 0
    }

}
