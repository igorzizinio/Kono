package me.igorunderplayer.kono.data.repositories

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.igorunderplayer.kono.data.DatabaseManager
import me.igorunderplayer.kono.data.entities.User
import me.igorunderplayer.kono.data.entities.Users
import org.ktorm.database.Database
import org.ktorm.dsl.and
import org.ktorm.dsl.eq
import org.ktorm.dsl.insertAndGenerateKey
import org.ktorm.dsl.isNull
import org.ktorm.dsl.less
import org.ktorm.dsl.or
import org.ktorm.dsl.update
import org.ktorm.entity.find
import org.ktorm.entity.sequenceOf
import java.time.Instant

class UserRepository(private val databaseManager: DatabaseManager)  {

    private val database: Database
        get() = databaseManager.db


    suspend fun getUserById(userId: Int): User? = withContext(Dispatchers.IO) {
        database.sequenceOf(Users).find { it.id eq userId }
    }

    suspend fun getUserByDiscordId(discordId: Long): User? = withContext(Dispatchers.IO) {
        database.sequenceOf(Users).find { it.discordId eq discordId }
    }

    suspend fun createUser(discordId: Long): User? = withContext(Dispatchers.IO) {
        val generatedId = database.insertAndGenerateKey(Users) {
            set(it.discordId, discordId)
        } as Int

        return@withContext getUserById(generatedId)
    }

    suspend fun assignRiotAccountToUser(userId: Int, riotPuuid: String, riotRegion: String): Boolean = withContext(Dispatchers.IO) {
        database.update(Users) {
            set(it.riotPuuid, riotPuuid)
            set(it.riotRegion, riotRegion)
            where { it.id eq userId }
        } > 0
    }

    suspend fun updateDaily(
        userId: Int,
        money: Int,
        streak: Int,
        claimedAt: Instant,
        currentReset: Instant
    ): Boolean = withContext(Dispatchers.IO) {
        val rowsUpdated = database.update(Users) {
            set(it.essence, money)
            set(it.dailyStreak, streak)
            set(it.dailyRewardClaimedAt, claimedAt)

            where {
                (it.id eq userId) and (
                        it.dailyRewardClaimedAt.isNull() or
                                (it.dailyRewardClaimedAt less currentReset)
                        )
            }
        }

        rowsUpdated > 0
    }

    suspend fun updateWork(
        userId: Int,
        konos: Int,
        workedAt: Instant
    ) = withContext(Dispatchers.IO) {
        val updated = database.update(Users) {
            set(it.konos, konos)
            set(it.lastWorkAt, workedAt)
            where { it.id eq userId }
        }

        updated > 0
    }

    suspend fun updateEssence(userId: Int, essence: Int) = withContext(Dispatchers.IO) {
        val updated = database.update(Users) {
            set(it.essence, essence)
            where { it.id eq userId }
        }

        updated > 0
    }
}
