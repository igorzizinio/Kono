package me.igorunderplayer.kono.data.entities

import org.ktorm.entity.Entity
import org.ktorm.schema.Table
import org.ktorm.schema.boolean
import org.ktorm.schema.int
import org.ktorm.schema.varchar

interface Deck : Entity<Deck> {
    companion object : Entity.Factory<Deck>()

    var id: Int
    var userId: Int
    var name: String
    var active: Boolean
}

object Decks : Table<Deck>("tb_decks") {
    val id = int("id").primaryKey().bindTo { it.id }
    val userId = int("user_id").bindTo { it.userId }
    val name = varchar("name").bindTo { it.name }
    val active = boolean("active").bindTo { it.active }
}

interface DeckCard : Entity<DeckCard> {
    companion object : Entity.Factory<DeckCard>()

    var id: Int
    var deckId: Int
    var slot: Int
    var definitionId: String
}

object DeckCards : Table<DeckCard>("tb_deck_cards") {
    val id = int("id").primaryKey().bindTo { it.id }
    val deckId = int("deck_id").bindTo { it.deckId }
    val slot = int("slot").bindTo { it.slot }
    val definitionId = varchar("definition_id").bindTo { it.definitionId }
}
