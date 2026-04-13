package me.igorunderplayer.kono.services

import CardRepository
import me.igorunderplayer.kono.data.entities.CardInstance
import me.igorunderplayer.kono.domain.card.Stat
import me.igorunderplayer.kono.domain.card.effect.EffectRegistry
import me.igorunderplayer.kono.domain.combat.CombatContext

class CardService(
    private val cardRepository: CardRepository
) {

    suspend fun buildCharacterStats(
        character: CardInstance,
        equipped: List<CardInstance>
    ): Map<Stat, Double> {

        val definition = cardRepository.getDefinition(character.definitionId)
            ?: throw IllegalArgumentException("Character definition not found for ID: ${character.definitionId}")

        val context = CombatContext(definition.baseStats)

        // aplica efeitos dos equipamentos
        for (card in equipped) {
            val def = cardRepository.getDefinition(card.definitionId) ?: throw IllegalArgumentException("Character definition not found for ID: ${character.definitionId}")
            val effect = EffectRegistry.get(def.effectId)

            effect?.apply(context)
        }

        return context.getAll()
    }
}
