package me.igorunderplayer.kono.commands.text.testing

import dev.kord.core.behavior.reply
import dev.kord.core.event.message.MessageCreateEvent
import me.igorunderplayer.kono.commands.BaseCommand
import me.igorunderplayer.kono.commands.CommandCategory
import me.igorunderplayer.kono.domain.team.UnequipItemHandler

@Suppress("unused")
class UnequipCommand(
    private val unequipItemHandler: UnequipItemHandler
) : BaseCommand(
    name = "unequip",
    description = "Remove um item do slot do personagem ativo",
    category = CommandCategory.Game
) {

    override suspend fun run(event: MessageCreateEvent, args: Array<String>) {
        val discordId = event.message.author?.id?.value?.toLong() ?: return
        val slotInput = args.getOrNull(0)?.toIntOrNull()

        if (slotInput == null) {
            event.message.reply {
                content = "Por favor, informe o slot do item. Ex: `!unequip 1`"
            }
            return
        }

        val slot = slotInput - 1

        when (val result = unequipItemHandler.execute(discordId, slot)) {
            is UnequipItemHandler.Result.Success -> {
                event.message.reply {
                    content = "✅ Item #${result.itemInstanceId} removido do slot ${result.slot + 1}."
                }
            }

            is UnequipItemHandler.Result.InvalidSlot -> {
                event.message.reply {
                    content = "❌ Slot inválido. Use 1, 2 ou 3."
                }
            }

            is UnequipItemHandler.Result.NoActiveCharacter -> {
                event.message.reply {
                    content = "❌ Você precisa selecionar um personagem ativo antes de remover itens."
                }
            }

            is UnequipItemHandler.Result.EmptySlot -> {
                event.message.reply {
                    content = "❌ Não há item equipado nesse slot."
                }
            }
        }
    }
}


