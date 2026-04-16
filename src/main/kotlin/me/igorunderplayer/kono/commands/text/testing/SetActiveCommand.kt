package me.igorunderplayer.kono.commands.text.testing

import dev.kord.core.behavior.reply
import dev.kord.core.event.message.MessageCreateEvent
import me.igorunderplayer.kono.commands.BaseCommand
import me.igorunderplayer.kono.domain.team.SetActiveCharacterHandler

class SetActiveCommand(
    private val setActiveCharacterHandler: SetActiveCharacterHandler
): BaseCommand(
    name = "setactive",
    description = "selecione seu personagem ativo por instance id"
) {

    override suspend fun run(event: MessageCreateEvent, args: Array<String>) {
        val userId = event.message.author?.id?.value ?: return
        if (args.isEmpty()) {
            event.message.reply {
                content = "Use: `setactive <instance_id>` (exemplo: `setactive 42`)."
            }
            return
        }

        val instanceId = args[0].toIntOrNull()
        if (instanceId == null || instanceId <= 0) {
            event.message.reply {
                content = "Instance ID inválido. Use um número inteiro positivo, exemplo: `setactive 42`."
            }
            return
        }

        when (val result = setActiveCharacterHandler.execute(userId.toLong(), instanceId)) {
            is SetActiveCharacterHandler.Result.Success -> {
                event.message.reply {
                    content = "Personagem '${result.characterName}' (#${result.instanceId}) definido como ativo com sucesso!"
                }
            }

            is SetActiveCharacterHandler.Result.CharacterNotFound -> {
                event.message.reply {
                    content = "Não encontrei um personagem com instance ID #${result.instanceId} na sua conta."
                }
            }

            is SetActiveCharacterHandler.Result.UserNotFound -> {
                event.message.reply {
                    content = "Usuário não encontrado. Por favor, registre-se usando o comando 'register'."
                }
            }
        }
    }
}
