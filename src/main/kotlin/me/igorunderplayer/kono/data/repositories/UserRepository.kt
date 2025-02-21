package me.igorunderplayer.kono.data.repositories

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.igorunderplayer.kono.data.entities.User
import me.igorunderplayer.kono.data.entities.Users
import org.ktorm.database.Database
import org.ktorm.dsl.eq
import org.ktorm.dsl.insertAndGenerateKey
import org.ktorm.entity.find
import org.ktorm.entity.sequenceOf

class UserRepository(private val database: Database)  {


    suspend fun getUserById(userId: Int): User? = withContext(Dispatchers.IO) {
        database.sequenceOf(Users).find { it.id eq userId }
    }

    suspend fun getUserByDiscordId(discordId: Long): User? = withContext(Dispatchers.IO) {
        database.sequenceOf(Users).find { it.discordId eq discordId }
    }

    suspend fun createUser(discordId: Long, money: Int): User? = withContext(Dispatchers.IO) {
        val generatedId = database.insertAndGenerateKey(Users) {
            set(it.discordId, discordId)
            set(it.money, money)
        } as Int

        return@withContext getUserById(generatedId)
    }

}