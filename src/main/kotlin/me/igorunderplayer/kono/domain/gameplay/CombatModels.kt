package me.igorunderplayer.kono.domain.gameplay

import me.igorunderplayer.kono.domain.card.CardDefinition
import me.igorunderplayer.kono.domain.card.Stat
import me.igorunderplayer.kono.domain.card.ability.Ability
import me.igorunderplayer.kono.domain.team.TeamState

data class Unit(
    val id: String,
    val card: CardDefinition,

    var hp: Double,
    val stats: MutableMap<Stat, Double>,

    val abilities: List<Ability>,
    val tags: Set<String>,

    // Formation slot groundwork for 3x3 (0..2 for a single lane row).
    var slot: Int = 0,

    val equipments: List<CardDefinition> = emptyList(),
)

typealias Team = TeamState

