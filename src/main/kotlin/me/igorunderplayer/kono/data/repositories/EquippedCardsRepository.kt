package me.igorunderplayer.kono.data.repositories

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.igorunderplayer.kono.data.DatabaseManager
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
}
