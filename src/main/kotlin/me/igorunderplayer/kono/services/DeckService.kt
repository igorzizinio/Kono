package me.igorunderplayer.kono.services

import me.igorunderplayer.kono.data.repositories.CardInstanceRepository
import me.igorunderplayer.kono.data.repositories.DeckRepository
import me.igorunderplayer.kono.domain.card.CardCatalog
import me.igorunderplayer.kono.domain.gameplay.DuelRulesConfig

class DeckService(
    private val userService: UserService,
    private val cardInstanceRepository: CardInstanceRepository,
    private val deckRepository: DeckRepository,
) {
    suspend fun list(discordId: Long) =
        userService.getUserByDiscordId(discordId)?.let { deckRepository.listByUserId(it.id) } ?: emptyList()

    suspend fun create(discordId: Long, name: String): String {
        val user = userService.getUserByDiscordId(discordId) ?: return "❌ Você não está registrado."
        val normalized = name.trim()
        if (normalized.isEmpty() || normalized.length > 32) return "❌ O nome deve ter entre 1 e 32 caracteres."
        return if (deckRepository.create(user.id, normalized)) "✅ Deck **$normalized** criado."
        else "❌ Você já possui um deck com esse nome."
    }

    suspend fun add(discordId: Long, name: String, cardId: String): String {
        val user = userService.getUserByDiscordId(discordId) ?: return "❌ Você não está registrado."
        val deck = deckRepository.findByName(user.id, name) ?: return "❌ Deck não encontrado."
        val definition = CardCatalog.getById(cardId) ?: return "❌ Carta não encontrada."
        if (deck.cardIds.size >= DuelRulesConfig.DECK_SIZE) return "❌ O deck já possui ${DuelRulesConfig.DECK_SIZE} cartas."
        if (deck.cardIds.count { it.equals(definition.id, true) } >= DuelRulesConfig.MAX_COPIES_PER_CARD) {
            return "❌ Essa carta já atingiu o limite de ${DuelRulesConfig.MAX_COPIES_PER_CARD} cópias."
        }
        val owned = cardInstanceRepository.countOwnedDefinitionInstances(user.id, definition.id)
        if (deck.cardIds.count { it.equals(definition.id, true) } >= owned) {
            return "❌ Você não possui uma cópia disponível dessa carta."
        }
        val legendaryOrMythic = deck.cardIds.count {
            val card = CardCatalog.getById(it)
            card?.type != me.igorunderplayer.kono.domain.card.CardType.CHARACTER &&
                card?.rarity in setOf(
                    me.igorunderplayer.kono.domain.card.Rarity.LEGENDARY,
                    me.igorunderplayer.kono.domain.card.Rarity.MYTHIC
                )
        }
        if (definition.type != me.igorunderplayer.kono.domain.card.CardType.CHARACTER &&
            definition.rarity in setOf(
                me.igorunderplayer.kono.domain.card.Rarity.LEGENDARY,
                me.igorunderplayer.kono.domain.card.Rarity.MYTHIC
            ) &&
            legendaryOrMythic >= DuelRulesConfig.MAX_LEGENDARY_OR_MYTHIC
        ) {
            return "❌ O deck já atingiu o limite de Legendary/Mythic."
        }
        return if (deckRepository.addCard(user.id, deck.deck.name, definition.id)) "✅ ${definition.name} adicionada ao deck."
        else "❌ Não foi possível atualizar o deck."
    }

    suspend fun remove(discordId: Long, name: String, slot: Int): String {
        val user = userService.getUserByDiscordId(discordId) ?: return "❌ Você não está registrado."
        if (slot !in 1..DuelRulesConfig.DECK_SIZE) return "❌ O slot deve estar entre 1 e ${DuelRulesConfig.DECK_SIZE}."
        return if (deckRepository.removeCard(user.id, name, slot - 1)) "✅ Carta removida."
        else "❌ Deck ou slot não encontrado."
    }

    suspend fun activate(discordId: Long, name: String): String {
        val user = userService.getUserByDiscordId(discordId) ?: return "❌ Você não está registrado."
        return if (deckRepository.setActive(user.id, name)) "✅ Deck **$name** selecionado."
        else "❌ Deck não encontrado."
    }

    suspend fun getActive(discordId: Long) =
        userService.getUserByDiscordId(discordId)?.let { deckRepository.getActive(it.id) }

    fun validate(cardIds: List<String>): List<String> {
        val definitions = cardIds.mapNotNull { CardCatalog.getById(it) }
        if (definitions.size != cardIds.size) return listOf("O deck contém cartas inválidas.")
        val errors = mutableListOf<String>()
        if (definitions.size != DuelRulesConfig.DECK_SIZE) {
            errors += "O deck deve ter exatamente ${DuelRulesConfig.DECK_SIZE} cartas."
        }
        val characterCount = definitions.count {
            it.type == me.igorunderplayer.kono.domain.card.CardType.CHARACTER
        }
        if (characterCount != 1) {
            errors += "O deck deve ter exatamente 1 personagem."
        }
        definitions.groupingBy { it.id }.eachCount()
            .filterValues { it > DuelRulesConfig.MAX_COPIES_PER_CARD }
            .forEach { (id, count) ->
                errors += "A carta $id aparece $count vezes; o máximo é ${DuelRulesConfig.MAX_COPIES_PER_CARD}."
            }
        val legendaryOrMythic = definitions.count {
            it.type != me.igorunderplayer.kono.domain.card.CardType.CHARACTER &&
                it.rarity in setOf(
                    me.igorunderplayer.kono.domain.card.Rarity.LEGENDARY,
                    me.igorunderplayer.kono.domain.card.Rarity.MYTHIC
                )
        }
        if (legendaryOrMythic > DuelRulesConfig.MAX_LEGENDARY_OR_MYTHIC) {
            errors += "O deck pode ter no máximo ${DuelRulesConfig.MAX_LEGENDARY_OR_MYTHIC} carta Legendary/Mythic."
        }
        return errors
    }
}
