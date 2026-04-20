package me.igorunderplayer.kono.data.entities

import org.ktorm.entity.Entity
import org.ktorm.schema.Table
import org.ktorm.schema.int
import org.ktorm.schema.text
import org.ktorm.schema.timestamp
import java.time.Instant

interface BattleVictory : Entity<BattleVictory> {
    companion object : Entity.Factory<BattleVictory>()

    var id: Int
    var userId: Int
    var enemyId: String
    var essenceReward: Int
    var firstWonAt: Instant
}

object BattleVictories : Table<BattleVictory>("tb_battle_victories") {
    val id = int("id").primaryKey().bindTo { it.id }
    val userId = int("user_id").bindTo { it.userId }
    val enemyId = text("enemy_id").bindTo { it.enemyId }
    val essenceReward = int("essence_reward").bindTo { it.essenceReward }
    val firstWonAt = timestamp("first_won_at").bindTo { it.firstWonAt }
}

