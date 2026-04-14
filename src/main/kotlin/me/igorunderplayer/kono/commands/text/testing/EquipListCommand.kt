package me.igorunderplayer.kono.commands.text.testing

import dev.kord.core.behavior.reply
import dev.kord.core.event.message.MessageCreateEvent
import me.igorunderplayer.kono.commands.BaseCommand
import me.igorunderplayer.kono.commands.CommandCategory
import me.igorunderplayer.kono.data.repositories.CardInstanceRepository

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

		val messageContent = if (items.isEmpty()) {
			"🧩 Seu personagem ativo não possui itens equipados."
		} else {
			buildString {
				appendLine("🧩 **Equipamentos do personagem ativo**\n")

				for (slot in 0..2) {
					val item = items.firstOrNull { it.slot == slot }
					if (item == null) {
						appendLine("Slot ${slot + 1}: vazio")
					} else {
						appendLine("Slot ${slot + 1}: #${item.cardInstanceId} **${item.name}** (${item.rarity})")
					}
				}

				appendLine("\nUse `!unequip <slot>` para remover um item.")
			}
		}

		event.message.reply { content = messageContent }
	}
}


