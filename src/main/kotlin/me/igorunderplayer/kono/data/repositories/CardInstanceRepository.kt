package me.igorunderplayer.kono.data.repositories

import me.igorunderplayer.kono.data.DatabaseManager
import me.igorunderplayer.kono.data.entities.CardInstances
import org.ktorm.database.Database
import org.ktorm.dsl.insert

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
}
