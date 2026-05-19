package me.igorunderplayer.kono.commands.slash.game.subcommand

import dev.kord.common.entity.ApplicationCommandOption
import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import dev.kord.rest.builder.message.embed
import me.igorunderplayer.kono.commands.KonoSlashSubCommand
import me.igorunderplayer.kono.domain.card.prettyName
import me.igorunderplayer.kono.domain.card.prettyValue
import me.igorunderplayer.kono.domain.card.toDisplayEmoji
import me.igorunderplayer.kono.domain.team.BuildUnitHandler
import me.igorunderplayer.kono.domain.team.UpgradeCharacterHandler

class PersonagemInfo(
    private val buildUnitHandler: BuildUnitHandler,
    private val upgradeCharacterHandler: UpgradeCharacterHandler
) : KonoSlashSubCommand {
    override val name = "info"
    override val description = "Exibe as informações e status do seu personagem ativo"
    override val options: List<ApplicationCommandOption> = listOf()

    override suspend fun run(event: ChatInputCommandInteractionCreateEvent) {
        val deferred = event.interaction.deferEphemeralResponse()
        val discordId = event.interaction.user.id.value.toLong()

        when (val result = buildUnitHandler.executeByDiscordId(discordId)) {
            is BuildUnitHandler.Result.Success -> {
                val unit = result.unit
                val upgradeHint = buildUpgradeHint(discordId)

                deferred.respond {
                    embed {
                        title = "${unit.card.rarity.toDisplayEmoji()} ${unit.card.name}"
                        description = buildString {
                            appendLine(unit.card.description)
                            appendLine()
                            if (upgradeHint != null) {
                                appendLine(upgradeHint)
                                appendLine()
                            }
                            appendLine("**Equipamentos:**")
                            if (unit.equipments.isEmpty()) {
                                appendLine("— Nenhum equipamento")
                            } else {
                                unit.equipments.forEach {
                                    appendLine("${it.rarity.toDisplayEmoji()} ${it.name}")
                                }
                            }
                            appendLine()
                            appendLine("**Status:**")
                            unit.stats.forEach { (stat, value) ->
                                appendLine("• **${stat.prettyName()}**: ${prettyValue(stat, value)}")
                            }
                        }
                    }
                }
            }
            is BuildUnitHandler.Result.UserNotFound ->
                deferred.respond { content = "❌ Você não está registrado. Use `/register` para começar." }
            is BuildUnitHandler.Result.NoActiveCard ->
                deferred.respond { content = "❌ Nenhum personagem ativo. Use `/personagem definir` para selecionar um." }
            is BuildUnitHandler.Result.CharacterNotFound ->
                deferred.respond { content = "❌ Personagem ativo (#${result.activeCharacterId}) não encontrado." }
        }
    }

    private suspend fun buildUpgradeHint(discordId: Long): String? {
        return when (val preview = upgradeCharacterHandler.previewActiveCharacter(discordId)) {
            is UpgradeCharacterHandler.PreviewResult.Ready ->
                "🆙 **Upgrade disponível!** Use `/personagem upgrade` (custo: ${preview.cost.konosCost} ₭, ${preview.cost.copiesRequired} cópias)."
            else -> null
        }
    }
}
