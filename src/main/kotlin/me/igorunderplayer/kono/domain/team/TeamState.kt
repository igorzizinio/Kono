package me.igorunderplayer.kono.domain.team

import me.igorunderplayer.kono.domain.gameplay.Unit


class TeamState(
    val id: String,
    val units: MutableList<Unit>,
    val shared: MutableMap<String, Int> = mutableMapOf()
) {

    companion object {
        const val DEFAULT_MAX_FORMATION_SLOTS = 3
    }

    fun addCoins(amount: Int) {
        shared["coins"] = (shared["coins"] ?: 0) + amount
    }

    fun coins(): Int = shared["coins"] ?: 0

    fun aliveUnits(): List<Unit> = units.filter { it.hp > 0 }

    fun aliveUnitBySlot(slot: Int): Unit? = aliveUnits().firstOrNull { it.slot == slot }

    fun normalizeSlots(maxSlots: Int = DEFAULT_MAX_FORMATION_SLOTS) {
        units.forEachIndexed { index, unit ->
            unit.slot = (unit.slot).coerceIn(0, maxSlots - 1)
            if (unit.slot != index.coerceIn(0, maxSlots - 1) && units.none { it !== unit && it.slot == unit.slot }) {
                return@forEachIndexed
            }

            unit.slot = index.coerceIn(0, maxSlots - 1)
        }
    }
}
