package me.igorunderplayer.kono.services

import me.igorunderplayer.kono.data.repositories.CardRepository

class CardService(
    private val cardRepository: CardRepository
) {

    suspend fun getCardDefinition(id: String) = cardRepository.getDefinition(id)

    suspend fun getCardDefinitionByName(name: String) = cardRepository.getByName(name)

    suspend fun getCardDefinitions() = cardRepository.getAll()
}
