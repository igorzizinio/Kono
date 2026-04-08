package me.igorunderplayer.kono.data

import me.igorunderplayer.kono.Config.Companion.databaseUrl
import org.ktorm.database.Database
import org.ktorm.support.postgresql.PostgreSqlDialect
import java.net.URI

class DatabaseManager {

    val db: Database = createDatabase()

    private fun createDatabase(): Database {
        val rawUrl = databaseUrl
        if (rawUrl.startsWith("jdbc:")) {
            return Database.connect(
                url = rawUrl,
                dialect = PostgreSqlDialect()
            )
        }

        val uri = URI(rawUrl)
        val (user, password) = uri.userInfo.split(":")

        val jdbcUrl =
            "jdbc:postgresql://${uri.host}:${uri.port}${uri.path}?sslmode=require"

        return Database.connect(
            url = jdbcUrl,
            user = user,
            password = password,
            dialect = PostgreSqlDialect()
        )
    }
}
