package me.igorunderplayer.kono.data.entities


import org.ktorm.schema.*
import org.ktorm.entity.Entity

interface User : Entity<User> {
    companion object : Entity.Factory<User>()

    var id: Int
    var discordId: Long
    var money: Int
}

object Users : Table<User>("tb_users") {
    val id = int("id").primaryKey().bindTo { it.id }
    val discordId = long("discord_id").bindTo { it.discordId }
    val money = int("money").bindTo { it.money }
}