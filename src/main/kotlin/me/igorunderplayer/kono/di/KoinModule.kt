package me.igorunderplayer.kono.di

import me.igorunderplayer.kono.Config
import me.igorunderplayer.kono.data.DatabaseManager
import me.igorunderplayer.kono.data.repositories.UserRepository
import me.igorunderplayer.kono.services.RiotService
import me.igorunderplayer.kono.services.UserService
import org.koin.dsl.module

val appModule = module {
    single { DatabaseManager(Config.databaseUrl, Config.databaseUser, Config.databasePassword) }
    single { get<DatabaseManager>().db }
    single { UserRepository(get<DatabaseManager>().db) }
    single { UserService() }
    single { RiotService(Config.riotApiKey) }
}