package me.igorunderplayer.kono.commands.text.testing

import dev.kord.core.behavior.reply
import dev.kord.core.event.message.MessageCreateEvent
import me.igorunderplayer.kono.commands.BaseCommand
import me.igorunderplayer.kono.commands.CommandCategory
import me.igorunderplayer.kono.domain.team.EquipItemHandler

class EquipItemCommand(
    private val equipItemHandler: EquipItemHandler
) : BaseCommand(
    name = "equip",
    description = "Equipe um item no personagem ativo",
    category = CommandCategory.Game
) {

    override suspend fun run(event: MessageCreateEvent, args: Array<String>) {
        val userId = event.message.author?.id?.value?.toLong() ?: return

        val itemInstanceId = args.getOrNull(0)?.toIntOrNull()
        if (itemInstanceId == null) {
            event.message.reply {
                content = "Por favor, informe o ID da instância do item. Ex: `!equip 12`"
            }
            return
        }

        when (val result = equipItemHandler.execute(userId, itemInstanceId)) {
            is EquipItemHandler.Result.Success -> {
                val replaced = if (result.replaced) " (substituiu o item anterior)" else ""
                event.message.reply {
                    content = "✅ Item #$itemInstanceId equipado no slot ${result.slot.icon} **${result.slot.displayName}**$replaced."
                }
            }

            is EquipItemHandler.Result.InvalidSlot -> {
                event.message.reply {
                    content = "❌ Esse item não possui um slot de equipamento válido."
                }
            }

            is EquipItemHandler.Result.NoActiveCharacter -> {
                event.message.reply {
                    content = "❌ Você precisa selecionar um personagem ativo antes de equipar itens."
                }
            }

            is EquipItemHandler.Result.InvalidItem -> {
                event.message.reply {
                    content = "❌ Esse item não existe, não pertence a você ou não é um equipamento."
                }
            }

            is EquipItemHandler.Result.ItemAlreadyEquipped -> {
                event.message.reply {
                    content = "❌ Esse item já está equipado em algum personagem."
                }
            }
        }
    }
}
