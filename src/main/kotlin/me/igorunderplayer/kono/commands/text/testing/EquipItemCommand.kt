package me.igorunderplayer.kono.commands.text.testing

import dev.kord.core.behavior.reply
import dev.kord.core.event.message.MessageCreateEvent
import me.igorunderplayer.kono.commands.BaseCommand
import me.igorunderplayer.kono.commands.CommandCategory
import me.igorunderplayer.kono.data.repositories.CardInstanceRepository
import me.igorunderplayer.kono.domain.team.EquipItemHandler

class EquipItemCommand(
    private val equipItemHandler: EquipItemHandler,
    private val cardInstanceRepository: CardInstanceRepository
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
                content = "Por favor, informe o ID da instância do item. Ex: `!equip 12 1`"
            }
            return
        }

        val requestedSlot = args.getOrNull(1)?.toIntOrNull()?.minus(1)
        val slot = requestedSlot ?: run {
            val occupiedSlots = cardInstanceRepository.getEquippedSlotsForActiveCharacter(userId)
            (0..2).firstOrNull { it !in occupiedSlots } ?: run {
                event.message.reply {
                    content = "❌ Seu personagem ativo já está com 3 itens equipados. Escolha um slot de 1 a 3 para substituir um item."
                }
                return
            }
        }

        when (val result = equipItemHandler.execute(userId, itemInstanceId, slot)) {
            is EquipItemHandler.Result.Success -> {
                event.message.reply {
                    content = "✅ Item #$itemInstanceId equipado com sucesso no slot ${result.slot + 1}."
                }
            }

            is EquipItemHandler.Result.InvalidSlot -> {
                event.message.reply {
                    content = "❌ Slot inválido. Use 1, 2 ou 3."
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




