package me.igorunderplayer.kono.data

import org.ktorm.database.Database

class DatabaseManager(databaseUrl: String, user: String, password: String) {
    val db: Database = Database.connect(databaseUrl, user = user, password = password)
}