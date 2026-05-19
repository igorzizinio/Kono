package me.igorunderplayer.kono.commands.slash.game.subcommand

import dev.kord.common.entity.ApplicationCommandOption
import dev.kord.common.entity.ButtonStyle
import dev.kord.common.entity.DiscordPartialEmoji
import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.behavior.interaction.response.createEphemeralFollowup
import dev.kord.core.behavior.interaction.response.edit
import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import dev.kord.rest.builder.component.ActionRowBuilder
import dev.kord.rest.builder.component.option
import dev.kord.rest.builder.message.embed
import kotlinx.coroutines.time.delay
import me.igorunderplayer.kono.commands.KonoSlashSubCommand
import me.igorunderplayer.kono.domain.battle.EnemyTeamCatalog
import me.igorunderplayer.kono.domain.gameplay.CombatState
import me.igorunderplayer.kono.domain.gameplay.Team
import me.igorunderplayer.kono.engine.combat.CombatEngine
import me.igorunderplayer.kono.services.TeamBattleService
import me.igorunderplayer.kono.utils.interaction.awaitButtonInteraction
import me.igorunderplayer.kono.utils.interaction.awaitStringSelectInteraction
import java.time.Duration
import kotlin.random.Random

class BatalhaBot(
    private val teamBattleService: TeamBattleService
) : KonoSlashSubCommand {
    override val name = "bot"
    override val description = "Luta contra um time inimigo do catálogo"
    override val options: List<ApplicationCommandOption> = listOf()

    override suspend fun run(event: ChatInputCommandInteractionCreateEvent) {
        val deferred = event.interaction.deferPublicResponse()
        val discordId = event.interaction.user.id.value.toLong()
        val username = event.interaction.user.username

        val playerRoster = when (val result = teamBattleService.buildPlayerRoster(discordId, username)) {
            is TeamBattleService.RosterResult.Success -> result
            is TeamBattleService.RosterResult.Failure -> {
                deferred.respond { content = result.message }
                return
            }
        }

        val selectId = "batalha-bot-sel-${discordId}-${System.currentTimeMillis()}"

        val response = deferred.respond {
            embed {
                title = "⚔️ Batalha — Escolha o inimigo"
                description = "Selecione um time inimigo para desafiar."
            }
            addComponent(ActionRowBuilder().apply {
                stringSelect(selectId) {
                    placeholder = "Escolha um time inimigo..."
                    EnemyTeamCatalog.all().take(25).forEach { team ->
                        option(label = team.name, value = team.id) {
                            description = "${team.description} • +${team.essenceReward} 💎 1ª vitória".take(100)
                        }
                    }
                }
            })
        }

        val selectEvent = event.kord.awaitStringSelectInteraction(selectId, discordId) ?: run {
            response.edit { content = "⏰ Tempo esgotado."; components = mutableListOf() }
            return
        }

        val teamId = selectEvent.interaction.values.firstOrNull() ?: run {
            response.edit { content = "❌ Seleção inválida."; components = mutableListOf() }
            return
        }

        val selectUpdate = selectEvent.interaction.deferPublicMessageUpdate()

        val enemyRoster = when (val result = teamBattleService.buildBotRoster(teamId)) {
            is TeamBattleService.RosterResult.Success -> result
            is TeamBattleService.RosterResult.Failure -> {
                selectUpdate.edit { content = result.message; components = mutableListOf() }
                return
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

        CombatEngine(state).run()

        val playerAlive = state.teams.firstOrNull { it.id == "player" }?.units?.any { it.hp > 0 } == true
        val enemyAlive = state.teams.firstOrNull { it.id == "enemy" }?.units?.any { it.hp > 0 } == true

        val rewardText = if (
            playerAlive && !enemyAlive &&
            enemyRoster.battleKey != null &&
            enemyRoster.essenceReward != null &&
            playerRoster.ownerUserId != null
        ) {
            val rewarded = teamBattleService.grantFirstVictoryReward(
                userId = playerRoster.ownerUserId,
                battleKey = enemyRoster.battleKey,
                reward = enemyRoster.essenceReward
            )
            if (rewarded) "\n\n💎 Recompensa de primeira vitória: **+${enemyRoster.essenceReward} essence**"
            else "\n\nℹ️ Você já recebeu a recompensa dessa batalha."
        } else ""

        val resultText = when {
            playerAlive && !enemyAlive -> "🏆 Você venceu!"
            enemyAlive && !playerAlive -> "💀 Você perdeu..."
            else -> "🤝 Empate"
        }

        val logButtonId = "batalha-log-${discordId}-${System.currentTimeMillis()}"

        selectUpdate.edit {
            embed {
                title = "⚔️ Team Fight"
                description = buildString {
                    appendLine("**$resultText**")
                    appendLine()
                    appendLine("**Seu time:**")
                    playerRoster.units.forEachIndexed { i, u ->
                        appendLine("${i + 1}. ${u.card.name} — ${u.hp.coerceAtLeast(0.0).toInt()} HP")
                    }
                    appendLine()
                    appendLine("**Inimigo:** ${enemyRoster.displayName}")
                    enemyRoster.units.forEachIndexed { i, u ->
                        appendLine("${i + 1}. ${u.card.name} — ${u.hp.coerceAtLeast(0.0).toInt()} HP")
                    }
                    if (rewardText.isNotBlank()) append(rewardText)
                }
            }
            components = mutableListOf()
            addComponent(ActionRowBuilder().apply {
                interactionButton(ButtonStyle.Primary, logButtonId) {
                    label = "Ver diário de batalha"
                    emoji = DiscordPartialEmoji(name = "📜")
                }
            })
        }

        val logClick = event.kord.awaitButtonInteraction(logButtonId, discordId) ?: return
        val logPages = buildCombatLogPages(state.combatLog)

        val logResponse = logClick.interaction.respondEphemeral {
            embed {
                title = logPages[0].title
                description = logPages[0].description
                logPages[0].footer?.let { footer { text = it } }
            }
        }

        logPages.drop(1).forEach { page ->
            logResponse.createEphemeralFollowup {
                embed {
                    title = page.title
                    description = page.description
                    page.footer?.let { footer { text = it } }
                }
            }
            delay(Duration.ofMillis(250))
        }
    }

    private data class CombatPage(val title: String, val description: String, val footer: String?)

    private fun buildCombatLogPages(eventLog: List<String>): List<CombatPage> {
        val limit = 3500
        val lines = eventLog.ifEmpty { listOf("ℹ️ Nenhum evento foi registrado.") }
        val pages = mutableListOf<String>()
        val current = StringBuilder()

        for (rawLine in lines) {
            val line = if (rawLine.length > limit - 8) "${rawLine.take(limit - 11)}..." else rawLine
            val formatted = "• $line"
            if (current.isNotEmpty() && current.length + formatted.length + 1 > limit) {
                pages += current.toString()
                current.clear()
            }
            if (current.isNotEmpty()) current.append('\n')
            current.append(formatted)
        }
        if (current.isNotEmpty()) pages += current.toString()

        return pages.mapIndexed { i, text ->
            CombatPage("📜 Diário de Batalha", text, "Página ${i + 1}/${pages.size}")
        }
    }
}
