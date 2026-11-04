package me.igorunderplayer.kono.domain.gameplay

import me.igorunderplayer.kono.domain.card.CardDefinition
import me.igorunderplayer.kono.domain.card.Rarity

@Deprecated("Use DuelRulesConfig while the card model is migrated.")
class DeckRules {
    fun validate(cards: List<CardDefinition>): List<String> {
        val errors = mutableListOf<String>()

        if (cards.size != DuelRulesConfig.DECK_SIZE) {
            errors += "Deck must contain exactly ${DuelRulesConfig.DECK_SIZE} cards."
        }

        cards.groupingBy { it.id }.eachCount()
            .filterValues { it > DuelRulesConfig.MAX_COPIES_PER_CARD }
            .forEach { (id, count) ->
                errors += "Card $id appears $count times; maximum is ${DuelRulesConfig.MAX_COPIES_PER_CARD}."
            }

        val legendaryCount = cards.count {
            it.type != me.igorunderplayer.kono.domain.card.CardType.CHARACTER &&
                    it.rarity in setOf(Rarity.LEGENDARY, Rarity.MYTHIC)
        }
        if (legendaryCount > DuelRulesConfig.MAX_LEGENDARY_OR_MYTHIC) {
            errors += "Deck may contain at most ${DuelRulesConfig.MAX_LEGENDARY_OR_MYTHIC} Legendary/Mythic card."
        }

        return errors
    }

    fun isValid(cards: List<CardDefinition>): Boolean = validate(cards).isEmpty()
}
