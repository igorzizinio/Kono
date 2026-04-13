package me.igorunderplayer.kono.data.entities

import me.igorunderplayer.kono.domain.card.CardType
import me.igorunderplayer.kono.domain.card.Rarity
import me.igorunderplayer.kono.domain.card.Stat
import me.igorunderplayer.kono.serializer.StatSerializer
import org.ktorm.entity.Entity
import org.ktorm.schema.Table
import org.ktorm.schema.text

interface CardDefinition : Entity<CardDefinition> {
    val id: String
    val name: String
    val type: CardType
    val rarity: Rarity
    var baseStatsRaw: String
    var baseStats: Map<Stat, Double>
        get() = StatSerializer.deserialize(baseStatsRaw)
        set(value) {
            baseStatsRaw = StatSerializer.serialize(value)
        }
    val effectId: String?
}

object CardDefinitions : Table<CardDefinition>("tb_card_definitions") {
    val id = text("id").primaryKey().bindTo { it.id }
    val name = text("name").bindTo { it.name }
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

    val effectId = text("effect_id").bindTo { it.effectId }
}
