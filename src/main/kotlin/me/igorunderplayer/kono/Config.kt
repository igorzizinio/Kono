package me.igorunderplayer.kono

import java.io.File
import java.io.FileInputStream
import java.util.*

class Config {
    companion object {
        private val properties = Properties()

        val token: String
            get() = properties.getProperty("TOKEN")
                ?: error("TOKEN not defined")

        val riotApiKey: String
            get() = properties.getProperty("RIOT_API_KEY")
                ?: error("RIOT_API_KEY not defined")

        val databaseUrl: String
            get() = properties.getProperty("DATABASE_URL")
                ?: error("DATABASE_URL not defined")

        val openRouterApiKey: String
            get() = properties.getProperty("OPEN_ROUTER_API_KEY")
                ?: error("OPEN_ROUTER_API_KEY not defined")

        val openRouterModel: String
            get() = properties.getProperty("OPEN_ROUTER_MODEL")
                ?: error("OPEN_ROUTER_MODEL not defined")
    }

    fun load(path: String = "config.properties"): Config {
        val file = File(path)

        if (file.exists()) {
            FileInputStream(file).use { properties.load(it) }
        } else {
            fun env(name: String) =
                System.getenv(name) ?: error("Env $name não definida")

            properties["TOKEN"] = env("TOKEN")
            properties["RIOT_API_KEY"] = env("RIOT_API_KEY")
            properties["DATABASE_URL"] = env("DATABASE_URL")
            properties["OPENROUTER_API_KEY"] = env("OPEN_ROUTER_API_KEY")
            properties["OPEN_ROUTER_MODEL"] = "konos"
        }

        return this
    }
}
