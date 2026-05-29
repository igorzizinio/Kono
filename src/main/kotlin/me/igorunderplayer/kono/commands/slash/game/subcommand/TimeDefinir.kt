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
import me.igorunderplayer.kono.data.repositories.CardInstanceRepository
import me.igorunderplayer.kono.data.repositories.UserRepository
import me.igorunderplayer.kono.domain.card.CardType

class TimeDefinir(
    private val userRepository: UserRepository,
    private val cardInstanceRepository: CardInstanceRepository,
    private val battleTeamRepository: BattleTeamRepository
) : KonoSlashSubCommand {
    override val name = "definir"
    override val description = "Coloca um personagem em um slot do seu time"
    override val options = listOf(
        ApplicationCommandOption(
            name = "slot",
            description = "Posição no time (1, 2 ou 3)",
            type = ApplicationCommandOptionType.Integer,
            required = OptionalBoolean.Value(true),
            choices = Optional(
                listOf(
                    Choice.IntegerChoice(name = "Slot 1", nameLocalizations = Optional(), value = 1L),
                    Choice.IntegerChoice(name = "Slot 2", nameLocalizations = Optional(), value = 2L),
                    Choice.IntegerChoice(name = "Slot 3", nameLocalizations = Optional(), value = 3L),
                )
            )
        ),
        ApplicationCommandOption(
            name = "instancia",
            description = "ID de instância do personagem (veja em /inventario)",
            type = ApplicationCommandOptionType.Integer,
            required = OptionalBoolean.Value(true),
        )
    )

    override suspend fun run(event: ChatInputCommandInteractionCreateEvent) {
        val deferred = event.interaction.deferEphemeralResponse()
        val discordId = event.interaction.user.id.value.toLong()
        val slot = event.interaction.command.integers["slot"]?.toInt()
        val instanceId = event.interaction.command.integers["instancia"]?.toInt()

        if (slot == null || slot !in 1..3 || instanceId == null || instanceId <= 0) {
            deferred.respond { content = "❌ Parâmetros inválidos." }
            return
        }

        val user = userRepository.getUserByDiscordId(discordId)
        if (user == null) {
            deferred.respond { content = "❌ Você não está registrado. Use `/register` para começar." }
            return
        }

        val character = cardInstanceRepository.getOwnedCharacterWithDefinition(user.id, instanceId)
        if (character == null) {
            deferred.respond {
                content =
                    "❌ Personagem #$instanceId não encontrado ou não pertence a você.\nUse `/inventario tipo:Personagens` para ver seus IDs."
            }
            return
        }

        if (character.second.type != CardType.CHARACTER) {
            deferred.respond { content = "❌ Apenas personagens podem entrar no time." }
            return
        }

        battleTeamRepository.setSlot(user.id, slot, instanceId)
        deferred.respond {
            content =
                "✅ **${character.second.name}** (Lv.${character.first.level}) colocado no **Slot $slot** do seu time."
        }
    }
}
