package me.igorunderplayer.kono.data.entities

import org.ktorm.entity.Entity
import org.ktorm.schema.Table
import org.ktorm.schema.int

interface BattleTeamSlot : Entity<BattleTeamSlot> {
    companion object : Entity.Factory<BattleTeamSlot>()

    var id: Int
    var userId: Int
    var slot: Int
    var characterInstanceId: Int
}

object BattleTeamSlots : Table<BattleTeamSlot>("tb_battle_team_slots") {
    val id = int("id").primaryKey().bindTo { it.id }
    val userId = int("user_id").bindTo { it.userId }
    val slot = int("slot").bindTo { it.slot }
    val characterInstanceId = int("character_instance_id").bindTo { it.characterInstanceId }
}

