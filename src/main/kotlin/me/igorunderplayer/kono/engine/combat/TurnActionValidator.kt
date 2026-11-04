package me.igorunderplayer.kono.engine.combat

import me.igorunderplayer.kono.domain.gameplay.CombatState
import me.igorunderplayer.kono.domain.gameplay.TurnAction
import me.igorunderplayer.kono.domain.gameplay.Unit
import me.igorunderplayer.kono.domain.card.CardCatalog

sealed interface ActionValidation {
    data object Valid : ActionValidation
    data class Invalid(val reason: String) : ActionValidation
}

class TurnActionValidator(
    private val state: CombatState,
    private val activeUnit: Unit
) {
    fun validate(action: TurnAction): ActionValidation {
        if (!state.isCurrentActor(activeUnit)) {
            return ActionValidation.Invalid("It is not this unit's turn.")
        }
        return when (action) {
            is TurnAction.Attack -> validateEnemyTarget(action.target, activeUnit.attackCost())
            is TurnAction.UseAbility -> {
                if (action.ability !in activeUnit.abilities ||
                    action.ability.type.name != "ACTIVE"
                ) {
                    return ActionValidation.Invalid("Ability is not available to this unit.")
                }
                val uses = state.usesThisTurn[abilityKey(action.ability.name)] ?: 0
                val limit = action.ability.usesPerTurn
                when {
                    limit != null && uses >= limit -> ActionValidation.Invalid("Ability use limit reached.")
                    !hasAp(action.ability.apCost) -> ActionValidation.Invalid("Not enough AP.")
                    else -> ActionValidation.Valid
                }
            }

            is TurnAction.PlayCard -> {
                val card = CardCatalog.getById(action.cardId)
                when {
                    card == null -> ActionValidation.Invalid("Unknown card.")
                    !state.currentHand().contains(card.id) -> ActionValidation.Invalid("Card is not in hand.")
                    !hasAp(card.apCost) -> ActionValidation.Invalid("Not enough AP.")
                    card.abilities.isEmpty() -> ActionValidation.Invalid("Card has no effect.")
                    else -> ActionValidation.Valid
                }
            }

            is TurnAction.Equip -> {
                val card = CardCatalog.getById(action.cardId)
                when {
                    card == null -> ActionValidation.Invalid("Unknown equipment.")
                    card.type.name != "EQUIPMENT" -> ActionValidation.Invalid("Card is not equipment.")
                    !hasAp(card.apCost) -> ActionValidation.Invalid("Not enough AP.")
                    else -> ActionValidation.Valid
                }
            }

            is TurnAction.Swap -> {
                val cost = if (state.freeSwapCount > 0) 0 else 1
                if (action.character.hp <= 0) ActionValidation.Invalid("Character is defeated.")
                else if (state.teamOf(action.character) != state.teamOf(activeUnit)) ActionValidation.Invalid("Swap target must be an ally.")
                else if (hasAp(cost)) ActionValidation.Valid
                else ActionValidation.Invalid("Not enough AP.")
            }

            TurnAction.Surrender, TurnAction.EndTurn -> ActionValidation.Valid
        }
    }

    private fun validateEnemyTarget(target: Unit, cost: Int): ActionValidation =
        if (target.hp <= 0) ActionValidation.Invalid("Target is defeated.")
        else if (state.teamOf(target) == state.teamOf(activeUnit)) ActionValidation.Invalid("Target must be an enemy.")
        else if (hasAp(cost)) ActionValidation.Valid
        else ActionValidation.Invalid("Not enough AP.")

    private fun hasAp(cost: Int): Boolean = state.ap >= cost

    private fun abilityKey(name: String): String = "${activeUnit.id}:$name"

    private fun Unit.attackCost(): Int = card.attackCost
}
