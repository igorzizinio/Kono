package me.igorunderplayer.kono.serializer

import kotlinx.serialization.json.Json
import me.igorunderplayer.kono.domain.card.ability.Ability

object AbilitySerializer {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = false
    }

    fun serialize(abilities: List<Ability>): String {
        return json.encodeToString(abilities)
    }

    fun deserialize(raw: String): List<Ability> {
        if (raw.isBlank()) return emptyList()

        return json.decodeFromString(raw)
    }
}
