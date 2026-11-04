package me.igorunderplayer.kono.commands.text.testing

import dev.kord.core.behavior.reply
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.rest.builder.message.embed
import me.igorunderplayer.kono.commands.BaseCommand
import me.igorunderplayer.kono.commands.CommandCategory
import me.igorunderplayer.kono.domain.card.CardCatalog
import me.igorunderplayer.kono.services.DeckService

class DeckCommand(
    private val deckService: DeckService
) : BaseCommand(
    name = "deck",
    description = "Gerencia decks do duelo 1v1",
    aliases = listOf("decks"),
    category = CommandCategory.Game
) {
    override suspend fun run(event: MessageCreateEvent, args: Array<String>) {
        val discordId = event.message.author?.id?.value?.toLong() ?: return
        val action = args.firstOrNull()?.lowercase() ?: "listar"

        when (action) {
            "listar", "list" -> list(event, discordId)
            "criar", "create" -> create(event, discordId, args.drop(1).joinToString(" "))
            "ver", "view" -> view(event, discordId, args.getOrNull(1))
            "adicionar", "add" -> add(event, discordId, args.drop(1))
            "remover", "remove" -> remove(event, discordId, args.drop(1))
            "usar", "use", "ativar" -> use(event, discordId, args.drop(1).joinToString(" "))
            "validar", "validate" -> validate(event, discordId, args.getOrNull(1))
            else -> usage(event)
        }
    }

    private suspend fun list(event: MessageCreateEvent, discordId: Long) {
        val decks = deckService.list(discordId)
        event.message.reply {
            embed {
                title = "🃏 Seus decks"
                description = decks.joinToString("\n") {
                    "${if (it.deck.active) "✅" else "▫️"} **${it.deck.name}** — ${it.cardIds.size}/20 cartas"
                }.ifBlank { "Você ainda não possui decks." }
            }
        }
    }

    private suspend fun create(event: MessageCreateEvent, discordId: Long, name: String) {
        event.message.reply { content = if (name.isBlank()) usageText() else deckService.create(discordId, name) }
    }

    private suspend fun view(event: MessageCreateEvent, discordId: Long, name: String?) {
        if (name.isNullOrBlank()) {
            event.message.reply { content = "Use: `deck ver <nome>`." }
            return
        }
        val deck = deckService.list(discordId).firstOrNull { it.deck.name.equals(name, true) }
        event.message.reply {
            embed {
                title = "🃏 Deck — $name"
                description = deck?.cardIds?.mapIndexed { index, id ->
                    val card = CardCatalog.getById(id)
                    "${index + 1}. ${card?.name ?: id} (`$id`)"
                }?.joinToString("\n") ?: "❌ Deck não encontrado."
            }
        }
    }

    private suspend fun add(event: MessageCreateEvent, discordId: Long, args: List<String>) {
        if (args.size < 2) {
            event.message.reply { content = "Use: `deck adicionar <nome> <card-id>`." }
            return
        }
        event.message.reply { content = deckService.add(discordId, args[0], args[1]) }
    }

    private suspend fun remove(event: MessageCreateEvent, discordId: Long, args: List<String>) {
        if (args.size < 2) {
            event.message.reply { content = "Use: `deck remover <nome> <slot>`." }
            return
        }
        event.message.reply { content = deckService.remove(discordId, args[0], args[1].toIntOrNull() ?: 0) }
    }

    private suspend fun use(event: MessageCreateEvent, discordId: Long, name: String) {
        event.message.reply { content = if (name.isBlank()) usageText() else deckService.activate(discordId, name) }
    }

    private suspend fun validate(event: MessageCreateEvent, discordId: Long, name: String?) {
        val deck = if (name.isNullOrBlank()) deckService.getActive(discordId)
        else deckService.list(discordId).firstOrNull { it.deck.name.equals(name, true) }
        if (deck == null) {
            event.message.reply { content = "❌ Deck não encontrado." }
            return
        }
        val errors = deckService.validate(deck.cardIds)
        event.message.reply {
            content = if (errors.isEmpty()) "✅ **${deck.deck.name}** está pronto para o duelo."
            else "❌ **${deck.deck.name}**:\n${errors.joinToString("\n") { "• $it" }}"
        }
    }

    private suspend fun usage(event: MessageCreateEvent) {
        event.message.reply { content = usageText() }
    }

    private fun usageText() = """
        Use:
        `deck listar`
        `deck criar <nome>`
        `deck ver <nome>`
        `deck adicionar <nome> <card-id>`
        `deck remover <nome> <slot>`
        `deck validar [nome]`
        `deck usar <nome>`
    """.trimIndent()
}
