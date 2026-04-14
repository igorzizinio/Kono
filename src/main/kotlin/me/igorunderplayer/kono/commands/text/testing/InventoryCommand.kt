package me.igorunderplayer.kono.commands.text.testing

import dev.kord.core.event.message.MessageCreateEvent
import me.igorunderplayer.kono.commands.BaseCommand
import me.igorunderplayer.kono.commands.CommandCategory
import me.igorunderplayer.kono.data.repositories.CardInstanceRepository
import me.igorunderplayer.kono.data.repositories.CardRepository
import me.igorunderplayer.kono.domain.card.Rarity

class InventoryCommand(
    private val cardInstanceRepository: CardInstanceRepository,
    private val cardRepository: CardRepository
) : BaseCommand(
    name = "inventory",
    description = "View your cards",
    category = CommandCategory.Game
) {

    private val PAGE_SIZE = 5

    override suspend fun run(event: MessageCreateEvent, args: Array<String>) {
        val discordId = event.message.author?.id?.value?.toLong() ?: return

        val page = args.getOrNull(0)?.toIntOrNull()?.coerceAtLeast(1) ?: 1

        val instances = cardInstanceRepository.getByDiscordId(discordId)

        if (instances.isEmpty()) {
            event.message.channel.createMessage("📦 Seu inventário está vazio.")
            return
        }

        // agrupar por definição
        val grouped = instances.groupBy { it.definitionId }

        val entries = grouped.mapNotNull { (definitionId, list) ->
            val def = cardRepository.getDefinition(definitionId) ?: return@mapNotNull null
            Triple(def, list.size, list)
        }.sortedByDescending { it.first.rarity.ordinal }

        val totalPages = (entries.size + PAGE_SIZE - 1) / PAGE_SIZE
        val safePage = page.coerceAtMost(totalPages)

        val pageItems = entries
            .drop((safePage - 1) * PAGE_SIZE)
            .take(PAGE_SIZE)

        val content = buildString {
            appendLine("📦 **Seu Inventário** (Página $safePage/$totalPages)\n")

            pageItems.forEach { (def, count, _) ->
                val emoji = rarityEmoji(def.rarity)

                appendLine(
                    "$emoji **${def.name}** (${def.rarity}) x$count"
                )
            }

            appendLine("\nUse `!inventory <página>`")
        }

        event.message.channel.createMessage(content)
    }

    private fun rarityEmoji(rarity: Rarity): String {
        return when (rarity) {
            Rarity.COMMON -> "⚪"
            Rarity.RARE -> "🔵"
            Rarity.EPIC -> "🟣"
            Rarity.LEGENDARY -> "🟡"
            Rarity.MYTHIC -> "🔴"
        }
    }
}
