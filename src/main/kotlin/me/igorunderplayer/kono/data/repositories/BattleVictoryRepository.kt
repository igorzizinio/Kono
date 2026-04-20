package me.igorunderplayer.kono.data.repositories

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.igorunderplayer.kono.data.DatabaseManager
import me.igorunderplayer.kono.data.entities.BattleVictories
import org.ktorm.database.Database
import org.ktorm.dsl.*
import java.time.Instant

class BattleVictoryRepository(
    private val databaseManager: DatabaseManager
) {
    private val database: Database
        get() = databaseManager.db

    suspend fun hasVictory(userId: Int, enemyId: String): Boolean = withContext(Dispatchers.IO) {
        database.from(BattleVictories)
            .select(BattleVictories.id)
            .where {
                (BattleVictories.userId eq userId) and (BattleVictories.enemyId eq enemyId)
            }
            .totalRecordsInAllPages > 0
    }

    suspend fun registerVictory(userId: Int, enemyId: String, essenceReward: Int): Boolean = withContext(Dispatchers.IO) {
        if (hasVictory(userId, enemyId)) return@withContext false

        database.insert(BattleVictories) {
            set(it.userId, userId)
            set(it.enemyId, enemyId)
            set(it.essenceReward, essenceReward)
            set(it.firstWonAt, Instant.now())
        } > 0
    }
}


