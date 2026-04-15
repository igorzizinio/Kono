package me.igorunderplayer.kono.commands.text.testing

import dev.kord.core.behavior.channel.createEmbed
import dev.kord.core.event.message.MessageCreateEvent
import kotlinx.coroutines.delay
import me.igorunderplayer.kono.commands.BaseCommand
import me.igorunderplayer.kono.commands.CommandCategory
import me.igorunderplayer.kono.data.entities.CardDefinition
import me.igorunderplayer.kono.data.repositories.CardRepository
import me.igorunderplayer.kono.domain.card.CardType
import me.igorunderplayer.kono.domain.card.Stat
import me.igorunderplayer.kono.domain.gameplay.CombatState
import me.igorunderplayer.kono.domain.gameplay.Team
import me.igorunderplayer.kono.domain.team.BuildUnitHandler
import me.igorunderplayer.kono.engine.combat.CombatEngine
import me.igorunderplayer.kono.domain.gameplay.Unit
import me.igorunderplayer.kono.utils.getMentionedUser
import kotlin.random.Random
import kotlin.time.Duration.Companion.milliseconds

class FightCommand(
    private val buildUnitHandler: BuildUnitHandler,
    private val cardRepository: CardRepository
) : BaseCommand(
    name = "fight",
    description = "Luta contra um inimigo",
    category = CommandCategory.Game
) {

    companion object {
        private const val EMBED_DESCRIPTION_LIMIT = 3500
        private const val EMBED_SEND_COOLDOWN_MS = 700L
    }

    override suspend fun run(event: MessageCreateEvent, args: Array<String>) {
        val discordId = event.message.author?.id?.value?.toLong() ?: return


        val enemyUser = getMentionedUser(event.message, args)

        if (enemyUser == null) {
            fightWithBot(event, args)
            return
        }

        // 🧠 build player
        val player = try {
            buildUnitHandler.executeByDiscordId(discordId)
        } catch (_: Exception) {
            event.message.channel.createMessage("❌ Você precisa selecionar um personagem.")
            return
        }

        // build enemy
        val enemy = try {
            buildUnitHandler.executeByDiscordId(enemyUser.id.value.toLong())
        } catch (_: Exception) {
            event.message.channel.createMessage("❌ Seu inimigo ainda não selecionou um personagem..")
            return
        }

        val playerOwnerName = event.message.author?.username ?: "Jogador"
        val enemyOwnerName = enemyUser.username

        runCombatAndSendEmbeds(
            event = event,
            player = player,
            enemy = enemy,
            playerOwnerName = playerOwnerName,
            enemyOwnerName = enemyOwnerName
        )

    }

    private suspend fun fightWithBot(event: MessageCreateEvent, args: Array<String>) {
        val discordId = event.message.author?.id?.value?.toLong() ?: return
        val enemyName = args.getOrNull(0)?.uppercase()

        if (enemyName == null) {
            event.message.channel.createMessage("⚠️ Use: `!fight <enemy_id>`")
            return
        }

        // 🧠 build player
        val player = try {
            buildUnitHandler.executeByDiscordId(discordId)
        } catch (_: Exception) {
            event.message.channel.createMessage("❌ Você precisa selecionar um personagem.")
            return
        }



        // 🧠 buscar inimigo no DB
        val enemyDef = cardRepository.getDefinition(enemyName)

        if (enemyDef == null || enemyDef.type != CardType.CHARACTER) {
            event.message.channel.createMessage("❌ Inimigo inválido.")
            return
        }

        val enemy = createUnitFromDefinition(enemyDef)

        val playerOwnerName = event.message.author?.username ?: "Jogador"

        runCombatAndSendEmbeds(
            event = event,
            player = player,
            enemy = enemy,
            playerOwnerName = playerOwnerName,
            enemyOwnerName = "Bot"
        )
    }

    // 🔥 transforma CardDefinition em Unit (inimigo)
    private fun createUnitFromDefinition(def: CardDefinition): Unit {
        val stats = def.baseStats.toMutableMap()

        return Unit(
            id = "enemy_${def.id}",
            card = def,
            hp = stats[Stat.HP] ?: 100.0,
            stats = stats,
            abilities = def.abilities.toList(),
            tags = def.tags
        )
    }

    private suspend fun runCombatAndSendEmbeds(
        event: MessageCreateEvent,
        player: Unit,
        enemy: Unit,
        playerOwnerName: String,
        enemyOwnerName: String
    ) {
        val (playerDisplayName, enemyDisplayName) = resolveCombatantDisplayNames(
            playerName = player.card.name,
            enemyName = enemy.card.name,
            playerOwnerName = playerOwnerName,
            enemyOwnerName = enemyOwnerName
        )

        val state = CombatState(
            teams = listOf(
                Team("player", mutableListOf(player)),
                Team("enemy", mutableListOf(enemy))
            ),
            rng = Random.Default
        )

        state.unitDisplayNamesById[player.id] = playerDisplayName
        state.unitDisplayNamesById[enemy.id] = enemyDisplayName

        val playerStartHp = player.hp
        val enemyStartHp = enemy.hp

        val result = CombatEngine.run(state)
        val playerAlive = result.teams[0].units.any { it.hp > 0 }

        val embeds = buildCombatEmbeds(
            playerDisplayName = playerDisplayName,
            enemyDisplayName = enemyDisplayName,
            playerStartHp = playerStartHp,
            enemyStartHp = enemyStartHp,
            playerFinalHp = player.hp,
            enemyFinalHp = enemy.hp,
            playerAlive = playerAlive,
            eventLog = result.combatLog
        )

        sendEmbedsWithCooldown(event, embeds)
    }

    private fun buildCombatEmbeds(
        playerDisplayName: String,
        enemyDisplayName: String,
        playerStartHp: Double,
        enemyStartHp: Double,
        playerFinalHp: Double,
        enemyFinalHp: Double,
        playerAlive: Boolean,
        eventLog: List<String>
    ): List<CombatEmbedPage> {
        val summaryDescription = buildString {
            appendLine("⚔️ **Combate iniciado!**")
            appendLine("👤 **Jogador:** $playerDisplayName (${playerStartHp.toInt()} HP)")
            appendLine("👹 **Inimigo:** $enemyDisplayName (${enemyStartHp.toInt()} HP)")
            appendLine()
            appendLine(if (playerAlive) "🏆 **Resultado:** Voce venceu!" else "💀 **Resultado:** Voce perdeu...")
            appendLine("❤️ **HP final Jogador:** ${playerFinalHp.coerceAtLeast(0.0).toInt()}")
            appendLine("💔 **HP final Inimigo:** ${enemyFinalHp.coerceAtLeast(0.0).toInt()}")
        }

        val pages = mutableListOf(
            CombatEmbedPage(
                title = "⚔️ Resultado do Combate",
                description = summaryDescription.trim(),
                footer = null
            )
        )

        val eventPages = paginateEventLog(eventLog)
        val totalPages = eventPages.size

        eventPages.forEachIndexed { index, page ->
            pages += CombatEmbedPage(
                title = "📜 Diario de Batalha",
                description = page,
                footer = "Pagina ${index + 1}/$totalPages"
            )
        }

        return pages
    }

    private fun resolveCombatantDisplayNames(
        playerName: String,
        enemyName: String,
        playerOwnerName: String,
        enemyOwnerName: String
    ): Pair<String, String> {
        if (!playerName.equals(enemyName, ignoreCase = true)) {
            return playerName to enemyName
        }

        return "$playerName de ${playerOwnerName.trim()}" to "$enemyName de ${enemyOwnerName.trim()}"
    }

    private suspend fun sendEmbedsWithCooldown(
        event: MessageCreateEvent,
        pages: List<CombatEmbedPage>
    ) {
        pages.forEachIndexed { index, page ->
            event.message.channel.createEmbed {
                title = page.title
                description = page.description
                page.footer?.let { footerText ->
                    footer {
                        text = footerText
                    }
                }
            }

            if (index != pages.lastIndex) {
                delay(EMBED_SEND_COOLDOWN_MS.milliseconds)
            }
        }
    }

    private fun paginateEventLog(eventLog: List<String>): List<String> {
        val lines = if (eventLog.isEmpty()) {
            listOf("ℹ️ Nenhum evento foi registrado durante a luta.")
        } else {
            eventLog
        }

        val pages = mutableListOf<String>()
        val current = StringBuilder()

        for (rawLine in lines) {
            val line = if (rawLine.length > EMBED_DESCRIPTION_LIMIT - 8) {
                "${rawLine.take(EMBED_DESCRIPTION_LIMIT - 11)}..."
            } else {
                rawLine
            }

            val formattedLine = "• $line"

            if (current.isNotEmpty() && current.length + formattedLine.length + 1 > EMBED_DESCRIPTION_LIMIT) {
                pages += current.toString()
                current.clear()
            }

            if (current.isNotEmpty()) current.append('\n')
            current.append(formattedLine)
        }

        if (current.isNotEmpty()) {
            pages += current.toString()
        }

        return pages
    }

    private data class CombatEmbedPage(
        val title: String,
        val description: String,
        val footer: String?
    )
}
