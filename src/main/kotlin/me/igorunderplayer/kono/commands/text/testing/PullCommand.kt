package me.igorunderplayer.kono.commands.text.testing

import dev.kord.core.behavior.reply
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.rest.builder.message.embed
import me.igorunderplayer.kono.commands.BaseCommand
import me.igorunderplayer.kono.commands.CommandCategory
import me.igorunderplayer.kono.domain.card.CardType
import me.igorunderplayer.kono.domain.card.Rarity
import me.igorunderplayer.kono.services.GachaResult
import me.igorunderplayer.kono.services.GachaService

class PullCommand(
    private val gachaService: GachaService
) : BaseCommand(
    name = "pull",
    description = "Pull a random card using essence!",
    category = CommandCategory.Game
) {
    override suspend fun run(event: MessageCreateEvent, args: Array<String>) {
        val userId = event.message.author?.id?.value?.toLong() ?: return

        println(args.joinToString(" "))
        val multiple = args.getOrNull(0) == "10"

        when (val result = gachaService.pull(userId, multiple)) {
            is GachaResult.Success -> {
                event.message.reply {
                    embed {
                        title = "🎰 Você puxou:"
                        description = "${resolveRarityEmoji(result.rarity)} **${result.cardName}** (${resolveCardType(result.type)})"
                        footer {
                            text = "💎 Essence restante: ${result.remainingEssence}"
                        }
                    }
                }
            }

            is GachaResult.MultipePullSuccess -> {
                val pulledCards = result.pulledCards.joinToString("\n") {
                    "${resolveRarityEmoji(it.rarity)} **${it.cardName}** (${resolveCardType(it.type)})"
                }
                event.message.reply {
                    embed {
                        title = "🎰 Você puxou:"
                        description = pulledCards
                        footer {
                            text = "💎 Essence restante: ${result.remainingEssence}"
                        }
                    }
                }
            }

            GachaResult.NotEnoughEssence -> {
                event.message.reply { content = "Você não tem essence suficiente 💎" }
            }

            GachaResult.UserNotFound -> {
                event.message.reply { content = "Você precisa se registrar primeiro." }
            }

            GachaResult.NoCardsAvailable -> {
                event.message.reply { content = "Nenhuma carta disponível nessa raridade." }
            }

            else -> {
                event.message.reply { content = "Erro ao fazer pull." }
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

    private fun resolveCardType(cardType: CardType): String = when (cardType) {
        CardType.CHARACTER -> "👤"
        CardType.EQUIPMENT -> "🎒"
    }
}

