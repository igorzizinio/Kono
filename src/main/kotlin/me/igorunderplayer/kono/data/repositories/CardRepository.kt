package me.igorunderplayer.kono.data.repositories

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.igorunderplayer.kono.domain.card.CardCatalog
import me.igorunderplayer.kono.domain.card.CardDefinition
import me.igorunderplayer.kono.domain.card.CardType
import me.igorunderplayer.kono.domain.card.Rarity

class CardRepository() {

    suspend fun getDefinition(id: String): CardDefinition? = withContext(Dispatchers.IO) {
        CardCatalog.getById(id)
    }

    suspend fun getAll(): List<CardDefinition> = withContext(Dispatchers.IO) {
        CardCatalog.all
    }

    suspend fun getByRarity(rarity: Rarity): List<CardDefinition> = withContext(Dispatchers.IO) {
        CardCatalog.all.filter { it.rarity == rarity }
    }

    suspend fun getByType(type: CardType): List<CardDefinition> = withContext(Dispatchers.IO) {
        CardCatalog.all.filter { it.type == type }
    }

    suspend fun getByName(name: String): CardDefinition? = withContext(Dispatchers.IO) {
        CardCatalog.all.firstOrNull { it.name.equals(name, ignoreCase = true) }
    }
}
