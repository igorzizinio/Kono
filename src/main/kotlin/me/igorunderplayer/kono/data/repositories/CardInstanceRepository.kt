@file:Suppress("unused")

package me.igorunderplayer.kono.data.repositories

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.igorunderplayer.kono.data.DatabaseManager
import me.igorunderplayer.kono.data.entities.CardDefinitions
import me.igorunderplayer.kono.data.entities.CardInstance
import me.igorunderplayer.kono.data.entities.CardInstances
import me.igorunderplayer.kono.data.entities.EquippedCards
import me.igorunderplayer.kono.data.entities.Users
import me.igorunderplayer.kono.domain.card.CardType
import me.igorunderplayer.kono.domain.card.Rarity
import org.ktorm.database.Database
import org.ktorm.dsl.*
import org.ktorm.entity.filter
import org.ktorm.entity.find
import org.ktorm.entity.sequenceOf
import org.ktorm.entity.toList
import kotlin.collections.emptyList

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

    suspend fun getEquippedItemsForActiveCharacter(discordId: Long): List<EquippedItemView> = withContext(Dispatchers.IO) {
        val characterId = getActiveCharacterId(discordId)
            ?: return@withContext emptyList()

        database.from(EquippedCards)
            .innerJoin(CardInstances, on = EquippedCards.cardInstanceId eq CardInstances.id)
            .innerJoin(CardDefinitions, on = CardInstances.definitionId eq CardDefinitions.id)
            .select(
                EquippedCards.slot,
                EquippedCards.cardInstanceId,
                CardDefinitions.id,
                CardDefinitions.name,
                CardDefinitions.rarity,
                CardDefinitions.type
            )
            .where { EquippedCards.characterInstanceId eq characterId }
            .map { row ->
                EquippedItemView(
                    slot = row[EquippedCards.slot]!!,
                    cardInstanceId = row[EquippedCards.cardInstanceId]!!,
                    definitionId = row[CardDefinitions.id]!!,
                    name = row[CardDefinitions.name]!!,
                    rarity = row[CardDefinitions.rarity]!!,
                    type = row[CardDefinitions.type]!!
                )
            }
            .sortedBy { it.slot }
    }

}
