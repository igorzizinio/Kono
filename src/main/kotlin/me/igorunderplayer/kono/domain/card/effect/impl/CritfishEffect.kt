package me.igorunderplayer.kono.domain.card.effect.impl

import me.igorunderplayer.kono.domain.card.Stat
import me.igorunderplayer.kono.domain.card.effect.Effect
import me.igorunderplayer.kono.domain.combat.CombatContext

class CritfishEffect : Effect {
    override fun apply(context: CombatContext) {
        context.setStat(Stat.CRIT_CHANCE, 0.05)
        context.lockStat(Stat.CRIT_CHANCE)
        context.multiplyStat(Stat.CRIT_DAMAGE, 3.0)
    }
}
