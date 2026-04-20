package me.igorunderplayer.kono.domain.team

import me.igorunderplayer.kono.domain.card.CardDefinition
import me.igorunderplayer.kono.domain.card.Stat
import me.igorunderplayer.kono.domain.card.ability.Ability
import me.igorunderplayer.kono.domain.gameplay.Unit

object CombatUnitFactory {

    fun createUnit(
        unitId: String,
        definition: CardDefinition,
        level: Int,
        equips: List<CardDefinition> = emptyList()
    ): Unit {
        val stats = definition.baseStats.toMutableMap()

        val safeLevel = level.coerceAtLeast(1)
        definition.statsPerLevel.forEach { (stat, perLevel) ->
            stats[stat] = (stats[stat] ?: 0.0) + (perLevel * (safeLevel - 1))
        }

        equips.forEach { equip ->
            equip.baseStats.forEach { (stat, value) ->
                stats[stat] = (stats[stat] ?: 0.0) + value
            }
        }

        val abilities = mutableListOf<Ability>()
        abilities += definition.abilities
        equips.forEach { abilities += it.abilities }

        val tags = mutableSetOf<String>()
        tags += definition.tags
        equips.forEach { tags += it.tags }

        return Unit(
            id = unitId,
            card = definition,
            hp = stats[Stat.HP] ?: 100.0,
            stats = stats,
            abilities = abilities,
            tags = tags,
            equipments = equips
        )
    }
}

