package me.igorunderplayer.kono.services

import me.igorunderplayer.kono.data.entities.User
import me.igorunderplayer.kono.data.repositories.UserRepository
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class UserService() : KoinComponent {
    private val userRepository: UserRepository by inject()

    suspend fun getUserById(userId: Int): User? {
       return userRepository.getUserById(userId)
    }

    suspend fun getUserByDiscordId(discordId: Long): User? {
        return userRepository.getUserByDiscordId(discordId)
    }

    suspend fun getOrCreateUserByDiscordId(discordId: Long): User? {
        return getUserByDiscordId(discordId) ?: createUser(discordId)
    }

    suspend fun createUser(discordId: Long, money: Int = 0): User? {
        return userRepository.createUser(discordId, money)
    }
}