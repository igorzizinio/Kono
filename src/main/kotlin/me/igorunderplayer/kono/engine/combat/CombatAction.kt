package me.igorunderplayer.kono.engine.combat

import me.igorunderplayer.kono.domain.card.ability.Ability
import me.igorunderplayer.kono.domain.gameplay.Unit

/**
 * Ação escolhida para uma unidade em seu turno.
 *
 * `target == null` significa "deixa o engine escolher" (usa a mesma lógica de
 * [selectTargetBySlotPriority] que já existe pro modo automático).
 */
sealed class CombatAction {

    data class BasicAttack(val target: Unit? = null) : CombatAction()

    data class UseAbility(val ability: Ability, val target: Unit? = null) : CombatAction()

    /** Unidade decidiu não agir nesse "hit" do turno (ex: sem mana/carga suficiente). */
    object Pass : CombatAction()
}
