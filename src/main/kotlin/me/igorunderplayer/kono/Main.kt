package me.igorunderplayer.kono

import kotlinx.coroutines.*
import me.igorunderplayer.kono.di.appModule
import org.koin.core.context.startKoin

object Launcher {
    @OptIn(DelicateCoroutinesApi::class)
    @JvmStatic
    fun main(args: Array<String>) {
        runBlocking {
            Config().load()

            startKoin {
                modules(appModule)
            }

            val botJob = GlobalScope.launch {
                Kono().start()
            }

            botJob.join()
        }
    }
}

