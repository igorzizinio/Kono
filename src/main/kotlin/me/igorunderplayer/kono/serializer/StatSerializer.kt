package me.igorunderplayer.kono.serializer

import kotlinx.serialization.json.*
import me.igorunderplayer.kono.domain.card.Stat

object StatSerializer {

    private val json = Json

    fun serialize(stats: Map<Stat, Double>): String {
        val map = stats.mapKeys { it.key.name }
        return json.encodeToString(map)
    }

    fun deserialize(data: String): Map<Stat, Double> {
        if (data.isBlank() || data == "{}") {
            return emptyMap()
        }
        return try {
            val map = json.decodeFromString<Map<String, Double>>(data)
            map.mapKeys { Stat.valueOf(it.key) }
        } catch (e: Exception) {
            println("Error deserializing stats '$data': ${e.message}")
            emptyMap()
        }
    }
}
