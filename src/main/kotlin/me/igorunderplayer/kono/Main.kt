package me.igorunderplayer.kono

import dev.kord.core.Kord
import kotlinx.coroutines.*
import me.igorunderplayer.kono.di.appModule
import me.igorunderplayer.kono.events.EventManager
import org.koin.core.context.startKoin
import org.koin.dsl.module

object Launcher {
    @OptIn(DelicateCoroutinesApi::class)
    @JvmStatic
    fun main(args: Array<String>) {
        runBlocking {

            Config().load()

            val koinApp = startKoin {
                modules(appModule)
            }

            val koin = koinApp.koin

            val botJob = launch {

                val kord = Kord(Config.token)

                koin.loadModules(
                    listOf(
                        module {
                            single { kord }
                            single { EventManager(get()) }
                        }
                    )
                )

                val kono = Kono()
                kono.start(koin)
            }

            val serverJob = launch {
                Server().start()
            }

            joinAll(botJob, serverJob)
        }
    }
}
