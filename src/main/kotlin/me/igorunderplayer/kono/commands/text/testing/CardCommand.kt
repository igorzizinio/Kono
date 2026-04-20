package me.igorunderplayer.kono.commands.text.testing

import dev.kord.core.behavior.reply
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.rest.builder.message.embed
import me.igorunderplayer.kono.commands.BaseCommand
import me.igorunderplayer.kono.domain.card.*
import me.igorunderplayer.kono.domain.card.ability.*
import me.igorunderplayer.kono.services.CardService

class CardCommand(
    private val cardService: CardService,
) : BaseCommand(
    name = "card",
    description = "exibe informações de uma carta"
) {

    override suspend fun run(event: MessageCreateEvent, args: Array<String>) {

        val query = args.joinToString(" ").trim()

        // 📜 LISTAGEM (sem argumento)
        if (query.isBlank()) {
            val cards = cardService.getCardDefinitions()
                .filter { it.rarity != Rarity.MYTHIC }

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
                            "${stat.prettyName()}: +${prettyValue(stat, value)}"
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
                if (card.abilities.isNotEmpty()) {
                    field {
                        name = "⚔️ Habilidades"
                        value = card.abilities.joinToString("\n\n") { ability ->

                            val effectsText = ability.effects.joinToString("\n") { effect ->
                                formatEffect(effect)
                            }

                            """
                            ${ability.type.prettyName()} **${ability.name}**
                            *(Trigger: ${ability.trigger})*
                            ${ability.description}
                            $effectsText
                            """.trimIndent()
                        }
                    }
                }
            }
        }
    }

    private fun formatEffect(effect: Effect): String {
        return when (effect) {
            is Effect.Damage -> "• 💥 Causa ${effect.value} de dano (${damageTypeLabel(effect.damageType)})"
            is Effect.DamageBasedOnStat ->
                "• 💥 Causa ${effect.scaling}x ${effect.stat.prettyName()} (${damageTypeLabel(effect.damageType)})"
            is Effect.DamageIncreasePercent ->
                "• 💥 +${effect.value * 100}% de dano"
            is Effect.Heal ->
                "• 💚 Cura ${effect.value}"
            is Effect.BuffStat ->
                "• 📈 +${effect.value} ${effect.stat.prettyName()}"
            is Effect.StatIncreasePercent ->
                "• 📈 +${effect.percent * 100}% ${effect.stat.prettyName()}"
            is Effect.AddCoins ->
                "• 💰 Gera ${effect.value} moedas"
            is Effect.AddCoinsScaling -> {
                val factionBonusText = if (effect.allyFactionForBaseBonus.isNullOrBlank() || effect.baseBonus <= 0) {
                    ""
                } else {
                    " • +${effect.baseBonus} base com ${effect.requiredAlliesForBaseBonus}+ aliado(s) da faccao ${effect.allyFactionForBaseBonus}"
                }

                "• 💰 Gera ${effect.base} moeda(s) base +${effect.bonusPerStack} a cada ${effect.coinsPerStack} moedas do time$factionBonusText"
            }
            is Effect.BuffStatByTeamCoins -> {
                val modeText = when (effect.mode) {
                    Effect.ScalingMode.STACK -> "acumulativo"
                    Effect.ScalingMode.HIGHEST_ONLY -> "apenas 1 stack"
                }
                val capText = effect.maxStacks?.let { " (max $it stacks)" } ?: ""

                "• 🎰 +${effect.valuePerStack} ${effect.stat.prettyName()} a cada ${effect.coinsPerStack} moedas do time [$modeText]$capText"
            }
            is Effect.ProtectAlliesDamageShare ->
                "• 🛡️ Intercepta ${(effect.sharePercent * 100).toInt()}% do dano recebido pelos aliados"
            Effect.Taunt ->
                "• 🎯 Provoca inimigos e vira alvo prioritario"
            is Effect.Random ->
                "• 🎲 Efeito aleatório (${effect.profile})"
            is Effect.StatIncreaseWhileBelowHealth ->
                "• ⚠️ +${effect.value} ${effect.stat.prettyName()} abaixo de ${(effect.threshold * 100).toInt()}% HP"
            else -> "• ❓ Efeito desconhecido"
        }
    }

    private fun damageTypeLabel(damageType: Effect.DamageType): String {
        return when (damageType) {
            Effect.DamageType.PHYSICAL -> "físico"
            Effect.DamageType.MAGIC -> "mágico"
            Effect.DamageType.TRUE -> "verdadeiro"
        }
    }
}
