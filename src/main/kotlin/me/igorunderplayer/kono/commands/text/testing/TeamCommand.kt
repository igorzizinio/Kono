package me.igorunderplayer.kono.commands.text.testing

import dev.kord.core.behavior.reply
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.rest.builder.message.embed
import me.igorunderplayer.kono.commands.BaseCommand
import me.igorunderplayer.kono.commands.CommandCategory
import me.igorunderplayer.kono.data.repositories.BattleTeamRepository
import me.igorunderplayer.kono.data.repositories.CardInstanceRepository
import me.igorunderplayer.kono.data.repositories.UserRepository
import me.igorunderplayer.kono.domain.card.CardType

class TeamCommand(
    private val userRepository: UserRepository,
    private val cardInstanceRepository: CardInstanceRepository,
    private val battleTeamRepository: BattleTeamRepository
) : BaseCommand(
    name = "team",
    description = "monta e visualiza seu time de 3 personagens",
    category = CommandCategory.Game
) {

    override suspend fun run(event: MessageCreateEvent, args: Array<String>) {
        val discordId = event.message.author?.id?.value?.toLong() ?: return
        val action = args.firstOrNull()?.lowercase() ?: "view"

        when (action) {
            "view", "show", "list" -> showTeam(event, discordId)
            "set" -> setSlot(event, discordId, args)
            "clear", "remove" -> clearSlot(event, discordId, args)
            else -> showUsage(event)
        }
    }

    private suspend fun showTeam(event: MessageCreateEvent, discordId: Long) {
        val user = userRepository.getUserByDiscordId(discordId)
        if (user == null) {
            event.message.reply { content = "❌ Você ainda não possui registro." }
            return
        }

        val slots = battleTeamRepository.getTeamByUserId(user.id).sortedBy { it.slot }
        val lines = (1..3).map { slot ->
            val row = slots.firstOrNull { it.slot == slot }
            if (row == null) {
                "Slot $slot: vazio"
            } else {
                val character = cardInstanceRepository.getOwnedCharacterWithDefinition(user.id, row.characterInstanceId)
                if (character == null) {
                    "Slot $slot: personagem ausente (id ${row.characterInstanceId})"
                } else {
                    val (instance, definition) = character
                    "Slot $slot: ${definition.name} (instância ${instance.id}, nível ${instance.level})"
                }
            }
        }

        event.message.reply {
            embed {
                title = "👥 Seu Time"
                description = buildString {
                    appendLine(lines.joinToString("\n"))
                    appendLine()
                    appendLine("Use `team set <slot> <instance_id>` para alterar sua formação.")
                    appendLine("Os slots vão de 1 a 3.")
                }
            }
        }
    }

    private suspend fun setSlot(event: MessageCreateEvent, discordId: Long, args: Array<String>) {
        val slot = args.getOrNull(1)?.toIntOrNull()
        val instanceId = args.getOrNull(2)?.toIntOrNull()

        if (slot == null || instanceId == null || slot !in 1..3) {
            event.message.reply { content = "❌ Uso: `team set <slot 1-3> <instance_id>`" }
            return
        }

        val user = userRepository.getUserByDiscordId(discordId)
        if (user == null) {
            event.message.reply { content = "❌ Você ainda não possui registro." }
            return
        }

        val character = cardInstanceRepository.getOwnedCharacterWithDefinition(user.id, instanceId)
            ?: run {
                event.message.reply { content = "❌ Esse personagem não existe ou não pertence a você." }
                return
            }

        if (character.second.type != CardType.CHARACTER) {
            event.message.reply { content = "❌ Apenas personagens podem entrar no time." }
            return
        }

        battleTeamRepository.setSlot(user.id, slot, instanceId)

        event.message.reply {
            content = "✅ ${character.second.name} foi definido no slot $slot do seu time."
        }
    }

    private suspend fun clearSlot(event: MessageCreateEvent, discordId: Long, args: Array<String>) {
        val slot = args.getOrNull(1)?.toIntOrNull()
        if (slot == null || slot !in 1..3) {
            event.message.reply { content = "❌ Uso: `team clear <slot 1-3>`" }
            return
        }

        val user = userRepository.getUserByDiscordId(discordId)
        if (user == null) {
            event.message.reply { content = "❌ Você ainda não possui registro." }
            return
        }

        val deleted = battleTeamRepository.clearSlot(user.id, slot)
        event.message.reply {
            content = if (deleted) {
                "✅ Slot $slot removido do seu time."
            } else {
                "⚠️ O slot $slot já estava vazio."
            }
        }
    }

    private suspend fun showUsage(event: MessageCreateEvent) {
        event.message.reply {
            content = "Uso: `team view`, `team set <slot 1-3> <instance_id>`, `team clear <slot 1-3>`"
        }
    }
}



