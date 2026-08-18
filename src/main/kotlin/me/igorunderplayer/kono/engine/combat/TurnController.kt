package me.igorunderplayer.kono.engine.combat

import me.igorunderplayer.kono.domain.card.ability.Ability
import me.igorunderplayer.kono.domain.gameplay.CombatState
import me.igorunderplayer.kono.domain.gameplay.Unit

/**
 * Decide a ação de uma unidade em seu turno. O CombatEngine chama isso uma vez
 * por "hit" disponível (resolveAttackCount), então uma unidade rápida pode ser
 * consultada mais de uma vez no mesmo turno.
 *
 * Implementações não devem mutar o CombatState diretamente — apenas decidir e
 * retornar a ação. Quem aplica o efeito é sempre o engine.
 */
interface TurnController {
    suspend fun decideAction(
        unit: Unit,
        state: CombatState,
        availableAbilities: List<Ability>
    ): CombatAction
}

/**
 * Reproduz o comportamento antigo (auto-attack) para quem não registrar nenhum
 * controller. Não é obrigatório usar isso — se `controllersByUnitId` não tiver
 * entrada pra uma unidade, o CombatEngine já cai nesse caminho automaticamente
 * no modo "controlled" — mas fica disponível caso você queira misturar IA com
 * player no mesmo time.
 */
class AutoTurnController : TurnController {
    override suspend fun decideAction(
        unit: Unit,
        state: CombatState,
        availableAbilities: List<Ability>
    ): CombatAction = CombatAction.BasicAttack()
}

/**
 * Ponte genérica pro player. Não conhece Discord, Slack, CLI, nada — só recebe
 * uma função suspend que você implementa na camada do bot pra, por exemplo,
 * mandar os botões da mensagem e suspender até o usuário clicar.
 *
 * Exemplo de uso no bot (pseudo-código):
 *
 * ```
 * val controller = PlayerTurnController { unit, abilities ->
 *     val deferred = CompletableDeferred<CombatAction>()
 *     pendingDecisions[interactionId] = deferred
 *     sendActionButtons(channel, unit, abilities) // ataque básico + 1 botão por ability ACTIVE
 *     deferred.await() // resume quando o listener de botão completar o deferred
 * }
 * ```
 */
class PlayerTurnController(
    private val onDecide: suspend (unit: Unit, availableAbilities: List<Ability>) -> CombatAction
) : TurnController {
    override suspend fun decideAction(
        unit: Unit,
        state: CombatState,
        availableAbilities: List<Ability>
    ): CombatAction = onDecide(unit, availableAbilities)
}
