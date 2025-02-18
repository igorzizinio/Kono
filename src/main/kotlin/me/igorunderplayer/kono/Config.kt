package me.igorunderplayer.kono

import java.io.File
import java.io.FileInputStream
import java.util.*

// Saw this on https://github.com/davidffa/D4rkBotKt/blob/main/src/main/kotlin/me/davidffa/d4rkbotkt/Credentials.kt
class Config {
    companion object {
        private val properties = Properties()
        val token: String get() = properties.getProperty("TOKEN")
        val riotApiKey: String get() = properties.getProperty("RIOT_API_KEY")

        val databaseUrl: String get() = properties.getProperty("DATABASE_URL")
        val databaseUser: String get() = properties.getProperty("DATABASE_USER")
        val databasePassword: String get() = properties.getProperty("DATABASE_PASSWORD")
    }

    fun load(path: String = "./config.properties"): Config {
        try {
            val file = File(path)
            FileInputStream(file).use { properties.load(it) }
        } catch (_: Exception) {
            properties.setProperty("TOKEN", System.getenv("TOKEN"))
            properties.setProperty("RIOT_API_KEY", System.getenv("RIOT_API_KEY"))

            properties.setProperty("DATABASE_URL", System.getenv("DATABASE_URL"))
            properties.setProperty("DATABASE_USER", System.getenv("DATABASE_USER"))
            properties.setProperty("DATABASE_PASSWORD", System.getenv("DATABASE_PASSWORD"))
        }

        return this
    }
}