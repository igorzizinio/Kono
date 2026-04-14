package me.igorunderplayer.kono.data.repositories

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.igorunderplayer.kono.data.DatabaseManager
import me.igorunderplayer.kono.data.entities.CardInstance
import me.igorunderplayer.kono.data.entities.CardInstances
import me.igorunderplayer.kono.data.entities.Users
import org.ktorm.database.Database
import org.ktorm.dsl.*
import org.ktorm.entity.filter
import org.ktorm.entity.find
import org.ktorm.entity.sequenceOf
import org.ktorm.entity.toList
import kotlin.collections.emptyList

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
}
