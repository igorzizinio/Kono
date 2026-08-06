package me.igorunderplayer.kono.domain.gameplay

import me.igorunderplayer.kono.domain.card.Stat
import me.igorunderplayer.kono.domain.team.TeamState
import kotlin.random.Random

data class TemporaryStatModifier(
    val unitId: String,
    val stat: Stat,
    val delta: Double,
    var remainingRounds: Int,
    val source: String
)

class CombatState(
    val teams: List<TeamState>,
    val rng: Random = Random.Default
) {

    var turn: Int = 1

    val queue: ArrayDeque<CombatEvent> = ArrayDeque()
    val combatLog: MutableList<String> = mutableListOf()

    val unitDisplayNamesById: MutableMap<String, String> = mutableMapOf()

    // 🎯 ABILITIES STATE
    val hitCounterByAbilityKey: MutableMap<String, Int> = mutableMapOf()
    val attackCountByUnitId: MutableMap<String, Int> = mutableMapOf()
    val lastDamageSourceByTeamId: MutableMap<String, String> = mutableMapOf()
    val lastDamageTurnByUnitId: MutableMap<String, Int> = mutableMapOf()
    val conditionalEffectStatesByKey: MutableMap<String, Boolean> = mutableMapOf()
    val dynamicScaleAppliedValueByKey: MutableMap<String, Double> = mutableMapOf()
    val protectorShareByUnitId: MutableMap<String, Double> = mutableMapOf()
    val tauntByUnitId: MutableSet<String> = mutableSetOf()
    val onceTriggeredAbilityKeys: MutableSet<String> = mutableSetOf()
    val temporaryStatModifiers: MutableList<TemporaryStatModifier> = mutableListOf()


    // 🎲 GLOBAL FLAGS
    val globalFlags: MutableMap<String, Any> = mutableMapOf()

    // 📊 DEBUG / REPLAY
    val eventHistory: MutableList<CombatEvent> = mutableListOf()

    fun isFinished(): Boolean {
        val aliveTeams = teams.count { team ->
            team.units.any { it.hp > 0 }
        }
        return aliveTeams <= 1
    }
}
