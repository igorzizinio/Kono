package me.igorunderplayer.kono.commands.slash.game.subcommand

import dev.kord.common.entity.ApplicationCommandOption
import dev.kord.common.entity.ApplicationCommandOptionType
import dev.kord.common.entity.ButtonStyle
import dev.kord.common.entity.DiscordPartialEmoji
import dev.kord.common.entity.optional.OptionalBoolean
import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.behavior.interaction.response.createEphemeralFollowup
import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import dev.kord.rest.builder.component.ActionRowBuilder
import dev.kord.rest.builder.message.embed
import kotlinx.coroutines.time.delay
import me.igorunderplayer.kono.commands.KonoSlashSubCommand
import me.igorunderplayer.kono.domain.gameplay.CombatState
import me.igorunderplayer.kono.domain.gameplay.Team
import me.igorunderplayer.kono.engine.combat.CombatEngine
import me.igorunderplayer.kono.services.TeamBattleService
import me.igorunderplayer.kono.utils.interaction.awaitButtonInteraction
import java.time.Duration
import kotlin.random.Random

class BatalhaJogador(
    private val teamBattleService: TeamBattleService
) : KonoSlashSubCommand {
    override val name = "jogador"
    override val description = "Desafia outro jogador para uma batalha de times"
    override val options = listOf(
        ApplicationCommandOption(
            name = "oponente",
            description = "O jogador que você quer desafiar",
            type = ApplicationCommandOptionType.User,
            required = OptionalBoolean.Value(true),
        )
    )

    override suspend fun run(event: ChatInputCommandInteractionCreateEvent) {
        val deferred = event.interaction.deferPublicResponse()
        val discordId = event.interaction.user.id.value.toLong()
        val username = event.interaction.user.username

        val opponent = event.interaction.command.users["oponente"]
        if (opponent == null || opponent.id == event.interaction.user.id) {
            deferred.respond { content = "❌ Selecione um oponente válido (não pode ser você mesmo)." }
            return
        }

        val playerRoster = when (val result = teamBattleService.buildPlayerRoster(discordId, username)) {
            is TeamBattleService.RosterResult.Success -> result
            is TeamBattleService.RosterResult.Failure -> {
                deferred.respond { content = result.message }
                return
            }
        }

        val opponentId = opponent.id.value.toLong()
        val enemyRoster = when (val result = teamBattleService.buildPlayerRoster(opponentId, opponent.username)) {
            is TeamBattleService.RosterResult.Success -> result
            is TeamBattleService.RosterResult.Failure -> {
                deferred.respond { content = "❌ **${opponent.username}**: ${result.message}" }
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

        val resultText = when {
            playerAlive && !enemyAlive -> "🏆 **$username** venceu!"
            enemyAlive && !playerAlive -> "🏆 **${opponent.username}** venceu!"
            else -> "🤝 Empate!"
        }

        val logButtonId = "batalha-pvp-log-${discordId}-${System.currentTimeMillis()}"

        deferred.respond {
            embed {
                title = "⚔️ Team Fight PvP — $username vs ${opponent.username}"
                description = buildString {
                    appendLine("**$resultText**")
                    appendLine()
                    appendLine("**$username:**")
                    playerRoster.units.forEachIndexed { i, u ->
                        appendLine("${i + 1}. ${u.card.name} — ${u.hp.coerceAtLeast(0.0).toInt()} HP")
                    }
                    appendLine()
                    appendLine("**${opponent.username}:**")
                    enemyRoster.units.forEachIndexed { i, u ->
                        appendLine("${i + 1}. ${u.card.name} — ${u.hp.coerceAtLeast(0.0).toInt()} HP")
                    }
                }
            }
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
