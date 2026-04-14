package me.igorunderplayer.kono.commands.text.testing

import dev.kord.core.event.message.MessageCreateEvent
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
import kotlin.random.Random

class FightCommand(
    private val buildUnitHandler: BuildUnitHandler,
    private val cardRepository: CardRepository
) : BaseCommand(
    name = "fight",
    description = "Fight against an enemy",
    category = CommandCategory.Game
) {

    override suspend fun run(event: MessageCreateEvent, args: Array<String>) {
        val discordId = event.message.author?.id?.value?.toLong() ?: return

        val enemyName = args.getOrNull(0)?.uppercase()

        if (enemyName == null) {
            event.message.channel.createMessage("⚠️ Use: `!fight <enemy_id>`")
            return
        }

        // 🧠 build player
        val player = try {
            buildUnitHandler.executeByDiscordId(discordId)
        } catch (e: Exception) {
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

        val state = CombatState(
            teams = listOf(
                Team("player", mutableListOf(player)),
                Team("enemy", mutableListOf(enemy))
            ),
            rng = Random.Default
        )

        val result = CombatEngine.run(state)

        val playerAlive = result.teams[0].units.any { it.hp > 0 }

        val content = buildString {
            appendLine("⚔️ **Combate iniciado!**\n")
            appendLine("👤 Player: ${player.card.name} (${player.hp.toInt()} HP)")
            appendLine("👹 Enemy: ${enemy.card.name} (${enemy.hp.toInt()} HP)\n")

            appendLine(
                if (playerAlive) "🏆 **Você venceu!**"
                else "💀 **Você perdeu...**"
            )
        }

        event.message.channel.createMessage(content)
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
}
