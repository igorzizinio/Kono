package me.igorunderplayer.kono.commands.slash.game

import dev.kord.common.Color
import dev.kord.common.entity.ApplicationCommandOption
import dev.kord.common.entity.ButtonStyle
import dev.kord.common.entity.DiscordPartialEmoji
import dev.kord.core.behavior.interaction.response.edit
import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.event.interaction.ButtonInteractionCreateEvent
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import dev.kord.rest.builder.component.ActionRowBuilder
import dev.kord.rest.builder.component.option
import dev.kord.rest.builder.message.embed
import me.igorunderplayer.kono.commands.KonoSlashCommand
import me.igorunderplayer.kono.data.entities.CardInstance
import me.igorunderplayer.kono.data.repositories.CardInstanceRepository
import me.igorunderplayer.kono.data.repositories.EquippedCardsRepository
import me.igorunderplayer.kono.data.repositories.EquippedItemView
import me.igorunderplayer.kono.data.repositories.UserRepository
import me.igorunderplayer.kono.domain.card.CardDefinition
import me.igorunderplayer.kono.domain.card.EquipmentSlot
import me.igorunderplayer.kono.domain.card.Rarity
import me.igorunderplayer.kono.domain.card.colorDefinition
import me.igorunderplayer.kono.domain.card.toDisplayEmoji
import me.igorunderplayer.kono.domain.card.toDisplayName
import me.igorunderplayer.kono.domain.team.EquipItemHandler
import me.igorunderplayer.kono.domain.team.UnequipItemHandler
import me.igorunderplayer.kono.utils.interaction.awaitFirstButtonInteraction
import me.igorunderplayer.kono.utils.interaction.awaitStringSelectInteraction

class EquipamentosSlashCommand(
    private val equipItemHandler: EquipItemHandler,
    private val unequipItemHandler: UnequipItemHandler,
    private val cardInstanceRepository: CardInstanceRepository,
    private val equippedCardsRepository: EquippedCardsRepository,
    private val userRepository: UserRepository
) : KonoSlashCommand {

    override val name = "equipamentos"
    override val description = "Mostra e gerencia os equipamentos do personagem ativo"
    override val options: List<ApplicationCommandOption> = listOf()

    override suspend fun run(event: ChatInputCommandInteractionCreateEvent) {
        val deferred = event.interaction.deferEphemeralResponse()
        val discordId = event.interaction.user.id.value.toLong()

        var equipped = cardInstanceRepository.getEquippedItemsForActiveCharacter(discordId)
        var statusMessage: String? = null
        var equipButtonId = newId("equip-btn", discordId)
        var removeButtonId = newId("remove-btn", discordId)

        val response = deferred.respond {
            embed {
                title = "🧩 Equipamentos"
                color = embedColor(equipped)
                description = slotDescription(equipped)
                footer { text = "Escolha uma ação abaixo" }
            }
            addComponent(overviewButtons(equipButtonId, removeButtonId, equipped.isNotEmpty()))
        }

        while (true) {
            val buttonIds = buildList {
                add(equipButtonId)
                if (equipped.isNotEmpty()) add(removeButtonId)
            }

            val clicked = event.kord.awaitFirstButtonInteraction(buttonIds, discordId) ?: run {
                response.edit { content = "⏰ Tempo esgotado."; components = mutableListOf() }
                return
            }

            val (clickedId, clickEvent) = clicked

            // null = timeout in sub-flow; non-null = success or error message to show in footer
            val result = if (clickedId == equipButtonId) {
                handleEquipFlow(event, discordId, clickEvent)
            } else {
                handleRemoveFlow(event, discordId, clickEvent, equipped)
            }

            if (result == null) {
                response.edit { content = "⏰ Tempo esgotado."; components = mutableListOf() }
                return
            }

            equipped = cardInstanceRepository.getEquippedItemsForActiveCharacter(discordId)
            statusMessage = result
            equipButtonId = newId("equip-btn", discordId)
            removeButtonId = newId("remove-btn", discordId)

            response.edit {
                embed {
                    title = "🧩 Equipamentos"
                    color = embedColor(equipped)
                    description = slotDescription(equipped)
                    footer { text = statusMessage }
                }
                components = mutableListOf()
                addComponent(overviewButtons(equipButtonId, removeButtonId, equipped.isNotEmpty()))
            }
        }
    }

    // Returns a status message (success or error) to show in the footer, or null on timeout.
    private suspend fun handleEquipFlow(
        event: ChatInputCommandInteractionCreateEvent,
        discordId: Long,
        buttonEvent: ButtonInteractionCreateEvent
    ): String? {
        val user = userRepository.getUserByDiscordId(discordId) ?: run {
            buttonEvent.interaction.deferEphemeralMessageUpdate()
            return "❌ Conta não encontrada."
        }

        val equippedIds = equippedCardsRepository.getEquippedCardInstanceIdsForUser(user.id)
        val unequipped = cardInstanceRepository.getOwnedEquipmentsWithDefinition(user.id)
            .filter { (instance, _) -> instance.id !in equippedIds }
            .sortedWith(compareByDescending<Pair<CardInstance, CardDefinition>> { it.second.rarity.ordinal }.thenBy { it.second.name })

        if (unequipped.isEmpty()) {
            buttonEvent.interaction.deferEphemeralMessageUpdate()
            return "🎒 Nenhum equipamento disponível para equipar."
        }

        val selectId = newId("equip-select", discordId)
        val buttonUpdate = buttonEvent.interaction.deferEphemeralMessageUpdate()

        buttonUpdate.edit {
            embed {
                title = "🎒 Escolha um item"
                description = "Selecione qual item equipar no personagem ativo."
                if (unequipped.size > 25) footer { text = "Exibindo os 25 melhores de ${unequipped.size} itens" }
            }
            components = mutableListOf()
            addComponent(ActionRowBuilder().apply {
                stringSelect(selectId) {
                    placeholder = "Selecione um item..."
                    unequipped.take(25).forEach { (instance, def) ->
                        option(
                            label = "${def.rarity.toDisplayEmoji()} ${def.name}",
                            value = instance.id.toString()
                        ) {
                            description = "${def.slot?.icon ?: "?"} ${def.slot?.displayName ?: "Sem slot"} • Nv.${instance.level} • ${def.rarity.toDisplayName()}"
                        }
                    }
                }
            })
        }

        val selectEvent = event.kord.awaitStringSelectInteraction(selectId, discordId) ?: return null

        val itemInstanceId = selectEvent.interaction.values.firstOrNull()?.toIntOrNull() ?: run {
            selectEvent.interaction.deferEphemeralMessageUpdate()
            return "❌ Seleção inválida."
        }

        selectEvent.interaction.deferEphemeralMessageUpdate()

        return when (val result = equipItemHandler.execute(discordId, itemInstanceId)) {
            is EquipItemHandler.Result.Success -> {
                val replaced = if (result.replaced) " (substituiu o item anterior)" else ""
                "✅ Item equipado em ${result.slot.icon} **${result.slot.displayName}**$replaced."
            }
            is EquipItemHandler.Result.InvalidSlot -> "❌ Esse item não possui slot de equipamento."
            is EquipItemHandler.Result.NoActiveCharacter -> "❌ Defina um personagem ativo antes de equipar itens."
            is EquipItemHandler.Result.InvalidItem -> "❌ Item inválido ou não pertence a você."
            is EquipItemHandler.Result.ItemAlreadyEquipped -> "❌ Esse item já está equipado."
        }
    }

    // Returns a status message (success or error) to show in the footer, or null on timeout.
    private suspend fun handleRemoveFlow(
        event: ChatInputCommandInteractionCreateEvent,
        discordId: Long,
        buttonEvent: ButtonInteractionCreateEvent,
        equipped: List<EquippedItemView>
    ): String? {
        val selectId = newId("remove-select", discordId)
        val buttonUpdate = buttonEvent.interaction.deferEphemeralMessageUpdate()

        buttonUpdate.edit {
            embed {
                title = "🗑️ Remover equipamento"
                description = "Selecione o slot que deseja desequipar."
            }
            components = mutableListOf()
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

        val selectEvent = event.kord.awaitStringSelectInteraction(selectId, discordId) ?: return null

        val slotInput = selectEvent.interaction.values.firstOrNull() ?: run {
            selectEvent.interaction.deferEphemeralMessageUpdate()
            return "❌ Seleção inválida."
        }

        selectEvent.interaction.deferEphemeralMessageUpdate()

        return when (val result = unequipItemHandler.execute(discordId, slotInput)) {
            is UnequipItemHandler.Result.Success -> {
                val itemName = equipped.firstOrNull { EquipmentSlot.fromIndex(it.slot)?.name == slotInput }?.name ?: "Item"
                "✅ **$itemName** removido do slot ${result.slot.icon} **${result.slot.displayName}**."
            }
            is UnequipItemHandler.Result.InvalidSlot -> "❌ Slot inválido: `${result.input}`."
            is UnequipItemHandler.Result.NoActiveCharacter -> "❌ Defina um personagem ativo antes de remover itens."
            is UnequipItemHandler.Result.EmptySlot -> "❌ Esse slot não possui item equipado."
        }
    }

    private fun slotDescription(equipped: List<EquippedItemView>) = buildString {
        for (slot in EquipmentSlot.entries) {
            val item = equipped.firstOrNull { it.slot == slot.index }
            if (item == null) {
                appendLine("${slot.icon} **${slot.displayName}** — *vazio*")
            } else {
                appendLine("${slot.icon} **${slot.displayName}** — **${item.name}** ${item.rarity.toDisplayEmoji()}")
            }
        }
    }

    private fun embedColor(equipped: List<EquippedItemView>) = equipped
        .mapNotNull { Rarity.entries.getOrNull(it.rarity.ordinal) }
        .maxByOrNull { it.ordinal }
        ?.colorDefinition()
        ?: Color(0x2b2d31)

    private fun overviewButtons(equipButtonId: String, removeButtonId: String, hasEquipped: Boolean) =
        ActionRowBuilder().apply {
            interactionButton(ButtonStyle.Success, equipButtonId) {
                label = "Equipar"
                emoji = DiscordPartialEmoji(name = "🎒")
            }
            if (hasEquipped) {
                interactionButton(ButtonStyle.Danger, removeButtonId) {
                    label = "Remover"
                    emoji = DiscordPartialEmoji(name = "🗑️")
                }
            }
        }

    private fun newId(prefix: String, discordId: Long) = "$prefix-$discordId-${System.currentTimeMillis()}"
}
