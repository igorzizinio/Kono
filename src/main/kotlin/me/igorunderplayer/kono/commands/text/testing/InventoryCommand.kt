package me.igorunderplayer.kono.commands.text.testing

import dev.kord.core.behavior.reply
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.rest.builder.message.embed
import me.igorunderplayer.kono.commands.BaseCommand
import me.igorunderplayer.kono.commands.CommandCategory
import me.igorunderplayer.kono.data.repositories.CardInstanceRepository
import me.igorunderplayer.kono.data.repositories.CardRepository
import me.igorunderplayer.kono.domain.card.CardType
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
            event.message.reply { content = "📦 Seu inventário está vazio." }
            return
        }

        // agrupar por definição
        val grouped = instances.groupBy { it.definitionId }
        val equippedSlots = cardInstanceRepository
            .getEquippedItemsForActiveCharacter(discordId)
            .associate { it.cardInstanceId to it.slot }

        val entries = grouped.mapNotNull { (definitionId, list) ->
            val def = cardRepository.getDefinition(definitionId) ?: return@mapNotNull null
            Triple(def, list.size, list)
        }.sortedByDescending { it.first.rarity.ordinal }

        val totalPages = (entries.size + PAGE_SIZE - 1) / PAGE_SIZE
        val safePage = page.coerceAtMost(totalPages)

        val pageItems = entries
            .drop((safePage - 1) * PAGE_SIZE)
            .take(PAGE_SIZE)

        val inventoryBody = buildString {

            pageItems.forEach { (def, count, list) ->
                val rarityEmoji = resolveRarityEmoji(def.rarity)
                val cardTypeEmoji = resolveCardTypeEmoji(def.type)

                appendLine(
                    "$rarityEmoji $cardTypeEmoji **${def.name}** (${def.rarity}) x$count"
                )

                list.forEach { instance ->
                    val slot = equippedSlots[instance.id]?.plus(1)
                    val status = slot?.let { "equipado no slot $it" } ?: "livre"
                    appendLine("   • #${instance.id} - $status")
                }
            }
        }

        event.message.reply {
            embed {
                title = "📦 Seu Inventário"
                description = inventoryBody.ifBlank { "Nenhuma carta nesta página." }
                footer {
                    text = "Página $safePage/$totalPages • Use inventory <página>"
                }
            }
        }
    }

    private fun resolveRarityEmoji(rarity: Rarity): String = when (rarity) {
        Rarity.COMMON -> "▫️"
        Rarity.RARE -> "🔹"
        Rarity.EPIC -> "🟣"
        Rarity.LEGENDARY -> "🟠"
        Rarity.MYTHIC -> "🔥"
    }

    private fun resolveCardTypeEmoji(cardType: CardType): String = when (cardType) {
        CardType.CHARACTER -> "👤"
        CardType.EQUIPMENT -> "🎒"
    }
}
