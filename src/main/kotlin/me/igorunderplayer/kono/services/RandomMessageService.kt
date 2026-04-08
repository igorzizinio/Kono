package me.igorunderplayer.kono.services

import me.igorunderplayer.kono.data.repositories.RandomMessageRepository

class RandomMessageService(
    private val randomMessageRepository: RandomMessageRepository
) {
    suspend fun createRandomMessage(content: String): Int {
        return randomMessageRepository.createRandomMessage(content)
    }

    suspend fun getRandomMessageById(id: Int): String? {
        return randomMessageRepository.getRandomMessageById(id)
    }

    suspend fun getRandomMessage(): String? {
        return randomMessageRepository.getRandomMessage()
    }
}
