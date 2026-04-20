package me.igorunderplayer.kono.commands.text.testing

import dev.kord.common.entity.ButtonStyle
import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.behavior.interaction.response.createEphemeralFollowup
import dev.kord.core.behavior.reply
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.rest.builder.component.ActionRowBuilder
import dev.kord.rest.builder.message.embed
import kotlinx.coroutines.time.delay
import me.igorunderplayer.kono.commands.BaseCommand
import me.igorunderplayer.kono.commands.CommandCategory
import me.igorunderplayer.kono.domain.battle.EnemyTeamCatalog
import me.igorunderplayer.kono.domain.gameplay.CombatState
import me.igorunderplayer.kono.domain.gameplay.Team
import me.igorunderplayer.kono.engine.combat.CombatEngine
import me.igorunderplayer.kono.services.TeamBattleService
import me.igorunderplayer.kono.utils.getMentionedUser
import me.igorunderplayer.kono.utils.interaction.awaitButtonInteraction
import java.time.Duration
import kotlin.random.Random

class TeamFightCommand(
    private val teamBattleService: TeamBattleService
) : BaseCommand(
    name = "teamfight",
    description = "luta 3v3 usando seu time salvo",
    category = CommandCategory.Game
) {

    companion object {
        private const val EMBED_DESCRIPTION_LIMIT = 3500
    }

    override suspend fun run(event: MessageCreateEvent, args: Array<String>) {
        val author = event.message.author ?: return
        val discordId = author.id.value.toLong()
        val mention = getMentionedUser(event.message, args)?.takeIf { it.id != author.id }

        val playerRoster = when (val result = teamBattleService.buildPlayerRoster(discordId, author.username)) {
            is TeamBattleService.RosterResult.Success -> result
            is TeamBattleService.RosterResult.Failure -> {
                event.message.reply { content = result.message }
                return
            }
        }

        val enemyRoster = when {
            mention != null -> {
                when (val result = teamBattleService.buildPlayerRoster(mention.id.value.toLong(), mention.username)) {
                    is TeamBattleService.RosterResult.Success -> result
                    is TeamBattleService.RosterResult.Failure -> {
                        event.message.reply { content = "❌ ${mention.username}: ${result.message}" }
                        return
                    }
                }
            }

            else -> {
                val botId = resolveBotId(args)
                    ?: run {
                        event.message.reply { content = buildBotHelp() }
                        return
                    }

                when (val result = teamBattleService.buildBotRoster(botId)) {
                    is TeamBattleService.RosterResult.Success -> result
                    is TeamBattleService.RosterResult.Failure -> {
                        event.message.reply { content = result.message }
                        return
                    }
                }
            }
        }

        val state = CombatState(
            teams = listOf(
                Team("player", playerRoster.units.toMutableList()),
                Team("enemy", enemyRoster.units.toMutableList())
            ),
            rng = Random.Default
        )

        playerRoster.units.forEach { state.unitDisplayNamesById[it.id] = it.card.name }
        enemyRoster.units.forEach { state.unitDisplayNamesById[it.id] = it.card.name }

        val engine = CombatEngine(state)
        engine.run()

        val playerAlive = state.teams.firstOrNull { it.id == "player" }?.units?.any { it.hp > 0 } == true
        val enemyAlive = state.teams.firstOrNull { it.id == "enemy" }?.units?.any { it.hp > 0 } == true

        val rewardText = if (
            playerAlive && !enemyAlive &&
            enemyRoster.battleKey != null &&
            enemyRoster.essenceReward != null &&
            enemyRoster.ownerUserId == null &&
            playerRoster.ownerUserId != null
        ) {
            val rewarded = teamBattleService.grantFirstVictoryReward(
                userId = playerRoster.ownerUserId,
                battleKey = enemyRoster.battleKey,
                reward = enemyRoster.essenceReward
            )

            if (rewarded) {
                "\n\n💎 Recompensa de primeira vitória: +${enemyRoster.essenceReward} essence"
            } else {
                "\n\nℹ️ Você já recebeu a recompensa dessa batalha."
            }
        } else {
            ""
        }

        val resultText = when {
            playerAlive && !enemyAlive -> "🏆 Você venceu!"
            enemyAlive && !playerAlive -> "💀 Você perdeu..."
            else -> "🤝 Empate"
        }

        val logPages = buildCombatLogEmbeds(state.combatLog)
        val logButtonId = "teamfight-log-${event.message.channelId}-${event.message.id}-${System.currentTimeMillis()}"

        event.message.reply {
            embed {
                title = "⚔️ Team Fight"
                description = buildString {
                    appendLine("**$resultText**")
                    appendLine()
                    appendLine("**Seu time:**")
                    playerRoster.units.forEachIndexed { index, unit ->
                        appendLine("${index + 1}. ${unit.card.name} - ${unit.hp.coerceAtLeast(0.0).toInt()} HP")
                    }
                    appendLine()
                    appendLine("**Inimigo:** ${enemyRoster.displayName}")
                    enemyRoster.units.forEachIndexed { index, unit ->
                        appendLine("${index + 1}. ${unit.card.name} - ${unit.hp.coerceAtLeast(0.0).toInt()} HP")
                    }
                    if (rewardText.isNotBlank()) {
                        appendLine()
                        appendLine(rewardText)
                    }
                }
            }

            addComponent(createLogButton(logButtonId))
        }

        val logClick = event.kord.awaitButtonInteraction(
            customId = logButtonId,
            allowedUserId = author.id.value.toLong()
        ) ?: return

        val firstPage = logPages.first()

        val response = logClick.interaction.respondEphemeral {
            content = "📜 Diario de batalha"
            embed {
                title = firstPage.title
                description = firstPage.description
                firstPage.footer?.let { footerText ->
                    footer {
                        text = footerText
                    }
                }
            }
        }

        logPages.subList(1, logPages.size).forEach { page ->
            response.createEphemeralFollowup {
                embed {
                    title = page.title
                    description = page.description
                    page.footer?.let { footerText ->
                        footer {
                            text = footerText
                        }
                    }
                }
            }

            delay(Duration.ofMillis(250))
        }
    }

    private fun buildCombatLogEmbeds(eventLog: List<String>): List<CombatEmbedPage> {
        val eventPages = paginateEventLog(eventLog)
        val totalPages = eventPages.size

        return eventPages.mapIndexed { index, page ->
            CombatEmbedPage(
                title = "📜 Diario de Batalha",
                description = page,
                footer = "Pagina ${index + 1}/$totalPages"
            )
        }
    }

    private fun paginateEventLog(eventLog: List<String>): List<String> {
        val lines = eventLog.ifEmpty {
            listOf("ℹ️ Nenhum evento foi registrado durante a luta.")
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

    private fun createLogButton(customButtonId: String) = ActionRowBuilder().apply {
        interactionButton(
            ButtonStyle.Primary,
            customButtonId
        ) {
            label = "Ver diario"
        }
    }

    private fun resolveBotId(args: Array<String>): String? {
        if (args.isEmpty()) return null
        if (args[0].equals("bot", ignoreCase = true)) return args.getOrNull(1)?.uppercase()
        return args[0].uppercase()
    }

    private fun buildBotHelp(): String {
        val teams = EnemyTeamCatalog.all().joinToString("\n") { team ->
            "• `${team.id.lowercase()}` — ${team.name} (+${team.essenceReward} essence na primeira vitória)"
        }

        return buildString {
            appendLine("⚠️ Use `teamfight <@usuario>` para desafiar outro jogador ou `teamfight <bot_id>` para lutar contra bots.")
            appendLine()
            appendLine("Bots disponíveis:")
            appendLine(teams)
        }
    }

    private data class CombatEmbedPage(
        val title: String,
        val description: String,
        val footer: String?
    )
}



