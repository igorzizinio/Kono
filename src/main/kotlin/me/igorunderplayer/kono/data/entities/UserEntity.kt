package me.igorunderplayer.kono.data.entities


import org.ktorm.entity.Entity
import org.ktorm.schema.Table
import org.ktorm.schema.int
import org.ktorm.schema.long
import org.ktorm.schema.text
import org.ktorm.schema.timestamp
import java.time.Instant

interface User : Entity<User> {
    companion object : Entity.Factory<User>()

    var id: Int
    var discordId: Long
    var money: Int
    var riotPuuid: String?
    var riotRegion: String?
    var dailyRewardClaimedAt: Instant?
    var dailyStreak: Int
}

object Users : Table<User>("tb_users") {
    val id = int("id").primaryKey().bindTo { it.id }
    val discordId = long("discord_id").bindTo { it.discordId }
    val money = int("money").bindTo { it.money }
    val riotPuuid = text("riot_puuid").bindTo { it.riotPuuid }
    val riotRegion = text("riot_region").bindTo { it.riotRegion }
    val dailyRewardClaimedAt = timestamp("daily_reward_claimed_at").bindTo { it.dailyRewardClaimedAt }
    val dailyStreak =  int("daily_streak")
        .bindTo { it.dailyStreak }
}
