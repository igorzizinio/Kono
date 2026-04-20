package me.igorunderplayer.kono.data.entities


import org.ktorm.entity.Entity
import org.ktorm.schema.*
import java.time.Instant

interface User : Entity<User> {
    companion object : Entity.Factory<User>()

    var id: Int
    var discordId: Long

    var konos: Int
    var essence: Int

    var riotPuuid: String?
    var riotRegion: String?

    var dailyRewardClaimedAt: Instant?
    var dailyStreak: Int

    var lastWorkAt: Instant?

    val activeCharacterInstanceId: Int?

    val pityLegendary: Int
    val pityEpic: Int
}

object Users : Table<User>("tb_users") {

    val id = int("id").primaryKey().bindTo { it.id }
    val discordId = long("discord_id").bindTo { it.discordId }

    val konos = int("konos").bindTo { it.konos }
    val essence = int("essence").bindTo { it.essence }

    val riotPuuid = text("riot_puuid").bindTo { it.riotPuuid }
    val riotRegion = text("riot_region").bindTo { it.riotRegion }

    val dailyRewardClaimedAt =
        timestamp("daily_reward_claimed_at").bindTo { it.dailyRewardClaimedAt }

    val dailyStreak = int("daily_streak")
        .bindTo { it.dailyStreak }

    val lastWorkAt = timestamp("last_work_at").bindTo { it.lastWorkAt }

    val activeCharacterInstanceId = int("active_character_instance_id").bindTo { it.activeCharacterInstanceId }

    val pityLegendary = int("pity_legendary").bindTo { it.pityLegendary }
    val pityEpic = int("pity_epic").bindTo { it.pityEpic }
}
