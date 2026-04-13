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
        val map = json.decodeFromString<Map<String, Double>>(data)
        return map.mapKeys { Stat.valueOf(it.key) }
    }
}
