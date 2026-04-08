package me.igorunderplayer.kono

import dev.kord.core.Kord
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import me.igorunderplayer.kono.di.appModule
import org.koin.core.context.startKoin
import org.koin.dsl.module

object Launcher {

    @JvmStatic
    fun main(args: Array<String>) = runBlocking {
        Config().load()

        val kord = Kord(Config.token)

        val koin = startKoin {
            modules(
                appModule,
                module {
                    single { kord }
                }
            )
        }.koin

        val botJob = launch {
            koin.get<Kono>().start()
        }

        val serverJob = launch {
            Server().start()
        }

        joinAll(botJob, serverJob)
    }
}
