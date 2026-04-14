package me.igorunderplayer.kono.commands.text.testing

import dev.kord.core.behavior.reply
import dev.kord.core.event.message.MessageCreateEvent
import me.igorunderplayer.kono.commands.BaseCommand
import me.igorunderplayer.kono.domain.team.SetActiveCharacterHandler

class SetActiveCommand(
    private val setActiveCharacterHandler: SetActiveCharacterHandler
): BaseCommand(
    name = "setactive",
    description = "selecione seu personagem ativo para combate!"
) {

    override suspend fun run(event: MessageCreateEvent, args: Array<String>) {
        val userId = event.message.author?.id?.value ?: return
        if (args.isEmpty()) {
            event.message.reply {
                content = "Por favor, forneça o nome do personagem que deseja definir como ativo."
            }
            return
        }

        val characterName = args.joinToString(" ")
        when (val result = setActiveCharacterHandler.execute(userId.toLong(), characterName)) {
            is SetActiveCharacterHandler.Result.Success -> {
                event.message.reply {
                    content = "Personagem '${result.characterName}' definido como ativo com sucesso!"
                }
            }

            is SetActiveCharacterHandler.Result.CharacterNotFound -> {
                event.message.reply {
                    content = "Personagem '${result.characterName}' não encontrado na sua equipe."
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
