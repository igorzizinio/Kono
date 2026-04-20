package me.igorunderplayer.kono.domain.gameplay

import me.igorunderplayer.kono.domain.team.TeamState
import kotlin.random.Random

class CombatState(
    val teams: List<TeamState>,
    val rng: Random = Random.Default
) {

    var turn: Int = 1

    val queue: ArrayDeque<CombatEvent> = ArrayDeque()
    val combatLog: MutableList<String> = mutableListOf()

    val unitDisplayNamesById: MutableMap<String, String> = mutableMapOf()

    // 💥 DAMAGE / SHIELDS
    val damageShieldStacksByUnitId: MutableMap<String, Int> = mutableMapOf()

    val incomingDamageModifiersByUnitId: MutableMap<String, MutableList<(Double) -> Double>> =
        mutableMapOf()

    val outgoingDamageModifiersByUnitId: MutableMap<String, MutableList<(Double) -> Double>> =
        mutableMapOf()

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

    // 💰 ECONOMY (team-based, 3x3 ready)
    val coinsByTeamId: MutableMap<String, Int> = mutableMapOf()

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

    fun addCoins(team: TeamState, amount: Int) {
        coinsByTeamId[team.id] =
            (coinsByTeamId[team.id] ?: 0) + amount
    }

    fun getCoins(team: TeamState): Int {
        return coinsByTeamId[team.id] ?: 0
    }

    fun addShield(unitId: String, stacks: Int) {
        damageShieldStacksByUnitId[unitId] =
            (damageShieldStacksByUnitId[unitId] ?: 0) + stacks
    }

    fun consumeShield(unitId: String): Boolean {
        val stacks = damageShieldStacksByUnitId[unitId] ?: 0
        if (stacks <= 0) return false

        damageShieldStacksByUnitId[unitId] = stacks - 1
        return true
    }
}
