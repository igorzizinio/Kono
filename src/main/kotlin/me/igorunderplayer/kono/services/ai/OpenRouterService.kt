package me.igorunderplayer.kono.services.ai

import io.ktor.client.HttpClient
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.serialization.json.Json
import me.igorunderplayer.kono.data.dto.ChatMessage
import me.igorunderplayer.kono.data.dto.ChatRequest
import me.igorunderplayer.kono.data.dto.ChatResponse

class OpenRouterAIService(
    private val client: HttpClient,
    private val apiKey: String,
    private val model: String
) : AIService {

    private val json = Json {
        ignoreUnknownKeys = true
    }

    override suspend fun generate(
        messages: List<ChatMessage>
    ): String {

        val response = client.post("https://openrouter.ai/api/v1/chat/completions") {

            contentType(ContentType.Application.Json)

            header("Authorization", "Bearer $apiKey")

            header("HTTP-Referer", "https://kono.bot")
            header("X-Title", "Kono")

            setBody(
                ChatRequest(
                    model = model,
                    messages = messages
                )
            )
        }

        val bodyText = response.bodyAsText()

        if (!response.status.isSuccess()) {
            println("===== OpenRouter Error =====")
            println("Status: ${response.status}")
            println(bodyText)
            error("OpenRouter retornou erro ${response.status}")
        }

        val body = try {
            json.decodeFromString<ChatResponse>(bodyText)
        } catch (e: Exception) {
            println("===== OpenRouter Parse Error =====")
            println("Status: ${response.status}")
            println(bodyText)
            throw e
        }

        return body.choices.firstOrNull()?.message?.content
            ?: error("OpenRouter retornou uma resposta sem choices:\n$bodyText")
    }
}
