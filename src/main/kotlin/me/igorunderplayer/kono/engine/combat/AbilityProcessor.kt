package me.igorunderplayer.kono.engine.combat

import me.igorunderplayer.kono.domain.card.ability.Ability
import me.igorunderplayer.kono.domain.card.ability.AbilityTrigger
import me.igorunderplayer.kono.domain.card.ability.AbilityType
import me.igorunderplayer.kono.domain.gameplay.CombatEvent
import me.igorunderplayer.kono.domain.gameplay.CombatState
import me.igorunderplayer.kono.domain.gameplay.Unit

object AbilityProcessor {

    suspend fun process(
        ability: Ability,
        owner: Unit,
        event: CombatEvent,
        state: CombatState,
        modifiers: MutableList<DamageModifier>
    ) {
        if (!matches(ability.trigger, event)) return

        when (ability.type) {

            AbilityType.LIFESTEAL -> {
                if (event is CombatEvent.AfterDamage && event.source == owner) {
                    val value = ability.value ?: return
                    val heal = event.damage * (value / 100.0)
                    owner.hp += heal
                }
            }

            AbilityType.DAMAGE -> {
                if (event is CombatEvent.Attack && event.attacker == owner) {
                    val value = ability.value ?: return
                    state.queue.add(
                        CombatEvent.BeforeDamage(owner, event.target, value)
                    )
                }
            }

            AbilityType.RNG_EFFECT -> {
                if (event is CombatEvent.TurnStart && event.unit == owner) {
                    val roll = state.rng.nextDouble()
                    if (roll < 0.5) {
                        owner.hp += 10
                    } else {
                        owner.hp -= 5
                    }
                }
            }

            AbilityType.INCOMING_DAMAGE_REDUCTION -> {
                if (event is CombatEvent.BeforeDamage && event.target == owner) {
                    addIncomingDamageReductionModifier(ability, modifiers)
                }
            }

            AbilityType.OUTGOING_DAMAGE_AMPLIFICATION -> {
                if (event is CombatEvent.BeforeDamage && event.source == owner) {
                    addOutgoingDamageAmplificationModifier(ability, modifiers)
                }
            }

            else -> {}
        }
    }

    private fun addIncomingDamageReductionModifier(
        ability: Ability,
        modifiers: MutableList<DamageModifier>
    ) {
        val value = ability.value ?: return
        modifiers.add { dmg -> dmg * (1 - value / 100.0) }
    }

    private fun addOutgoingDamageAmplificationModifier(
        ability: Ability,
        modifiers: MutableList<DamageModifier>
    ) {
        val value = ability.value ?: return
        modifiers.add { dmg -> dmg * (1 + value / 100.0) }
    }

    private fun matches(trigger: AbilityTrigger?, event: CombatEvent): Boolean {
        return when (trigger) {
            AbilityTrigger.ON_TURN_START -> event is CombatEvent.TurnStart
            AbilityTrigger.ON_ATTACK -> event is CombatEvent.Attack
            AbilityTrigger.ON_DAMAGE_TAKEN -> event is CombatEvent.BeforeDamage
            AbilityTrigger.ON_HIT -> event is CombatEvent.AfterDamage
            AbilityTrigger.PASSIVE -> true
            else -> false
        }
    }
}
