package me.igorunderplayer.kono.domain.gameplay

import me.igorunderplayer.kono.domain.card.ability.Ability

sealed interface TurnAction {
    data class Attack(val target: Unit) : TurnAction
    data class UseAbility(val ability: Ability, val target: Unit? = null) : TurnAction
    data class PlayCard(val cardId: String, val target: Unit? = null) : TurnAction
    data class Equip(val cardId: String) : TurnAction
    data class Swap(val character: Unit) : TurnAction
    data object Surrender : TurnAction
    data object EndTurn : TurnAction
}
