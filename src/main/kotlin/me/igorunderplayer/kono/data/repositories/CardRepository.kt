package me.igorunderplayer.kono.data.repositories

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.igorunderplayer.kono.data.DatabaseManager
import me.igorunderplayer.kono.data.entities.CardDefinition
import me.igorunderplayer.kono.data.entities.CardDefinitions
import me.igorunderplayer.kono.domain.card.CardType
import me.igorunderplayer.kono.domain.card.Rarity
import org.ktorm.database.Database
import org.ktorm.dsl.eq
import org.ktorm.entity.filter
import org.ktorm.entity.find
import org.ktorm.entity.sequenceOf
import org.ktorm.entity.toList

class CardRepository(
    private val databaseManager: DatabaseManager
) {
    private val database: Database
        get() = databaseManager.db

    suspend fun getDefinition(id: String): CardDefinition? = withContext(Dispatchers.IO) {
        database.sequenceOf(CardDefinitions)
            .find { it.id eq id }
    }

    suspend fun getAll(): List<CardDefinition> = withContext(Dispatchers.IO) {
        database.sequenceOf(CardDefinitions).toList()
    }

    suspend fun getByRarity(rarity: Rarity): List<CardDefinition> = withContext(Dispatchers.IO) {
        database.sequenceOf(CardDefinitions)
            .filter { it.rarity eq rarity }
            .toList()
    }

    suspend fun getByType(type: CardType): List<CardDefinition> = withContext(Dispatchers.IO) {
        database.sequenceOf(CardDefinitions)
            .filter { it.type eq type }
            .toList()
    }

    suspend fun getByName(name: String): CardDefinition? = withContext(Dispatchers.IO) {
        database.sequenceOf(CardDefinitions)
            .find { it.name eq name }
    }
}
