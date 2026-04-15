package me.igorunderplayer.kono.services

import me.igorunderplayer.kono.data.repositories.CardInstanceRepository
import me.igorunderplayer.kono.data.repositories.CardRepository
import me.igorunderplayer.kono.data.repositories.UserRepository
import me.igorunderplayer.kono.domain.card.CardType
import me.igorunderplayer.kono.domain.card.Rarity
import kotlin.random.Random

const val GACHA_COST = 40

data class PulledCard (
    val cardName: String,
    val rarity: Rarity,
    val type: CardType
)

sealed class GachaResult {

    object UserNotFound : GachaResult()
    object NotEnoughEssence : GachaResult()
    object NoCardsAvailable : GachaResult()
    object Error : GachaResult()

    data class Success(
        val cardName: String,
        val rarity: Rarity,
        val type: CardType,
        val remainingEssence: Int
    ) : GachaResult()

    data class MultipePullSuccess(
        val pulledCards: List<PulledCard>,
        val remainingEssence: Int
    ) : GachaResult()
}

class GachaService(
    private val userRepository: UserRepository,
    private val cardRepository: CardRepository,
    private val cardInstanceRepository: CardInstanceRepository
) {

    suspend fun pull(discordId: Long, multiple: Boolean = false): GachaResult {
        val user = userRepository.getUserByDiscordId(discordId)
            ?: return GachaResult.UserNotFound

        val pullCount = if (multiple) 10 else 1
        val totalCost = GACHA_COST * pullCount

        if (user.essence < totalCost) {
            return GachaResult.NotEnoughEssence
        }

        val results = mutableListOf<PulledCard>()

        repeat(pullCount) {
            val rarity = rollRarity()

            val pool = cardRepository.getByRarity(rarity)

            if (pool.isEmpty()) {
                return GachaResult.NoCardsAvailable
            }

            val card = pool.random()

            val inserted = cardInstanceRepository.insert(
                userId = user.id,
                definitionId = card.id
            )

            if (!inserted) return GachaResult.Error

            results.add(
                PulledCard(
                    cardName = card.name,
                    rarity = card.rarity,
                    type = card.type,
                )
            )
        }

        val newEssence = user.essence - totalCost
        val updated = userRepository.updateEssence(user.id, newEssence)

        if (!updated) return GachaResult.Error

        return if (multiple) {
            GachaResult.MultipePullSuccess(
                pulledCards = results,
                remainingEssence = newEssence
            )
        } else {
            val card = results.first()
            GachaResult.Success(
                cardName = card.cardName,
                rarity = card.rarity,
                remainingEssence = newEssence,
                type = card.type
            )
        }
    }

    // 🎰 chances por raridade
    private fun rollRarity(): Rarity {
        val roll = Random.nextDouble() * 100 // 0.0 até 100.0

        return when {
            roll < 60.0 -> Rarity.COMMON        // 50%
            roll < 85.0 -> Rarity.RARE          // 25%
            roll < 98.6 -> Rarity.EPIC          // 13.6%
            roll < 99.8 -> Rarity.LEGENDARY     // 1.2%
            else -> Rarity.MYTHIC               // 0.2%
        }
    }
}
