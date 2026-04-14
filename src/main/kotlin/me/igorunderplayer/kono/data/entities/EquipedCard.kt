package me.igorunderplayer.kono.data.entities

import org.ktorm.entity.Entity
import org.ktorm.schema.Table
import org.ktorm.schema.int

interface EquippedCard : Entity<EquippedCard> {

    val id: Int

    val characterInstanceId: Int
    val cardInstanceId: Int

    val slot: Int
}

object EquippedCards : Table<EquippedCard>("tb_equipped_cards") {

    val id = int("id").primaryKey().bindTo { it.id }

    val characterInstanceId = int("character_instance_id")
        .bindTo { it.characterInstanceId }

    val cardInstanceId = int("card_instance_id")
        .bindTo { it.cardInstanceId }

    val slot = int("slot")
        .bindTo { it.slot }
}
