package me.igorunderplayer.kono.services

import me.igorunderplayer.kono.data.repositories.RandomMessageRepository
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class RandomMessageService() : KoinComponent {
    private val randomMessageService: RandomMessageRepository by inject()

    suspend fun createRandomMessage(content: String): Int {
        return randomMessageService.createRandomMessage(content)
    }

    suspend fun getRandomMessageById(id: Int): String? {
        return randomMessageService.getRandomMessageById(id)
    }

    suspend fun getRandomMessage(): String? {
        return randomMessageService.getRandomMessage()
    }
}