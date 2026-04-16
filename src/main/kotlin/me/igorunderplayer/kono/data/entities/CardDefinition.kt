package me.igorunderplayer.kono.data.entities

import me.igorunderplayer.kono.domain.card.CardType
import me.igorunderplayer.kono.domain.card.Rarity
import me.igorunderplayer.kono.domain.card.Stat
import me.igorunderplayer.kono.domain.card.ability.Ability
import me.igorunderplayer.kono.serializer.AbilitySerializer
import me.igorunderplayer.kono.serializer.StatSerializer
import org.ktorm.entity.Entity
import org.ktorm.schema.Table
import org.ktorm.schema.text

interface CardDefinition : Entity<CardDefinition> {
    val id: String
    val name: String
    val description: String

    val type: CardType
    val rarity: Rarity

    var statsPerLevelRaw: String
    var statsPerLevel: Map<Stat, Double>
        get() = StatSerializer.deserialize(statsPerLevelRaw)
        set(value) {
            statsPerLevelRaw = StatSerializer.serialize(value)
        }

    var baseStatsRaw: String
    var baseStats: Map<Stat, Double>
        get() = StatSerializer.deserialize(baseStatsRaw)
        set(value) {
            baseStatsRaw = StatSerializer.serialize(value)
        }

    val faction: String?

    var tagsRaw: String
    var tags: Set<String>
        get() = if (tagsRaw.isBlank()) emptySet() else tagsRaw.split(",").toSet()
        set(value) {
            tagsRaw = value.joinToString(",")
        }

    var abilitiesRaw: String?
    var abilities: List<Ability>
        get() = abilitiesRaw?.let { AbilitySerializer.deserialize(it) } ?: emptyList()
        set(value) {
            abilitiesRaw = AbilitySerializer.serialize(value)
        }
}

object CardDefinitions : Table<CardDefinition>("tb_card_definitions") {

    val id = text("id").primaryKey().bindTo { it.id }

    val name = text("name").bindTo { it.name }

    val description = text("description")
        .bindTo { it.description }

    val type = text("type").transform(
        { CardType.valueOf(it) },
        { it.name }
    ).bindTo { it.type }

    val rarity = text("rarity").transform(
        { Rarity.valueOf(it) },
        { it.name }
    ).bindTo { it.rarity }

    val baseStatsRaw = text("base_stats")
        .bindTo { it.baseStatsRaw }

    val statsPerLevelRaw = text("stats_per_level")
        .bindTo { it.statsPerLevelRaw }

    val faction = text("faction")
        .bindTo { it.faction }

    val tagsRaw = text("tags")
        .bindTo { it.tagsRaw }

    val abilitiesRaw = text("abilities")
        .bindTo { it.abilitiesRaw }
}
