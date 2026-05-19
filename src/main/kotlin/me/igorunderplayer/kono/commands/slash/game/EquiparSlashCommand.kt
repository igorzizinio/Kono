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
import me.igorunderplayer.kono.data.repositories.EquippedCardsRepository
import me.igorunderplayer.kono.data.repositories.UserRepository
import me.igorunderplayer.kono.domain.card.toDisplayEmoji
import me.igorunderplayer.kono.domain.card.toDisplayName
import me.igorunderplayer.kono.domain.team.EquipItemHandler
import me.igorunderplayer.kono.utils.interaction.awaitStringSelectInteraction

class EquiparSlashCommand(
    private val equipItemHandler: EquipItemHandler,
    private val cardInstanceRepository: CardInstanceRepository,
    private val equippedCardsRepository: EquippedCardsRepository,
    private val userRepository: UserRepository
) : KonoSlashCommand {

    override val name = "equipar"
    override val description = "Equipa um item no personagem ativo via menu de seleção"
    override val options: List<ApplicationCommandOption> = listOf()

    override suspend fun run(event: ChatInputCommandInteractionCreateEvent) {
        val deferred = event.interaction.deferEphemeralResponse()
        val discordId = event.interaction.user.id.value.toLong()

        val user = userRepository.getUserByDiscordId(discordId) ?: run {
            deferred.respond { content = "❌ Você ainda não tem conta. Use `/register` primeiro." }
            return
        }

        val allEquipment = cardInstanceRepository.getOwnedEquipmentsWithDefinition(user.id)
        val unequipped = allEquipment.filter { (instance, _) ->
            !equippedCardsRepository.existsByCardInstanceId(instance.id)
        }

        if (unequipped.isEmpty()) {
            deferred.respond { content = "🎒 Nenhum equipamento disponível para equipar no momento." }
            return
        }

        val selectId = "equipar-${discordId}-${System.currentTimeMillis()}"

        val response = deferred.respond {
            embed {
                title = "🎒 Equipar item"
                description = "Selecione um item do inventário para equipar no personagem ativo."
                if (unequipped.size > 25) {
                    footer { text = "Exibindo os primeiros 25 de ${unequipped.size} itens" }
                }
            }
            addComponent(ActionRowBuilder().apply {
                stringSelect(selectId) {
                    placeholder = "Selecione um item..."
                    unequipped.take(25).forEach { (instance, def) ->
                        option(
                            label = "${def.rarity.toDisplayEmoji()} ${def.name}",
                            value = instance.id.toString()
                        ) {
                            description = "${def.slot?.displayName ?: "?"} • Nível ${instance.level} • ${def.rarity.toDisplayName()}"
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
            selectEvent.interaction.respondEphemeral { content = "❌ Seleção inválida." }
            return
        }

        val update = selectEvent.interaction.deferEphemeralMessageUpdate()

        when (val result = equipItemHandler.execute(discordId, itemInstanceId)) {
            is EquipItemHandler.Result.Success -> {
                val replaced = if (result.replaced) " (substituiu o item anterior)" else ""
                update.edit {
                    content = "✅ Item equipado no slot ${result.slot.icon} **${result.slot.displayName}**$replaced."
                    components = mutableListOf()
                }
            }
            is EquipItemHandler.Result.InvalidSlot ->
                update.edit { content = "❌ Esse item não possui slot de equipamento definido."; components = mutableListOf() }
            is EquipItemHandler.Result.NoActiveCharacter ->
                update.edit { content = "❌ Selecione um personagem ativo antes de equipar itens."; components = mutableListOf() }
            is EquipItemHandler.Result.InvalidItem ->
                update.edit { content = "❌ Item inválido ou não pertence a você."; components = mutableListOf() }
            is EquipItemHandler.Result.ItemAlreadyEquipped ->
                update.edit { content = "❌ Esse item já está equipado em algum personagem."; components = mutableListOf() }
        }
    }
}
