package me.igorunderplayer.kono.domain.combat

import me.igorunderplayer.kono.domain.card.Stat

class CombatContext(
    baseStats: Map<Stat, Double>
) {

    private val stats = baseStats.toMutableMap()
    private val lockedStats = mutableSetOf<Stat>()

    fun setStat(stat: Stat, value: Double) {
        if (!lockedStats.contains(stat)) {
            stats[stat] = value
        }
    }

    fun addStat(stat: Stat, value: Double) {
        if (!lockedStats.contains(stat)) {
            stats[stat] = (stats[stat] ?: 0.0) + value
        }
    }

    fun multiplyStat(stat: Stat, multiplier: Double) {
        if (!lockedStats.contains(stat)) {
            stats[stat] = (stats[stat] ?: 0.0) * multiplier
        }
    }

    fun lockStat(stat: Stat) {
        lockedStats.add(stat)
    }

    fun getStat(stat: Stat): Double {
        return stats[stat] ?: 0.0
    }

    fun getAll(): Map<Stat, Double> = stats
}
