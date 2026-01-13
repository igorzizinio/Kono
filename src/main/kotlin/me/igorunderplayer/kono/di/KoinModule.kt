package me.igorunderplayer.kono.di

import me.igorunderplayer.kono.data.DatabaseManager
import me.igorunderplayer.kono.data.repositories.UserRepository
import me.igorunderplayer.kono.services.RiotService
import me.igorunderplayer.kono.services.UserService
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val appModule = module {
    singleOf(::DatabaseManager)
    singleOf(::UserRepository)
    singleOf(::UserService)
    singleOf(::RiotService)
}
