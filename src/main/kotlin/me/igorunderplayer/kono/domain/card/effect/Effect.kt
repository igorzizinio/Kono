package me.igorunderplayer.kono.domain.card.effect

import me.igorunderplayer.kono.domain.combat.CombatContext

interface Effect {
    fun apply(context: CombatContext)
}
