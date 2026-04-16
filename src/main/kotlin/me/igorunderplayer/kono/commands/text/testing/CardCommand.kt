package me.igorunderplayer.kono.commands.text.testing

import dev.kord.core.behavior.reply
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.rest.builder.message.embed
import me.igorunderplayer.kono.commands.BaseCommand
import me.igorunderplayer.kono.domain.card.Rarity
import me.igorunderplayer.kono.domain.card.colorDefinition
import me.igorunderplayer.kono.domain.card.toDisplayEmoji
import me.igorunderplayer.kono.domain.card.toDisplayName
import me.igorunderplayer.kono.services.CardService

class CardCommand(
    private val cardService: CardService,
): BaseCommand(
    name = "card",
    description = "exibe informações de uma carta"
) {
    override suspend fun run(event: MessageCreateEvent, args: Array<String>) {
        val cardId = args.getOrNull(0)
        if (cardId == null) {
            val cards = cardService.getCardDefinitions().filter { it.rarity != Rarity.MYTHIC }
            event.message.reply {
                embed {
                    description = cards.joinToString("\n") {
                        "${it.rarity.toDisplayEmoji()} ${it.name}"
                    }
                }
            }
            return
        }

        val card = cardService.getCardDefinitionByName(args.joinToString(" "))
        if (card == null) {
            event.message.reply { content = "❌ Nenhuma carta com nome $cardId encontrada." }
            return
        }

        event.message.reply {
            embed {
                title = "${card.rarity.toDisplayEmoji()} ${card.name}"
                color = card.rarity.colorDefinition()
                // TODO: image = ""
                description = card.description
                footer {
                    text = card.type.toDisplayName()
                }
            }
        }
    }
}
