package me.igorunderplayer.kono.data


import me.igorunderplayer.kono.Config.Companion.databaseUrl
import me.igorunderplayer.kono.Config.Companion.databaseUser
import me.igorunderplayer.kono.Config.Companion.databasePassword
import org.ktorm.database.Database

class DatabaseManager() {
    val db: Database = Database.connect(databaseUrl, user = databaseUser, password = databasePassword)
}