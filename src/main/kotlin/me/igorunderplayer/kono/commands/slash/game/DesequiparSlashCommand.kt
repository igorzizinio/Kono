package me.igorunderplayer.kono.commands.slash.game

import dev.kord.common.entity.ApplicationCommandOption
import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.behavior.interaction.response.edit
import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import dev.kord.rest.builder.component.ActionRowBuilder
import dev.kord.rest.builder.component.option
import dev.kord.rest.builder.message.embed
import me.igorunderplayer.kono.commands.KonoSlashCommand
import me.igorunderplayer.kono.data.repositories.CardInstanceRepository
import me.igorunderplayer.kono.domain.card.EquipmentSlot
import me.igorunderplayer.kono.domain.card.toDisplayEmoji
import me.igorunderplayer.kono.domain.card.toDisplayName
import me.igorunderplayer.kono.domain.team.UnequipItemHandler
import me.igorunderplayer.kono.utils.interaction.awaitStringSelectInteraction

class DesequiparSlashCommand(
    private val unequipItemHandler: UnequipItemHandler,
    private val cardInstanceRepository: CardInstanceRepository
) : KonoSlashCommand {

    override val name = "desequipar"
    override val description = "Remove um equipamento do personagem ativo via menu de seleção"
    override val options: List<ApplicationCommandOption> = listOf()

    override suspend fun run(event: ChatInputCommandInteractionCreateEvent) {
        val deferred = event.interaction.deferEphemeralResponse()
        val discordId = event.interaction.user.id.value.toLong()

        val equipped = cardInstanceRepository.getEquippedItemsForActiveCharacter(discordId)

        if (equipped.isEmpty()) {
            deferred.respond { content = "🧩 Seu personagem ativo não possui itens equipados." }
            return
        }

        val selectId = "desequipar-${discordId}-${System.currentTimeMillis()}"

        val response = deferred.respond {
            embed {
                title = "🧩 Remover equipamento"
                description = "Selecione o slot que deseja desequipar."
            }
            addComponent(ActionRowBuilder().apply {
                stringSelect(selectId) {
                    placeholder = "Selecione o slot..."
                    equipped.forEach { item ->
                        val slot = EquipmentSlot.fromIndex(item.slot) ?: return@forEach
                        option(
                            label = "${slot.icon} ${slot.displayName} — ${item.name}",
                            value = slot.name
                        ) {
                            description = item.rarity.toDisplayName()
                        }
                    }
                }
            })
        }

        val selectEvent = event.kord.awaitStringSelectInteraction(selectId, discordId) ?: run {
            response.edit { content = "⏰ Tempo esgotado."; components = mutableListOf() }
            return
        }

        val slotInput = selectEvent.interaction.values.firstOrNull() ?: run {
            selectEvent.interaction.respondEphemeral { content = "❌ Seleção inválida." }
            return
        }

        val update = selectEvent.interaction.deferEphemeralMessageUpdate()

        when (val result = unequipItemHandler.execute(discordId, slotInput)) {
            is UnequipItemHandler.Result.Success ->
                update.edit {
                    content = "✅ Item #${result.itemInstanceId} removido do slot ${result.slot.icon} **${result.slot.displayName}**."
                    components = mutableListOf()
                }
            is UnequipItemHandler.Result.InvalidSlot ->
                update.edit { content = "❌ Slot inválido: `${result.input}`."; components = mutableListOf() }
            is UnequipItemHandler.Result.NoActiveCharacter ->
                update.edit { content = "❌ Selecione um personagem ativo antes de remover itens."; components = mutableListOf() }
            is UnequipItemHandler.Result.EmptySlot ->
                update.edit { content = "❌ Esse slot não possui item equipado."; components = mutableListOf() }
        }
    }
}
