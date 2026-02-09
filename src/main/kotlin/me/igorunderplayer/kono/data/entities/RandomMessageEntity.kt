package me.igorunderplayer.kono.data.entities

import org.ktorm.entity.Entity
import org.ktorm.schema.Table
import org.ktorm.schema.int
import org.ktorm.schema.text

interface RandomMessage : Entity<RandomMessage> {
    companion object : Entity.Factory<RandomMessage>()

    var id: Int
    var content: String
}

object RandomMessages : Table<RandomMessage>("tb_random_messages") {
    val id = int("id").primaryKey().bindTo { it.id }
    val content = text("content").bindTo { it.content }
}