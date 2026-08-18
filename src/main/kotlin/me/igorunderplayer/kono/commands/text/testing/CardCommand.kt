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

        val type = when (ability.type) {
            AbilityType.PASSIVE -> "🟢 PASSIVA"
            AbilityType.ACTIVE -> "🔵 ATIVA"
            else -> "⚪ ${ability.type.prettyName()}"
        }

        val trigger = formatTrigger(ability.trigger)

        val description = compactDescription(ability.description)

        val effects = ability.effects.joinToString(" ") { formatEffectCompact(it) }

        return buildString {

            append("$type • **${ability.name}**")

            if (trigger.isNotBlank()) {
                append(" `$trigger`")
            }

            append("\n")
            append(description)

            if (effects.isNotBlank()) {
                append("\n")
                append(effects)
            }
        }
    }

    private fun formatTrigger(trigger: AbilityTrigger): String {
        return when (trigger) {
            AbilityTrigger.Manual -> "Manual"

            AbilityTrigger.OnTurnStart ->
                "Início do turno"

            AbilityTrigger.OnBattleStart ->
                "Início da batalha"

            AbilityTrigger.OnHit ->
                "Ao acertar"

            AbilityTrigger.OnCrit ->
                "Ao critar"

            is AbilityTrigger.OnTurnEvery ->
                "A cada ${trigger.turns} turnos"

            is AbilityTrigger.OnAttackEvery ->
                "A cada ${trigger.attacks} ataques"

            is AbilityTrigger.OnAttackAgainstTag ->
                "Contra ${trigger.tag}"

            else -> ""
        }
    }

    private fun formatEffectCompact(effect: Effect): String {
        return when (effect) {

            is Effect.Damage ->
                "💥 ${effect.value} dano ${damageTypeLabel(effect.damageType)}"

            is Effect.DamageBasedOnStat ->
                "💥 ${effect.scaling}x ${effect.stat.prettyName()}"

            is Effect.DamageIncreasePercent ->
                "💥 +${prettyPercent(effect.value)} dano"

            is Effect.Heal ->
                "💚 Cura ${prettyValue(Stat.HP, effect.value)}"

            is Effect.BuffStat ->
                "📈 +${effect.value} ${effect.stat.prettyName()}"

            is Effect.StatIncreasePercent ->
                "📈 +${prettyPercent(effect.percent)} ${effect.stat.prettyName()}"

            is Effect.AddCoins ->
                "💰 +${effect.value} fichas"

            is Effect.AddCoinsScaling ->
                "💰 +${effect.base} fichas + bônus por fichas do time"

            is Effect.BuffStatByTeamCoins -> {
                val mode = when (effect.mode) {
                    ScalingMode.STACK -> "stack"
                    ScalingMode.HIGHEST_ONLY -> "maior stack"
                }

                "🎰 +${effect.valuePerStack} ${effect.stat.prettyName()} / ${effect.coinsPerStack} fichas ($mode)"
            }

            is Effect.ProtectAlliesDamageShare ->
                "🛡️ Intercepta ${(effect.sharePercent * 100).toInt()}% do dano aliado"

            Effect.Taunt ->
                "🎯 Provoca inimigos"

            is Effect.Random ->
                "🎲 Efeito aleatório: ${effect.profile}"

            is Effect.StatIncreaseWhileBelowHealth ->
                "⚠️ +${effect.value} ${effect.stat.prettyName()} abaixo de ${(effect.threshold * 100).toInt()}% HP"

            else -> ""
        }
    }

    private fun prettyPercent(value: Double): String {
        return if (value % 1.0 == 0.0) {
            "${(value * 100).toInt()}%"
        } else {
            "${"%.1f".format(value * 100)}%"
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


    private fun damageTypeLabel(damageType: DamageType): String {
        return when (damageType) {
            DamageType.PHYSICAL -> "físico"
            DamageType.MAGIC -> "mágico"
            DamageType.TRUE -> "verdadeiro"
        }
    }
}
