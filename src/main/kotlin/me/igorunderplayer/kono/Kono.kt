
package me.igorunderplayer.kono

import dev.kord.core.Kord
import dev.kord.core.entity.Emoji
import dev.kord.gateway.ALL
import dev.kord.gateway.Intents
import dev.kord.gateway.PrivilegedIntent
import me.igorunderplayer.kono.commands.CommandManager
import me.igorunderplayer.kono.events.EventManager
import org.koin.core.Koin
import org.slf4j.LoggerFactory
import kotlin.time.Instant
import kotlin.time.Clock


@OptIn(PrivilegedIntent::class)
class Kono {
    companion object {
        lateinit var kord: Kord
        lateinit var events: EventManager
        lateinit var commands: CommandManager

        lateinit var startupAt: Instant
        lateinit var emojis: List<Emoji>
    }

    private val logger = LoggerFactory.getLogger(this::class.java)

    suspend fun start(koin: Koin) {

        logger.info("Starting up!")

        kord = koin.get()

        logger.info(
            """
             
            [0;31m _  __                        
            [0;32m| |/ /   ___    _ __     ___  
            [0;33m| ' /   / _ \  | '_ \   / _ \ 
            [0;34m| . \  | (_) | | | | | | (_) |
            [0;35m|_|\_\  \___/  |_| |_|  \___/ 
            [0m             
            """.trimIndent()
        )

        logger.info("Starting up!")

        startupAt = Clock.System.now()

        events = koin.get()
        commands = koin.get()

        logger.info("Starting event listeners...")
        events.start()

        logger.info("Starting command manager...")
        commands.start()

        logger.info("Logging in!")
        kord.login {
            intents = Intents.ALL
        }
    }
}
