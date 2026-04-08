package me.igorunderplayer.kono.services

import dev.kord.core.Kord
import dev.kord.core.entity.Emoji
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.toList

class EmojiService(
    private val kord: Kord
) {

    private val emojiGuilds = listOf(
        "931300984242724864",
        "978482978143498280",
        "1124789207982931998",
        "1124821720784699442"
    )

    private val emojis = mutableListOf<Emoji>()

    suspend fun loadEmojis() {
        emojis.clear()

        val guilds = kord.guilds
            .filter { emojiGuilds.contains(it.id.toString()) }
            .toList()

        for (guild in guilds) {
            val guildEmojis = guild.emojis.toList()
            emojis.addAll(guildEmojis)
        }
    }

    fun getAll(): List<Emoji> = emojis

    fun getRandom(): Emoji? = emojis.randomOrNull()

    fun findByName(name: String): Emoji? =
        emojis.find { it.name.equals(name, ignoreCase = true) }
}
