package me.igorunderplayer.kono.data


import me.igorunderplayer.kono.Config.Companion.databaseUrl
import org.ktorm.database.Database
import org.ktorm.support.postgresql.PostgreSqlDialect

class DatabaseManager() {
    val db: Database = Database.connect(databaseUrl, dialect = PostgreSqlDialect())
}