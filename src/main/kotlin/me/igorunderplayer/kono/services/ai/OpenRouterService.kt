package me.igorunderplayer.kono.ai

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import me.igorunderplayer.kono.data.dto.ChatMessage
import me.igorunderplayer.kono.data.dto.ChatRequest
import me.igorunderplayer.kono.data.dto.ChatResponse
import me.igorunderplayer.kono.services.ai.AIService

class OpenRouterAIService(
    private val client: HttpClient,
    private val apiKey: String,
    private val model: String
) : AIService {

    override suspend fun generate(
        messages: List<ChatMessage>
    ): String {

        val response = client.post("https://openrouter.ai/api/v1/chat/completions") {

            contentType(ContentType.Application.Json)

            header("Authorization", "Bearer $apiKey")

            // opcionais, mas recomendados pelo OpenRouter
            header("HTTP-Referer", "https://kono.bot")
            header("X-Title", "Kono")

            setBody(
                ChatRequest(
                    model = model,
                    messages = messages
                )
            )
        }

        val body = response.body<ChatResponse>()

        return body.choices.firstOrNull()?.message?.content
            ?: error("OpenRouter retornou uma resposta vazia.")
    }
}
