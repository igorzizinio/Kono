package me.igorunderplayer.kono.domain.card.effect

import me.igorunderplayer.kono.domain.card.effect.impl.CritfishEffect

object EffectRegistry {

    private val effects = mapOf(
        "CRITFISH" to CritfishEffect(),
    )

    fun get(effectId: String?): Effect? {
        return effects[effectId]
    }
}
