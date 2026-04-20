package me.igorunderplayer.kono.data.repositories

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.igorunderplayer.kono.data.DatabaseManager
import me.igorunderplayer.kono.data.entities.BattleTeamSlot
import me.igorunderplayer.kono.data.entities.BattleTeamSlots
import me.igorunderplayer.kono.data.entities.Users
import org.ktorm.database.Database
import org.ktorm.dsl.*
import org.ktorm.entity.find
import org.ktorm.entity.sequenceOf

class BattleTeamRepository(
    private val databaseManager: DatabaseManager
) {
    private val database: Database
        get() = databaseManager.db

    suspend fun getTeamByUserId(userId: Int): List<BattleTeamSlot> = withContext(Dispatchers.IO) {
        database.from(BattleTeamSlots)
            .select()
            .where { BattleTeamSlots.userId eq userId }
            .orderBy(BattleTeamSlots.slot.asc())
            .map { BattleTeamSlots.createEntity(it) }
    }

    suspend fun getTeamByDiscordId(discordId: Long): List<BattleTeamSlot> = withContext(Dispatchers.IO) {
        val user = database.sequenceOf(Users).find { it.discordId eq discordId }
            ?: return@withContext emptyList()

        getTeamByUserId(user.id)
    }

    suspend fun setSlot(userId: Int, slot: Int, characterInstanceId: Int): Boolean = withContext(Dispatchers.IO) {
        if (slot !in 1..3) return@withContext false

        database.delete(BattleTeamSlots) {
            (it.userId eq userId) and (it.slot eq slot)
        }
        database.delete(BattleTeamSlots) {
            (it.characterInstanceId eq characterInstanceId)
        }

        database.insert(BattleTeamSlots) {
            set(it.userId, userId)
            set(it.slot, slot)
            set(it.characterInstanceId, characterInstanceId)
        } > 0
    }

    suspend fun clearSlot(userId: Int, slot: Int): Boolean = withContext(Dispatchers.IO) {
        database.delete(BattleTeamSlots) {
            (it.userId eq userId) and (it.slot eq slot)
        } > 0
    }

    suspend fun clearTeam(userId: Int): Int = withContext(Dispatchers.IO) {
        database.delete(BattleTeamSlots) { it.userId eq userId }
    }
}


