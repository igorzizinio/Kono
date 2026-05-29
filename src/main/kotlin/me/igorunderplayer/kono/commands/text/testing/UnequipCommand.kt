package me.igorunderplayer.kono.commands.text.testing

import dev.kord.core.behavior.reply
import dev.kord.core.event.message.MessageCreateEvent
import me.igorunderplayer.kono.commands.BaseCommand
import me.igorunderplayer.kono.commands.CommandCategory
import me.igorunderplayer.kono.domain.card.EquipmentSlot
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
        val slotInput = args.getOrNull(0)

        if (slotInput == null) {
            val slotNames = EquipmentSlot.entries.joinToString(", ") { "${it.index + 1} (${it.displayName})" }
            event.message.reply {
                content =
                    "Por favor, informe o slot do item. Ex: `!unequip arma` ou `!unequip 1`\nSlots disponíveis: $slotNames"
            }
            return
        }

        when (val result = unequipItemHandler.execute(discordId, slotInput)) {
            is UnequipItemHandler.Result.Success -> {
                event.message.reply {
                    content =
                        "✅ Item #${result.itemInstanceId} removido do slot ${result.slot.icon} **${result.slot.displayName}**."
                }
            }

            is UnequipItemHandler.Result.InvalidSlot -> {
                val slotNames = EquipmentSlot.entries.joinToString(", ") { "${it.index + 1} (${it.displayName})" }
                event.message.reply {
                    content = "❌ Slot `${result.input}` inválido. Slots disponíveis: $slotNames"
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
