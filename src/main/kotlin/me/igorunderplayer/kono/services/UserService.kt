package me.igorunderplayer.kono.services

import me.igorunderplayer.kono.data.entities.User
import me.igorunderplayer.kono.data.entities.Users
import me.igorunderplayer.kono.data.repositories.UserRepository
import no.stelar7.api.r4j.basic.constants.api.regions.LeagueShard
import org.ktorm.dsl.desc

class UserService(
    private val userRepository: UserRepository,
) {
    suspend fun getUserById(userId: Int): User? {
       return userRepository.getUserById(userId)
    }

    suspend fun getUserByDiscordId(discordId: Long): User? {
        return userRepository.getUserByDiscordId(discordId)
    }

    suspend fun getOrCreateUserByDiscordId(discordId: Long): User? {
        return getUserByDiscordId(discordId) ?: createUser(discordId)
    }

    suspend fun createUser(discordId: Long): User? {
        return userRepository.createUser(discordId)
    }

    suspend fun assignRiotAccountToUser(userId: Int, riotPuuid: String, riotRegion: LeagueShard): Boolean {
        return userRepository.assignRiotAccountToUser(userId, riotPuuid, riotRegion.value)
    }

    suspend fun getUsers(): List<User> {
        return userRepository.getUsers()
    }

    suspend fun getTopMoney(limit: Int = 10, startAt: Int = 0): List<User> {
        return userRepository.getUsers(limit, orderBy = Users.konos.desc(), startAt)
    }
}
