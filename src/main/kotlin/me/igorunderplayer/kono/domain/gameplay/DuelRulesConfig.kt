package me.igorunderplayer.kono.domain.gameplay

/**
 * The single source of truth for deck and duel tuning.
 * Keep balance changes here instead of scattering constants through the engine.
 */
object DuelRulesConfig {
    const val DECK_SIZE = 20
    const val MAX_COPIES_PER_CARD = 2
    const val MAX_LEGENDARY_OR_MYTHIC = 1

    const val STARTING_HAND_SIZE = 5
    const val DRAW_PER_TURN = 1
    const val MAX_HAND_SIZE = 7
    const val MULLIGAN_LIMIT = 1
    const val MULLIGAN_MAX_CARDS = 2

    const val AP_PER_TURN = 3
    const val SWAP_AP_COST = 1
    const val FORCED_SWAP_AP_COST = 0
    const val TURN_LIMIT = 30
    const val TIMEOUTS_FOR_LOSS = 3
    const val FATIGUE_DAMAGE = 1.0
}
