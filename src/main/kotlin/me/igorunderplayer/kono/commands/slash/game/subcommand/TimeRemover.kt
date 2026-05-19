package me.igorunderplayer.kono.commands.slash.game.subcommand

import dev.kord.common.entity.ApplicationCommandOption
import dev.kord.common.entity.ApplicationCommandOptionType
import dev.kord.common.entity.Choice
import dev.kord.common.entity.optional.Optional
import dev.kord.common.entity.optional.OptionalBoolean
import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import me.igorunderplayer.kono.commands.KonoSlashSubCommand
import me.igorunderplayer.kono.data.repositories.BattleTeamRepository
import me.igorunderplayer.kono.data.repositories.UserRepository

class TimeRemover(
    private val userRepository: UserRepository,
    private val battleTeamRepository: BattleTeamRepository
) : KonoSlashSubCommand {
    override val name = "remover"
    override val description = "Remove um personagem de um slot do time"
    override val options = listOf(
        ApplicationCommandOption(
            name = "slot",
            description = "Posição no time a limpar (1, 2 ou 3)",
            type = ApplicationCommandOptionType.Integer,
            required = OptionalBoolean.Value(true),
            choices = Optional(listOf(
                Choice.IntegerChoice(name = "Slot 1", nameLocalizations = Optional(), value = 1L),
                Choice.IntegerChoice(name = "Slot 2", nameLocalizations = Optional(), value = 2L),
                Choice.IntegerChoice(name = "Slot 3", nameLocalizations = Optional(), value = 3L),
            ))
        )
    )

    override suspend fun run(event: ChatInputCommandInteractionCreateEvent) {
        val deferred = event.interaction.deferEphemeralResponse()
        val discordId = event.interaction.user.id.value.toLong()
        val slot = event.interaction.command.integers["slot"]?.toInt()

        if (slot == null || slot !in 1..3) {
            deferred.respond { content = "❌ Slot inválido." }
            return
        }

        val user = userRepository.getUserByDiscordId(discordId)
        if (user == null) {
            deferred.respond { content = "❌ Você não está registrado. Use `/register` para começar." }
            return
        }

        val deleted = battleTeamRepository.clearSlot(user.id, slot)
        deferred.respond {
            content = if (deleted) "✅ Slot $slot removido do seu time."
            else "⚠️ O slot $slot já estava vazio."
        }
    }
}
