package me.igorunderplayer.kono.engine.combat

import kotlin.math.ceil

fun ceilPct(value: Double, pct: Double): Double =
    ceil(value * (1.0 + pct.coerceAtLeast(-1.0)))

fun basicDamage(attack: Double, defense: Double): Double =
    ceil((attack - defense).coerceAtLeast(1.0))
