package me.igorunderplayer.kono.commands.slash.game.subcommand

import dev.kord.common.entity.ApplicationCommandOption
import dev.kord.common.entity.ApplicationCommandOptionType
import dev.kord.common.entity.optional.OptionalBoolean
import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import me.igorunderplayer.kono.commands.KonoSlashSubCommand
import me.igorunderplayer.kono.domain.team.SetActiveCharacterHandler

class PersonagemDefinir(
    private val setActiveCharacterHandler: SetActiveCharacterHandler
) : KonoSlashSubCommand {
    override val name = "definir"
    override val description = "Define o personagem ativo pelo ID de instância"
    override val options = listOf(
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
        val instanceId = event.interaction.command.integers["instancia"]?.toInt()

        if (instanceId == null || instanceId <= 0) {
            deferred.respond { content = "❌ ID de instância inválido. Use `/inventario tipo:Personagens` para ver seus IDs." }
            return
        }

        when (val result = setActiveCharacterHandler.execute(discordId, instanceId)) {
            is SetActiveCharacterHandler.Result.Success ->
                deferred.respond {
                    content = "✅ **${result.characterName}** (#${result.instanceId}) definido como personagem ativo!"
                }
            is SetActiveCharacterHandler.Result.CharacterNotFound ->
                deferred.respond {
                    content = "❌ Nenhum personagem com instância #${result.instanceId} na sua conta.\nUse `/inventario tipo:Personagens` para ver seus IDs."
                }
            is SetActiveCharacterHandler.Result.UserNotFound ->
                deferred.respond { content = "❌ Você não está registrado. Use `/register` para começar." }
        }
    }
}
