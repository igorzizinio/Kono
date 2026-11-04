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

    val abilities: MutableList<Ability>,
    val tags: Set<String>,

    var slot: Int = 0,

    val equipments: MutableList<CardDefinition> = mutableListOf(),
)

typealias Team = TeamState
