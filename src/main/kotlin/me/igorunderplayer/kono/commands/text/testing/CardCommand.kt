package me.igorunderplayer.kono.commands.text.testing

import dev.kord.core.behavior.reply
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.rest.builder.message.embed
import me.igorunderplayer.kono.commands.BaseCommand
import me.igorunderplayer.kono.domain.card.*
import me.igorunderplayer.kono.domain.card.ability.*
import me.igorunderplayer.kono.services.CardService
import me.igorunderplayer.kono.utils.prettyPercent

class CardCommand(
    private val cardService: CardService,
) : BaseCommand(
    name = "card",
    description = "exibe informações de uma carta"
) {

    override suspend fun run(event: MessageCreateEvent, args: Array<String>) {

        val query = args.joinToString(" ").trim()

        // 📜 LISTAGEM
        if (query.isBlank()) {
            val cards = cardService.getCardDefinitions()
                .filter { it.rarity != Rarity.KONO }

            val grouped = cards.groupBy { it.type }

            event.message.reply {
                embed {
                    title = "📚 Cartas disponíveis"

                    description = grouped.entries.joinToString("\n\n") { (type, list) ->
                        "**${type.toDisplayName()}**\n" +
                                list.joinToString("\n") {
                                    "${it.rarity.toDisplayEmoji()} **${it.name}**"
                                }
                    }
                }
            }
            return
        }

        val cards = cardService.getCardDefinitions()

        // 🔍 1. PRIORIDADE: ID exato
        val byId = cards.firstOrNull {
            it.id.equals(query, ignoreCase = true)
        }

        // 🔍 2. BUSCA por nome (ILIKE)
        val byName = cards.filter {
            it.name.contains(query, ignoreCase = true)
        }

        val card = byId ?: byName.firstOrNull()

        if (card == null) {
            event.message.reply {
                content = "❌ Nenhuma carta encontrada para: **$query**"
            }
            return
        }

        event.message.reply {
            embed {
                title = "${card.rarity.toDisplayEmoji()} ${card.name}"
                color = card.rarity.colorDefinition()
                description = card.description

                footer {
                    text = "${card.type.toDisplayName()} • ID: ${card.id}"
                }

                // 📊 Stats base
                if (card.baseStats.isNotEmpty()) {
                    field {
                        name = "📊 Status Base"
                        value = card.baseStats.entries.joinToString("\n") { (stat, value) ->
                            "${stat.prettyName()}: **${prettyValue(stat, value)}**"
                        }
                        inline = true
                    }
                }

                // 📈 Scaling
                if (card.statsPerLevel.isNotEmpty()) {
                    field {
                        name = "📈 Stats por nível"
                        value = card.statsPerLevel.entries.joinToString("\n") { (stat, value) ->
                            "${stat.prettyName()}: ${prettyValue(stat, value)}"
                        }
                        inline = true
                    }
                }

                // 🏷️ Tags
                if (card.tags.isNotEmpty()) {
                    field {
                        name = "🏷️ Tags"
                        value = card.tags.joinToString(", ")
                        inline = true
                    }
                }

                // ⚔️ Habilidades
                // ⚔️ Habilidades
                if (card.abilities.isNotEmpty()) {

                    val abilityBlocks = card.abilities.map { ability ->
                        formatAbility(ability)
                    }

                    val chunks = chunkFields(
                        items = abilityBlocks,
                        maxLength = 900
                    )

                    chunks.forEachIndexed { index, chunk ->
                        field {
                            name = if (chunks.size == 1) {
                                "⚔️ Habilidades"
                            } else {
                                "⚔️ Habilidades ${index + 1}/${chunks.size}"
                            }

                            value = chunk.joinToString("\n\n")
                        }
                    }
                }
            }
        }
    }

    private fun formatAbility(ability: Ability): String {

        val description = compactDescription(ability.description)

        val effects = ability.effects.joinToString(" ") { it.pretty() }

        return buildString {

            append("$${ability.type.prettyName()} • **${ability.name}**")

            if (ability.trigger.prettyName().isNotBlank()) {
                append(" `$ability.trigger.prettyName()`")
            }

            append("\n")
            append(description)

            if (effects.isNotBlank()) {
                append("\n")
                append(effects)
            }
        }
    }


    private fun compactDescription(description: String?): String {
        return description
            ?.replace(Regex("\\s+"), " ")
            ?.trim() ?: ""
    }

    private fun chunkFields(
        items: List<String>,
        maxLength: Int
    ): List<List<String>> {

        val chunks = mutableListOf<MutableList<String>>()
        var current = mutableListOf<String>()
        var currentLength = 0

        for (item in items) {

            val additionalLength =
                item.length + if (current.isEmpty()) 0 else 2

            if (
                current.isNotEmpty() &&
                currentLength + additionalLength > maxLength
            ) {
                chunks += current
                current = mutableListOf()
                currentLength = 0
            }

            current += item
            currentLength += additionalLength
        }

        if (current.isNotEmpty()) {
            chunks += current
        }

        return chunks
    }

}
