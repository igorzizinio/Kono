package me.igorunderplayer.kono.commands.slash.game

import dev.kord.common.entity.ApplicationCommandOption
import dev.kord.common.entity.ApplicationCommandOptionType
import dev.kord.common.entity.optional.Optional
import dev.kord.common.entity.optional.OptionalBoolean
import dev.kord.core.entity.interaction.SubCommand
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import dev.kord.core.behavior.interaction.response.respond
import dev.kord.rest.builder.message.embed
import me.igorunderplayer.kono.commands.KonoSlashCommand
import me.igorunderplayer.kono.commands.KonoSlashSubCommand
import me.igorunderplayer.kono.domain.card.CardCatalog
import me.igorunderplayer.kono.services.DeckService

class DeckSlashCommand(private val deckService: DeckService) : KonoSlashCommand {
    override val name = "deck"
    override val description = "Cria e gerencia seus decks de cartas"

    private val subCommands = listOf(
        DeckListSubCommand(deckService),
        DeckViewSubCommand(deckService),
        DeckCreateSubCommand(deckService),
        DeckAddSubCommand(deckService),
        DeckRemoveSubCommand(deckService),
        DeckUseSubCommand(deckService)
    )

    override val options = subCommands.map {
        ApplicationCommandOption(
            name = it.name,
            description = it.description,
            type = ApplicationCommandOptionType.SubCommand,
            options = Optional(it.options)
        )
    }

    override suspend fun run(event: ChatInputCommandInteractionCreateEvent) {
        val command = event.interaction.command as SubCommand
        subCommands.firstOrNull { it.name == command.name }?.run(event)
    }
}

private class DeckViewSubCommand(private val service: DeckService) : KonoSlashSubCommand {
    override val name = "ver"
    override val description = "Exibe as cartas de um deck"
    override val options = listOf(option("deck", "Nome do deck"))

    override suspend fun run(event: ChatInputCommandInteractionCreateEvent) {
        val name = event.interaction.command.strings["deck"] ?: return
        val deck = service.list(event.interaction.user.id.value.toLong())
            .firstOrNull { it.deck.name.equals(name, true) }
        val deferred = event.interaction.deferPublicResponse()
        deferred.respond {
            embed {
                title = "🃏 Deck — $name"
                description = deck?.cardIds?.mapIndexed { index, id ->
                    val card = CardCatalog.getById(id)
                    "${index + 1}. ${card?.name ?: id} (`$id`)"
                }?.joinToString("\n") ?: "❌ Deck não encontrado."
            }
        }
    }
}

private class DeckListSubCommand(private val service: DeckService) : KonoSlashSubCommand {
    override val name = "listar"
    override val description = "Lista seus decks"
    override val options = emptyList<ApplicationCommandOption>()

    override suspend fun run(event: ChatInputCommandInteractionCreateEvent) {
        val decks = service.list(event.interaction.user.id.value.toLong())
        val description = decks.ifEmpty { emptyList() }
            .joinToString("\n") { deck ->
                val marker = if (deck.deck.active) "✅" else "▫️"
                "$marker **${deck.deck.name}** — ${deck.cardIds.size}/20 cartas"
            }.ifBlank { "Você ainda não possui decks." }
        val deferred = event.interaction.deferPublicResponse()
        deferred.respond {
            embed {
                title = "🃏 Seus decks"
                this.description = description
            }
        }
    }
}

private class DeckCreateSubCommand(private val service: DeckService) : KonoSlashSubCommand {
    override val name = "criar"
    override val description = "Cria um deck vazio"
    override val options = listOf(option("nome", "Nome do deck"))

    override suspend fun run(event: ChatInputCommandInteractionCreateEvent) {
        val name = event.interaction.command.strings["nome"] ?: return
        val deferred = event.interaction.deferPublicResponse()
        deferred.respond { content = service.create(event.interaction.user.id.value.toLong(), name) }
    }
}

private class DeckAddSubCommand(private val service: DeckService) : KonoSlashSubCommand {
    override val name = "adicionar"
    override val description = "Adiciona uma carta pelo ID do catálogo"
    override val options = listOf(option("deck", "Nome do deck"), option("carta", "ID da carta"))

    override suspend fun run(event: ChatInputCommandInteractionCreateEvent) {
        val deck = event.interaction.command.strings["deck"] ?: return
        val card = event.interaction.command.strings["carta"] ?: return
        val deferred = event.interaction.deferPublicResponse()
        deferred.respond {
            content = service.add(event.interaction.user.id.value.toLong(), deck, card)
        }
    }
}

private class DeckRemoveSubCommand(private val service: DeckService) : KonoSlashSubCommand {
    override val name = "remover"
    override val description = "Remove uma carta pelo slot"
    override val options = listOf(
        option("deck", "Nome do deck"),
        ApplicationCommandOption(
            name = "slot",
            description = "Slot da carta (1 a 20)",
            type = ApplicationCommandOptionType.Integer,
            required = OptionalBoolean.Value(true)
        )
    )

    override suspend fun run(event: ChatInputCommandInteractionCreateEvent) {
        val deck = event.interaction.command.strings["deck"] ?: return
        val slot = (event.interaction.command.integers["slot"] ?: 0L).toInt()
        val deferred = event.interaction.deferPublicResponse()
        deferred.respond {
            content = service.remove(event.interaction.user.id.value.toLong(), deck, slot)
        }
    }
}

private class DeckUseSubCommand(private val service: DeckService) : KonoSlashSubCommand {
    override val name = "usar"
    override val description = "Seleciona o deck ativo"
    override val options = listOf(option("deck", "Nome do deck"))

    override suspend fun run(event: ChatInputCommandInteractionCreateEvent) {
        val deck = event.interaction.command.strings["deck"] ?: return
        val deferred = event.interaction.deferPublicResponse()
        deferred.respond {
            content = service.activate(event.interaction.user.id.value.toLong(), deck)
        }
    }
}

private fun option(name: String, description: String) = ApplicationCommandOption(
    name = name,
    description = description,
    type = ApplicationCommandOptionType.String,
    required = OptionalBoolean.Value(true)
)
