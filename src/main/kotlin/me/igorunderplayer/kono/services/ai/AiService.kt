package me.igorunderplayer.kono.services.ai

import me.igorunderplayer.kono.data.dto.ChatMessage

interface AIService {

    suspend fun generate(
        messages: List<ChatMessage>
    ): String

}
