package me.igorunderplayer.kono

import dev.kord.core.Kord
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import me.igorunderplayer.kono.di.appModule
import me.igorunderplayer.kono.services.ai.AIService
import me.igorunderplayer.kono.services.ai.OpenRouterAIService
import org.koin.core.context.startKoin
import org.koin.dsl.module

object Launcher {

    @JvmStatic
    fun main(args: Array<String>) = runBlocking {
        Config().load()

        val kord = Kord(Config.token)

        val koin = startKoin {
            modules(
                module {
                    single {
                        HttpClient(CIO) {
                            install(ContentNegotiation) {
                                json(
                                    Json {
                                        ignoreUnknownKeys = true
                                        isLenient = true
                                    }
                                )
                            }
                        }
                    }

                    single<AIService> {
                        OpenRouterAIService(
                            client = get(),
                            apiKey = Config.openRouterApiKey,
                            model = Config.openRouterModel
                        )
                    }
                },
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
