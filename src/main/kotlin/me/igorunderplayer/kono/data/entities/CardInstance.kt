package me.igorunderplayer.kono.data.entities

import org.ktorm.entity.Entity
import org.ktorm.schema.Table
import org.ktorm.schema.boolean
import org.ktorm.schema.int
import org.ktorm.schema.text

interface CardInstance : Entity<CardInstance> {
    val id: Int
    val userId: Int
    val definitionId: String
    val level: Int
    val upgraded: Boolean
}

object CardInstances : Table<CardInstance>("tb_card_instances") {
    val id = int("id").primaryKey().bindTo { it.id }
    val userId = int("user_id").bindTo { it.userId }
    val definitionId = text("definition_id").bindTo { it.definitionId }
    val level = int("level").bindTo { it.level }
    val upgraded = boolean("upgraded").bindTo { it.upgraded }
}
