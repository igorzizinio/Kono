package me.igorunderplayer.kono.data.repositories

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.igorunderplayer.kono.data.DatabaseManager
import me.igorunderplayer.kono.data.entities.Deck
import me.igorunderplayer.kono.data.entities.DeckCards
import me.igorunderplayer.kono.data.entities.Decks
import org.ktorm.database.Database
import org.ktorm.dsl.*
import org.ktorm.entity.find
import org.ktorm.entity.sequenceOf

data class DeckWithCards(val deck: Deck, val cardIds: List<String>)

class DeckRepository(private val databaseManager: DatabaseManager) {
    private val database: Database
        get() = databaseManager.db

    suspend fun listByUserId(userId: Int): List<DeckWithCards> = withContext(Dispatchers.IO) {
        database.from(Decks).select().where { Decks.userId eq userId }
            .orderBy(Decks.name.asc())
            .map { row -> row.toDeckWithCards() }
    }

    suspend fun findByName(userId: Int, name: String): DeckWithCards? = withContext(Dispatchers.IO) {
        database.from(Decks).select()
            .where { (Decks.userId eq userId) and (Decks.name eq name) }
            .map { it.toDeckWithCards() }
            .firstOrNull()
    }

    suspend fun getActive(userId: Int): DeckWithCards? = withContext(Dispatchers.IO) {
        database.from(Decks).select()
            .where { (Decks.userId eq userId) and (Decks.active eq true) }
            .map { it.toDeckWithCards() }
            .firstOrNull()
    }

    suspend fun create(userId: Int, name: String): Boolean = withContext(Dispatchers.IO) {
        if (findByName(userId, name) != null) return@withContext false
        val hasDeck = database.from(Decks).select(Decks.id).where { Decks.userId eq userId }
            .totalRecordsInAllPages > 0
        database.insert(Decks) {
            set(it.userId, userId)
            set(it.name, name)
            set(it.active, !hasDeck)
        } > 0
    }

    suspend fun setActive(userId: Int, name: String): Boolean = withContext(Dispatchers.IO) {
        val deck = findByName(userId, name) ?: return@withContext false
        database.update(Decks) {
            set(it.active, false)
            where { Decks.userId eq userId }
        }
        database.update(Decks) {
            set(it.active, true)
            where { Decks.id eq deck.deck.id }
        } > 0
    }

    suspend fun addCard(userId: Int, name: String, definitionId: String): Boolean =
        withContext(Dispatchers.IO) {
            val deck = findByName(userId, name) ?: return@withContext false
            val nextSlot = database.from(DeckCards).select(DeckCards.slot)
                .where { DeckCards.deckId eq deck.deck.id }
                .mapNotNull { it[DeckCards.slot] }.maxOrNull()?.plus(1) ?: 0
            database.insert(DeckCards) {
                set(it.deckId, deck.deck.id)
                set(it.slot, nextSlot)
                set(it.definitionId, definitionId)
            } > 0
        }

    suspend fun removeCard(userId: Int, name: String, slot: Int): Boolean =
        withContext(Dispatchers.IO) {
            val deck = findByName(userId, name) ?: return@withContext false
            database.delete(DeckCards) { (it.deckId eq deck.deck.id) and (it.slot eq slot) } > 0
        }

    private fun QueryRowSet.toDeckWithCards(): DeckWithCards {
        val deck = Decks.createEntity(this)
        val cards = database.from(DeckCards).select()
            .where { DeckCards.deckId eq deck.id }
            .orderBy(DeckCards.slot.asc())
            .mapNotNull { it[DeckCards.definitionId] }
        return DeckWithCards(deck, cards)
    }
}
