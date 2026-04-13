package me.igorunderplayer.kono.services

import me.igorunderplayer.kono.data.entities.User
import me.igorunderplayer.kono.data.repositories.UserRepository
import no.stelar7.api.r4j.basic.constants.api.regions.LeagueShard

class UserService(
    private val userRepository: UserRepository
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
}
