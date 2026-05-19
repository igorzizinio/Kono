package me.igorunderplayer.kono.commands.text.testing

import dev.kord.core.behavior.reply
import dev.kord.core.event.message.MessageCreateEvent
import me.igorunderplayer.kono.commands.BaseCommand
import me.igorunderplayer.kono.commands.CommandCategory
import me.igorunderplayer.kono.data.repositories.CardInstanceRepository
import me.igorunderplayer.kono.domain.card.EquipmentSlot

class EquipListCommand(
    private val cardInstanceRepository: CardInstanceRepository
) : BaseCommand(
    name = "equiplist",
    description = "Lista os itens equipados no personagem ativo",
    category = CommandCategory.Game
) {

    override suspend fun run(event: MessageCreateEvent, args: Array<String>) {
        val discordId = event.message.author?.id?.value?.toLong() ?: return
        val items = cardInstanceRepository.getEquippedItemsForActiveCharacter(discordId)

        val messageContent = buildString {
            appendLine("🧩 **Equipamentos do personagem ativo**\n")

            for (slot in EquipmentSlot.entries) {
                val item = items.firstOrNull { it.slot == slot.index }
                if (item == null) {
                    appendLine("${slot.icon} **${slot.displayName}**: *vazio*")
                } else {
                    appendLine("${slot.icon} **${slot.displayName}**: #${item.cardInstanceId} **${item.name}** (${item.rarity})")
                }
            }

            appendLine("\nUse `!unequip <slot>` para remover um item. Ex: `!unequip arma`")
        }

        event.message.reply { content = messageContent }
    }
}
