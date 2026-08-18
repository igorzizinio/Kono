package me.igorunderplayer.kono.engine.combat

import me.igorunderplayer.kono.domain.card.ability.Ability
import me.igorunderplayer.kono.domain.gameplay.CombatState
import me.igorunderplayer.kono.domain.gameplay.Unit
import kotlin.random.Random

/**
 * IA simples para batalhas vs npc
 */
class RandomAiTurnController(
    private val random: Random = Random.Default
) : TurnController {
    override suspend fun decideAction(
        unit: Unit,
        state: CombatState,
        availableAbilities: List<Ability>
    ): CombatAction {
        if (availableAbilities.isEmpty()) return CombatAction.BasicAttack()

        // +1 pra incluir o ataque básico como uma opção a mais no sorteio
        val roll = random.nextInt(availableAbilities.size + 1)
        return if (roll == 0) {
            CombatAction.BasicAttack()
        } else {
            CombatAction.UseAbility(availableAbilities[roll - 1])
        }
    }
}
