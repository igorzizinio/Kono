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
import me.igorunderplayer.kono.utils.interaction.awaitButtonInteraction
import java.time.Duration
import kotlin.random.Random

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
            event.message.channel.createMessage("⚠️ Use: `fight <enemy_id> (exemplo: slime)`")
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

        val result = CombatEngine.runAutonomous(state)
        val playerAlive = result.teams[0].units.any { it.hp > 0 }

        val summary = buildCombatSummaryEmbed(
            playerDisplayName = playerDisplayName,
            enemyDisplayName = enemyDisplayName,
            playerStartHp = playerStartHp,
            enemyStartHp = enemyStartHp,
            playerFinalHp = player.hp,
            enemyFinalHp = enemy.hp,
            playerAlive = playerAlive,
        )

        val logPages = buildCombatLogEmbeds(result.combatLog)
        val logButtonId = "fight-log-${event.message.channelId}-${event.message.id}-${System.currentTimeMillis()}"

        event.message.reply {
            embed {
                title = summary.title
                description = summary.description
                summary.footer?.let { footerText ->
                    footer {
                        text = footerText
                    }
                }
            }

            addComponent(createLogButton(logButtonId))
        }

        val logClick = event.kord.awaitButtonInteraction(
            customId = logButtonId,
            allowedUserId = event.message.author?.id?.value?.toLong() ?: return
        ) ?: return

        val firstPage = logPages.first()

        val response = logClick.interaction.respondEphemeral {
            content = "📜 Diário de batalha"
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

            delay(Duration.ofMillis(250)) // delay para não enviar tudo de uma vez
        }
    }

    private fun buildCombatSummaryEmbed(
        playerDisplayName: String,
        enemyDisplayName: String,
        playerStartHp: Double,
        enemyStartHp: Double,
        playerFinalHp: Double,
        enemyFinalHp: Double,
        playerAlive: Boolean,
    ): CombatEmbedPage {
        val summaryDescription = buildString {
            appendLine("⚔️ **Combate iniciado!**")
            appendLine("👤 **Jogador:** $playerDisplayName (${playerStartHp.toInt()} HP)")
            appendLine("👹 **Inimigo:** $enemyDisplayName (${enemyStartHp.toInt()} HP)")
            appendLine()
            appendLine(if (playerAlive) "🏆 **Resultado:** Voce venceu!" else "💀 **Resultado:** Voce perdeu...")
            appendLine("❤️ **HP final Jogador:** ${playerFinalHp.coerceAtLeast(0.0).toInt()}")
            appendLine("💔 **HP final Inimigo:** ${enemyFinalHp.coerceAtLeast(0.0).toInt()}")
        }

        return CombatEmbedPage(
            title = "⚔️ Resultado do Combate",
            description = summaryDescription.trim(),
            footer = "Clique no botão abaixo para ver o diário em modo privado"
        )
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

    private fun createLogButton(customButtonId: String) = ActionRowBuilder().apply {
        interactionButton(
            ButtonStyle.Primary,
            customButtonId,
        ) {
            label = "Ver diário"
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

    private data class CombatEmbedPage(
        val title: String,
        val description: String,
        val footer: String?
    )
}
