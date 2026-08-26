package me.igorunderplayer.kono.domain.card.ability

enum class DamageType {
    PHYSICAL,
    MAGIC,
    TRUE
}

fun DamageType.prettyName() = when (this) {
    DamageType.PHYSICAL -> "físico"
    DamageType.MAGIC -> "mágico"
    DamageType.TRUE -> "verdadeiro"
}
