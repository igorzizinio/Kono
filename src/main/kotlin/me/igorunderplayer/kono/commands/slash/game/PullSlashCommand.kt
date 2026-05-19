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
import me.igorunderplayer.kono.domain.card.CardType
import me.igorunderplayer.kono.domain.card.toDisplayEmoji
import me.igorunderplayer.kono.services.GachaResult
import me.igorunderplayer.kono.services.GachaService

class PullSlashCommand(
    private val gachaService: GachaService
) : KonoSlashCommand {
    override val name = "pull"
    override val description = "Puxa cartas do gacha usando essence (40 💎 por carta)"
    override val options = listOf(
        ApplicationCommandOption(
            name = "quantidade",
            description = "Quantas cartas puxar (padrão: 1)",
            type = ApplicationCommandOptionType.Integer,
            required = OptionalBoolean.Value(false),
            choices = Optional(listOf(
                Choice.IntegerChoice(name = "1 carta  (40 💎)", nameLocalizations = Optional(), value = 1L),
                Choice.IntegerChoice(name = "10 cartas (400 💎)", nameLocalizations = Optional(), value = 10L),
            ))
        )
    )

    override suspend fun run(event: ChatInputCommandInteractionCreateEvent) {
        val deferred = event.interaction.deferPublicResponse()
        val discordId = event.interaction.user.id.value.toLong()
        val multiple = (event.interaction.command.integers["quantidade"] ?: 1L) == 10L

        when (val result = gachaService.pull(discordId, multiple)) {
            is GachaResult.Success -> deferred.respond {
                embed {
                    title = "🎰 Você puxou:"
                    description = "${result.rarity.toDisplayEmoji()} **${result.cardName}** (${cardTypeLabel(result.type)})"
                    footer { text = "💎 Essence restante: ${result.remainingEssence}" }
                }
            }
            is GachaResult.MultipePullSuccess -> deferred.respond {
                embed {
                    title = "🎰 Você puxou (10x):"
                    description = result.pulledCards.joinToString("\n") {
                        "${it.rarity.toDisplayEmoji()} **${it.cardName}** (${cardTypeLabel(it.type)})"
                    }
                    footer { text = "💎 Essence restante: ${result.remainingEssence}" }
                }
            }
            GachaResult.NotEnoughEssence -> deferred.respond {
                content = "💎 Essence insuficiente. Use `/daily` ou `/work` para ganhar mais."
            }
            GachaResult.UserNotFound -> deferred.respond {
                content = "❌ Você não está registrado. Use `/register` para começar."
            }
            GachaResult.NoCardsAvailable -> deferred.respond {
                content = "❌ Nenhuma carta disponível no momento."
            }
            else -> deferred.respond { content = "⚠️ Erro ao realizar o pull. Tente novamente." }
        }
    }

    private fun cardTypeLabel(type: CardType) = when (type) {
        CardType.CHARACTER -> "👤 Personagem"
        CardType.EQUIPMENT -> "🎒 Equipamento"
    }
}
