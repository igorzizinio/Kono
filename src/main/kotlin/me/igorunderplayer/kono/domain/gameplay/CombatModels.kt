package me.igorunderplayer.kono.domain.gameplay

import me.igorunderplayer.kono.data.entities.CardDefinition
import me.igorunderplayer.kono.domain.card.Stat
import me.igorunderplayer.kono.domain.card.ability.Ability

data class Unit(
    val id: String,
    val card: CardDefinition,

    var hp: Double,
    val stats: MutableMap<Stat, Double>,

    val abilities: List<Ability>,
    val tags: Set<String>,

    val equipments: List<CardDefinition> = emptyList(),
)

data class Team(
    val id: String,
    val units: MutableList<Unit>
)
