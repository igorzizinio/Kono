package me.igorunderplayer.kono.utils.combat

import dev.kord.common.entity.ButtonStyle
import dev.kord.rest.builder.component.ActionRowBuilder
import me.igorunderplayer.kono.domain.card.ability.Ability

fun createActionRow(
    attackButtonId: String,
    availableAbilities: List<Ability>,
    abilityButtonIds: List<String>
) = ActionRowBuilder().apply {
    interactionButton(ButtonStyle.Primary, attackButtonId) {
        label = "⚔️ Atacar"
    }

    availableAbilities.take(4).forEachIndexed { index, ability ->
        interactionButton(ButtonStyle.Secondary, abilityButtonIds[index]) {
            label = ability.name.take(80)
        }
    }
}

fun createLogButton(customButtonId: String, disabled: Boolean = false) = ActionRowBuilder().apply {
    interactionButton(ButtonStyle.Primary, customButtonId) {
        label = "Ver diário"
        this.disabled = disabled
    }
}

fun resolveCombatantDisplayNames(
    playerName: String,
    enemyName: String,
    playerOwnerName: String,
    enemyOwnerName: String
): Pair<String, String> {
    if (!playerName.equals(enemyName, ignoreCase = true)) {
        return playerName to enemyName
    }

    return "$playerName de ${playerOwnerName.trim()}" to "$enemyName de ${enemyOwnerName.trim()}"
}

fun buildCombatSummaryEmbed(
    playerDisplayName: String,
    enemyDisplayName: String,
    playerStartHp: Double,
    enemyStartHp: Double,
    playerFinalHp: Double,
    enemyFinalHp: Double,
    playerAlive: Boolean,
    playerLabel: String = "Jogador",
    enemyLabel: String = "Inimigo",
): CombatEmbedPage {
    val summaryDescription = buildString {
        appendLine("⚔️ **Combate iniciado!**")
        appendLine("👤 **$playerLabel:** $playerDisplayName (${playerStartHp.toInt()} HP)")
        appendLine("👹 **$enemyLabel:** $enemyDisplayName (${enemyStartHp.toInt()} HP)")
        appendLine()
        appendLine(if (playerAlive) "🏆 **Resultado:** Você venceu!" else "💀 **Resultado:** Você perdeu...")
        appendLine("❤️ **HP final $playerLabel:** ${playerFinalHp.coerceAtLeast(0.0).toInt()}")
        appendLine("💔 **HP final $enemyLabel:** ${enemyFinalHp.coerceAtLeast(0.0).toInt()}")
    }

    return CombatEmbedPage(
        title = "⚔️ Resultado do Combate",
        description = summaryDescription.trim(),
        footer = "Clique no botão abaixo para ver o diário em modo privado"
    )
}

fun buildCombatLogEmbeds(eventLog: List<String>, embedDescriptionLimit: Int = 3500): List<CombatEmbedPage> {
    val eventPages = paginateEventLog(eventLog, embedDescriptionLimit)
    val totalPages = eventPages.size

    return eventPages.mapIndexed { index, page ->
        CombatEmbedPage(
            title = "📜 Diario de Batalha",
            description = page,
            footer = "Pagina ${index + 1}/$totalPages"
        )
    }
}

fun paginateEventLog(eventLog: List<String>, embedDescriptionLimit: Int = 3500): List<String> {
    val lines = eventLog.ifEmpty {
        listOf("ℹ️ Nenhum evento foi registrado durante a luta.")
    }

    val pages = mutableListOf<String>()
    val current = StringBuilder()

    for (rawLine in lines) {
        val line = if (rawLine.length > embedDescriptionLimit - 8) {
            "${rawLine.take(embedDescriptionLimit - 11)}..."
        } else {
            rawLine
        }

        val formattedLine = "• $line"

        if (current.isNotEmpty() && current.length + formattedLine.length + 1 > embedDescriptionLimit) {
            pages += current.toString()
            current.clear()
        }

        if (current.isNotEmpty()) current.append('\n')
        current.append(formattedLine)
    }

    if (current.isNotEmpty()) {
        pages += current.toString()
    }

    return pages
}

fun createContinueRow(
    customId: String,
    label: String = "Próxima rodada",
    disabled: Boolean = false
) = ActionRowBuilder().apply {
    interactionButton(ButtonStyle.Primary, customId) {
        this.label = label
        this.disabled = disabled
    }
}


data class CombatEmbedPage(
    val title: String,
    val description: String,
    val footer: String?
)
