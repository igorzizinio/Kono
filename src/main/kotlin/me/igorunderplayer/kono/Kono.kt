
package me.igorunderplayer.kono

import dev.kord.core.Kord
import dev.kord.gateway.ALL
import dev.kord.gateway.Intents
import dev.kord.gateway.PrivilegedIntent
import me.igorunderplayer.kono.commands.CommandManager
import me.igorunderplayer.kono.events.EventManager
import org.slf4j.LoggerFactory


@OptIn(PrivilegedIntent::class)
class Kono(
    private val kord: Kord,
    private val events: EventManager,
    private val commands: CommandManager
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    suspend fun start() {
        logger.info("Starting up!")
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
