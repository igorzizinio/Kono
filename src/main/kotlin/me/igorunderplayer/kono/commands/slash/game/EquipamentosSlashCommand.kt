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

        val equipped = cardInstanceRepository.getEquippedItemsForActiveCharacter(discordId)
        val embedColor = equipped
            .mapNotNull { Rarity.entries.getOrNull(it.rarity.ordinal) }
            .maxByOrNull { it.ordinal }
            ?.colorDefinition()
            ?: Color(0x2b2d31)

        val equipButtonId = "equip-btn-${discordId}-${System.currentTimeMillis()}"
        val removeButtonId = "remove-btn-${discordId}-${System.currentTimeMillis() + 1}"

        val response = deferred.respond {
            embed {
                title = "🧩 Equipamentos"
                color = embedColor
                description = buildString {
                    for (slot in EquipmentSlot.entries) {
                        val item = equipped.firstOrNull { it.slot == slot.index }
                        if (item == null) {
                            appendLine("${slot.icon} **${slot.displayName}** — *vazio*")
                        } else {
                            appendLine("${slot.icon} **${slot.displayName}** — **${item.name}** ${item.rarity.toDisplayEmoji()}")
                        }
                    }
                }
                footer { text = "Escolha uma ação abaixo" }
            }
            addComponent(ActionRowBuilder().apply {
                interactionButton(ButtonStyle.Success, equipButtonId) {
                    label = "Equipar"
                    emoji = DiscordPartialEmoji(name = "🎒")
                }
                if (equipped.isNotEmpty()) {
                    interactionButton(ButtonStyle.Danger, removeButtonId) {
                        label = "Remover"
                        emoji = DiscordPartialEmoji(name = "🗑️")
                    }
                }
            })
        }

        val buttonIds = buildList {
            add(equipButtonId)
            if (equipped.isNotEmpty()) add(removeButtonId)
        }

        val clicked = event.kord.awaitFirstButtonInteraction(buttonIds, discordId)
        if (clicked == null) {
            response.edit { content = "⏰ Tempo esgotado."; components = mutableListOf() }
            return
        }

        val (clickedId, clickEvent) = clicked

        if (clickedId == equipButtonId) {
            handleEquipFlow(event, discordId, clickEvent, response)
        } else {
            handleRemoveFlow(event, discordId, clickEvent, response, equipped)
        }
    }

    private suspend fun handleEquipFlow(
        event: ChatInputCommandInteractionCreateEvent,
        discordId: Long,
        buttonEvent: ButtonInteractionCreateEvent,
        response: dev.kord.core.behavior.interaction.response.EphemeralMessageInteractionResponseBehavior
    ) {
        val user = userRepository.getUserByDiscordId(discordId) ?: run {
            response.edit { content = "❌ Conta não encontrada."; components = mutableListOf() }
            return
        }

        // Single bulk query for all equipped IDs — no N+1
        val equippedIds = equippedCardsRepository.getEquippedCardInstanceIdsForUser(user.id)
        val unequipped = cardInstanceRepository.getOwnedEquipmentsWithDefinition(user.id)
            .filter { (instance, _) -> instance.id !in equippedIds }
            .sortedWith(compareByDescending<Pair<CardInstance, CardDefinition>> { it.second.rarity.ordinal }.thenBy { it.second.name })

        if (unequipped.isEmpty()) {
            response.edit { content = "🎒 Nenhum equipamento disponível para equipar."; components = mutableListOf() }
            return
        }

        val selectId = "equip-select-${discordId}-${System.currentTimeMillis()}"
        val update = buttonEvent.interaction.deferEphemeralMessageUpdate()

        update.edit {
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

        val selectEvent = event.kord.awaitStringSelectInteraction(selectId, discordId) ?: run {
            response.edit { content = "⏰ Tempo esgotado."; components = mutableListOf() }
            return
        }

        val itemInstanceId = selectEvent.interaction.values.firstOrNull()?.toIntOrNull() ?: run {
            response.edit { content = "❌ Seleção inválida."; components = mutableListOf() }
            return
        }

        val selectUpdate = selectEvent.interaction.deferEphemeralMessageUpdate()

        when (val result = equipItemHandler.execute(discordId, itemInstanceId)) {
            is EquipItemHandler.Result.Success -> {
                val replaced = if (result.replaced) " (substituiu o item anterior)" else ""
                selectUpdate.edit {
                    content = "✅ Item equipado em ${result.slot.icon} **${result.slot.displayName}**$replaced."
                    components = mutableListOf()
                }
            }
            is EquipItemHandler.Result.InvalidSlot ->
                selectUpdate.edit { content = "❌ Esse item não possui slot de equipamento."; components = mutableListOf() }
            is EquipItemHandler.Result.NoActiveCharacter ->
                selectUpdate.edit { content = "❌ Defina um personagem ativo antes de equipar itens."; components = mutableListOf() }
            is EquipItemHandler.Result.InvalidItem ->
                selectUpdate.edit { content = "❌ Item inválido ou não pertence a você."; components = mutableListOf() }
            is EquipItemHandler.Result.ItemAlreadyEquipped ->
                selectUpdate.edit { content = "❌ Esse item já está equipado."; components = mutableListOf() }
        }
    }

    private suspend fun handleRemoveFlow(
        event: ChatInputCommandInteractionCreateEvent,
        discordId: Long,
        buttonEvent: ButtonInteractionCreateEvent,
        response: dev.kord.core.behavior.interaction.response.EphemeralMessageInteractionResponseBehavior,
        equipped: List<EquippedItemView>
    ) {
        val selectId = "remove-select-${discordId}-${System.currentTimeMillis()}"
        val update = buttonEvent.interaction.deferEphemeralMessageUpdate()

        update.edit {
            embed {
                title = "🗑️ Remover equipamento"
                description = "Selecione o slot que deseja desquipar."
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

        val selectEvent = event.kord.awaitStringSelectInteraction(selectId, discordId) ?: run {
            response.edit { content = "⏰ Tempo esgotado."; components = mutableListOf() }
            return
        }

        val slotInput = selectEvent.interaction.values.firstOrNull() ?: run {
            response.edit { content = "❌ Seleção inválida."; components = mutableListOf() }
            return
        }

        val selectUpdate = selectEvent.interaction.deferEphemeralMessageUpdate()

        when (val result = unequipItemHandler.execute(discordId, slotInput)) {
            is UnequipItemHandler.Result.Success -> {
                val itemName = equipped.firstOrNull { EquipmentSlot.fromIndex(it.slot)?.name == slotInput }?.name ?: "Item"
                selectUpdate.edit {
                    content = "✅ **$itemName** removido do slot ${result.slot.icon} **${result.slot.displayName}**."
                    components = mutableListOf()
                }
            }
            is UnequipItemHandler.Result.InvalidSlot ->
                selectUpdate.edit { content = "❌ Slot inválido: `${result.input}`."; components = mutableListOf() }
            is UnequipItemHandler.Result.NoActiveCharacter ->
                selectUpdate.edit { content = "❌ Defina um personagem ativo antes de remover itens."; components = mutableListOf() }
            is UnequipItemHandler.Result.EmptySlot ->
                selectUpdate.edit { content = "❌ Esse slot não possui item equipado."; components = mutableListOf() }
        }
    }
}
