package me.igorunderplayer.kono.commands.slash.game.subcommand

import dev.kord.common.entity.ApplicationCommandOption
import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import dev.kord.rest.builder.message.embed
import me.igorunderplayer.kono.commands.KonoSlashSubCommand
import me.igorunderplayer.kono.data.repositories.BattleTeamRepository
import me.igorunderplayer.kono.data.repositories.CardInstanceRepository
import me.igorunderplayer.kono.data.repositories.UserRepository
import me.igorunderplayer.kono.domain.card.toDisplayEmoji

class TimeVer(
    private val userRepository: UserRepository,
    private val cardInstanceRepository: CardInstanceRepository,
    private val battleTeamRepository: BattleTeamRepository
) : KonoSlashSubCommand {
    override val name = "ver"
    override val description = "Exibe os 3 slots do seu time de batalha"
    override val options: List<ApplicationCommandOption> = listOf()

    override suspend fun run(event: ChatInputCommandInteractionCreateEvent) {
        val deferred = event.interaction.deferEphemeralResponse()
        val discordId = event.interaction.user.id.value.toLong()

        val user = userRepository.getUserByDiscordId(discordId)
        if (user == null) {
            deferred.respond { content = "❌ Você não está registrado. Use `/register` para começar." }
            return
        }

        val slots = battleTeamRepository.getTeamByUserId(user.id).sortedBy { it.slot }

        deferred.respond {
            embed {
                title = "👥 Seu Time"
                description = buildString {
                    for (slotNum in 1..3) {
                        val row = slots.firstOrNull { it.slot == slotNum }
                        if (row == null) {
                            appendLine("**Slot $slotNum:** — *vazio*")
                        } else {
                            val char =
                                cardInstanceRepository.getOwnedCharacterWithDefinition(user.id, row.characterInstanceId)
                            if (char == null) {
                                appendLine("**Slot $slotNum:** ⚠️ personagem ausente (id ${row.characterInstanceId})")
                            } else {
                                val (instance, def) = char
                                appendLine("**Slot $slotNum:** ${def.rarity.toDisplayEmoji()} **${def.name}** — Lv.${instance.level} `#${instance.id}`")
                            }
                        }
                    }
                    appendLine()
                    append("Use `/time definir` para alterar os slots e `/batalha` para lutar.")
                }
            }
        }
    }
}
