package me.igorunderplayer.kono.engine.combat

import me.igorunderplayer.kono.domain.card.ability.Ability
import me.igorunderplayer.kono.domain.gameplay.CombatState
import me.igorunderplayer.kono.domain.gameplay.Unit

interface TurnController {
    suspend fun decideAction(
        unit: Unit,
        state: CombatState,
        availableAbilities: List<Ability>
    ): CombatAction
}
