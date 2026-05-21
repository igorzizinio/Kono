package me.igorunderplayer.kono.commands.slash.game

import dev.kord.common.entity.ApplicationCommandOption
import dev.kord.common.entity.ApplicationCommandOptionType
import dev.kord.common.entity.Choice
import dev.kord.common.entity.optional.Optional
import dev.kord.common.entity.optional.OptionalBoolean
import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import dev.kord.rest.builder.message.embed
import me.igorunderplayer.kono.commands.KonoSlashCommand
import me.igorunderplayer.kono.data.entities.CardInstance
import me.igorunderplayer.kono.data.repositories.CardInstanceRepository
import me.igorunderplayer.kono.data.repositories.CardRepository
import me.igorunderplayer.kono.data.repositories.EquippedCardsRepository
import me.igorunderplayer.kono.domain.card.CardDefinition
import me.igorunderplayer.kono.domain.card.CardType
import me.igorunderplayer.kono.domain.card.toDisplayEmoji
import me.igorunderplayer.kono.domain.card.toDisplayName
import me.igorunderplayer.kono.services.UserService

class InventarioSlashCommand(
    private val cardInstanceRepository: CardInstanceRepository,
    private val cardRepository: CardRepository,
    private val equippedCardsRepository: EquippedCardsRepository,
    private val userService: UserService
) : KonoSlashCommand {
    override val name = "inventario"
    override val description = "Exibe seu inventário de cartas"
    override val options = listOf(
        ApplicationCommandOption(
            name = "tipo",
            description = "Tipo de carta a exibir",
            type = ApplicationCommandOptionType.String,
            required = OptionalBoolean.Value(true),
            choices = Optional(listOf(
                Choice.StringChoice(name = "👤 Personagens", nameLocalizations = Optional(), value = "personagens"),
                Choice.StringChoice(name = "🎒 Equipamentos", nameLocalizations = Optional(), value = "equipamentos"),
            ))
        ),
        ApplicationCommandOption(
            name = "pagina",
            description = "Número da página (padrão: 1)",
            type = ApplicationCommandOptionType.Integer,
            required = OptionalBoolean.Value(false),
        )
    )

    private val PAGE_SIZE = 5
    private val EMBED_LIMIT = 3500

    private data class InventoryEntry(val definition: CardDefinition, val count: Int, val instances: List<CardInstance>)

    override suspend fun run(event: ChatInputCommandInteractionCreateEvent) {
        val deferred = event.interaction.deferEphemeralResponse()
        val discordId = event.interaction.user.id.value.toLong()
        val tipoToken = event.interaction.command.strings["tipo"] ?: "personagens"
        val page = (event.interaction.command.integers["pagina"] ?: 1L).toInt().coerceAtLeast(1)

        val cardType = if (tipoToken == "personagens") CardType.CHARACTER else CardType.EQUIPMENT
        val tipoLabel = if (cardType == CardType.CHARACTER) "👤 Personagens" else "🎒 Equipamentos"

        val user = userService.getUserByDiscordId(discordId)
        if (user == null) {
            deferred.respond { content = "❌ Você não está registrado. Use `/register` para começar." }
            return
        }

        val instances = cardInstanceRepository.getByDiscordId(discordId)
        if (instances.isEmpty()) {
            deferred.respond { content = "📦 Seu inventário está vazio." }
            return
        }

        val userEquipped = equippedCardsRepository.getEquippedCardsForUser(user.id)
        val equippedIds = userEquipped.map { it.cardInstanceId }.toSet()

        val entries = instances
            .groupBy { it.definitionId }
            .mapNotNull { (defId, list) ->
                val def = cardRepository.getDefinition(defId) ?: return@mapNotNull null
                InventoryEntry(def, list.size, list)
            }
            .filter { it.definition.type == cardType }
            .sortedWith(compareByDescending<InventoryEntry> { it.definition.rarity.ordinal }.thenBy { it.definition.name })

        if (entries.isEmpty()) {
            deferred.respond { content = "📦 Você não possui $tipoLabel ainda." }
            return
        }

        val totalPages = (entries.size + PAGE_SIZE - 1) / PAGE_SIZE
        val safePage = page.coerceAtMost(totalPages)
        val pageItems = entries.drop((safePage - 1) * PAGE_SIZE).take(PAGE_SIZE)

        val body = buildString {
            pageItems.forEach { entry ->
                appendLine("${entry.definition.rarity.toDisplayEmoji()} **${entry.definition.name}** (${entry.definition.rarity.toDisplayName()}) x${entry.count}")
                entry.instances
                    .groupBy { it.level }
                    .toSortedMap(compareByDescending { it })
                    .forEach { (level, levelInstances) ->
                        val equippedCount = levelInstances.count { it.id in equippedIds }
                        val suffix = if (equippedCount > 0) " *(${equippedCount} equipado)*" else ""
                        val ids = levelInstances.sortedBy { it.id }.map { it.id }
                        val idsText = ids.take(3).joinToString(", ") { "`$it`" }.let {
                            if (ids.size > 3) "$it ..." else it
                        }
                        appendLine("   • ${levelInstances.size}x Lv.$level$suffix — IDs: $idsText")
                    }
            }
        }.trimEnd().let { if (it.length > EMBED_LIMIT) it.take(EMBED_LIMIT - 30) + "\n…" else it }

        deferred.respond {
            embed {
                title = "📦 Inventário — $tipoLabel"
                description = body.ifBlank { "*(nenhum)*" }
                footer { text = "Página $safePage/$totalPages • Use a opção pagina para navegar" }
            }
        }
    }
}
