package me.igorunderplayer.kono.commands.text.testing

import dev.kord.core.behavior.reply
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.rest.builder.message.embed
import me.igorunderplayer.kono.commands.BaseCommand
import me.igorunderplayer.kono.commands.CommandCategory
import me.igorunderplayer.kono.data.entities.CardDefinition
import me.igorunderplayer.kono.data.entities.CardInstance
import me.igorunderplayer.kono.data.repositories.CardInstanceRepository
import me.igorunderplayer.kono.data.repositories.CardRepository
import me.igorunderplayer.kono.domain.card.CardType
import me.igorunderplayer.kono.domain.card.toDisplayEmoji

class InventoryCommand(
    private val cardInstanceRepository: CardInstanceRepository,
    private val cardRepository: CardRepository
) : BaseCommand(
    name = "inventory",
    description = "Use inventory item|perso <pagina>",
    category = CommandCategory.Game
) {

    private val PAGE_SIZE = 5

    private data class InventoryEntry(
        val definition: CardDefinition,
        val count: Int,
        val instances: List<CardInstance>
    )

    private data class InventoryFilter(
        val type: CardType,
        val title: String
    )

    override suspend fun run(event: MessageCreateEvent, args: Array<String>) {
        val discordId = event.message.author?.id?.value?.toLong() ?: return
        val filterToken = args.getOrNull(0)?.lowercase()
        if (filterToken == null) {
            event.message.reply { content = "Use: `inventory item <pagina>` ou `inventory perso <pagina>`." }
            return
        }

        val filter = parseFilter(filterToken)
        if (filter == null) {
            event.message.reply { content = "Filtro inválido. Use `item` ou `char`." }
            return
        }

        val page = args.getOrNull(1)?.toIntOrNull()?.coerceAtLeast(1) ?: 1

        val instances = cardInstanceRepository.getByDiscordId(discordId)
        if (instances.isEmpty()) {
            event.message.reply { content = "📦 Seu inventário está vazio." }
            return
        }

        val equippedSlots = cardInstanceRepository
            .getEquippedItemsForActiveCharacter(discordId)
            .associate { it.cardInstanceId to it.slot }

        val entries = instances
            .groupBy { it.definitionId }
            .mapNotNull { (definitionId, list) ->
                val definition = cardRepository.getDefinition(definitionId) ?: return@mapNotNull null
                InventoryEntry(definition = definition, count = list.size, instances = list)
            }
            .sortedWith(compareByDescending<InventoryEntry> { it.definition.rarity.ordinal }.thenBy { it.definition.name })

        val filteredEntries = entries.filter { it.definition.type == filter.type }

        val section = buildSection(
            entries = filteredEntries,
            page = page,
            equippedSlots = equippedSlots
        )

        event.message.reply {
            embed {
                title = "📦 Seu Inventário - ${filter.title}"
                description = section.body.ifBlank { "Nenhuma carta nesta página." }
                footer {
                    text = "Página ${section.safePage}/${section.totalPages} • Use inventory $filterToken <pagina>"
                }
            }
        }
    }

    private fun parseFilter(token: String): InventoryFilter? {
        return when (token) {
            "perso", "personagem", "char", "character" -> InventoryFilter(CardType.CHARACTER, "👤 Personagens")
            "item", "itens", "equip", "equipment" -> InventoryFilter(CardType.EQUIPMENT, "🎒 Equipamentos")
            else -> null
        }
    }

    private data class SectionResult(
        val body: String,
        val safePage: Int,
        val totalPages: Int
    )

    private fun buildSection(
        entries: List<InventoryEntry>,
        page: Int,
        equippedSlots: Map<Int, Int>
    ): SectionResult {
        if (entries.isEmpty()) {
            return SectionResult(body = "*(nenhum)*", safePage = 1, totalPages = 1)
        }

        val totalPages = (entries.size + PAGE_SIZE - 1) / PAGE_SIZE
        val safePage = page.coerceAtMost(totalPages)
        val pageItems = entries
            .drop((safePage - 1) * PAGE_SIZE)
            .take(PAGE_SIZE)

        val body = buildString {
            pageItems.forEach { entry ->
                val rarityEmoji = entry.definition.rarity.toDisplayEmoji()
                appendLine("$rarityEmoji **${entry.definition.name}** (${entry.definition.rarity}) x${entry.count}")

                entry.instances.forEach { instance ->
                    val slot = equippedSlots[instance.id]?.plus(1)
                    val status = slot?.let { "equipado no slot $it" } ?: "livre"
                    appendLine("   • #${instance.id} • Lv.${instance.level} • $status")
                }
            }
        }.trimEnd()

        return SectionResult(body = body, safePage = safePage, totalPages = totalPages)
    }
}
