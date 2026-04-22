package me.igorunderplayer.kono.domain.team

import me.igorunderplayer.kono.data.repositories.CardInstanceRepository
import me.igorunderplayer.kono.data.repositories.EquippedCardsRepository
import me.igorunderplayer.kono.data.repositories.UserRepository
import me.igorunderplayer.kono.domain.card.CardType
import me.igorunderplayer.kono.domain.card.Rarity

class DismantleEquipmentHandler(
    private val userRepository: UserRepository,
    private val cardInstanceRepository: CardInstanceRepository,
    private val equippedCardsRepository: EquippedCardsRepository
) {

    private val rarityOrder = listOf(
        Rarity.COMMON,
        Rarity.RARE,
        Rarity.EPIC,
        Rarity.LEGENDARY,
        Rarity.MYTHIC,
        Rarity.KONO
    )

    data class Reward(
        val rarity: Rarity,
        val smithingStones: Int
    )

    sealed class PreviewResult {
        data class Ready(
            val instanceId: Int,
            val equipmentName: String,
            val reward: Reward,
            val currentSmithingStones: Int
        ) : PreviewResult()

        object UserNotFound : PreviewResult()
        data class EquipmentNotFound(val instanceId: Int) : PreviewResult()
        data class InvalidCardType(val definitionId: String) : PreviewResult()
        data class EquipmentEquipped(val instanceId: Int) : PreviewResult()
    }

    sealed class Result {
        data class Success(
            val instanceId: Int,
            val equipmentName: String,
            val reward: Reward,
            val newSmithingStonesBalance: Int
        ) : Result()

        object UserNotFound : Result()
        data class EquipmentNotFound(val instanceId: Int) : Result()
        data class InvalidCardType(val definitionId: String) : Result()
        data class EquipmentEquipped(val instanceId: Int) : Result()
        object PersistFailed : Result()
    }

    sealed class BulkPreviewResult {
        data class Ready(
            val maxRarity: Rarity,
            val dismantleCount: Int,
            val totalSmithingStonesReward: Int,
            val currentSmithingStones: Int
        ) : BulkPreviewResult()

        object UserNotFound : BulkPreviewResult()
        data class NoEligibleEquipment(val maxRarity: Rarity) : BulkPreviewResult()
    }

    sealed class BulkResult {
        data class Success(
            val maxRarity: Rarity,
            val dismantledCount: Int,
            val totalSmithingStonesReward: Int,
            val newSmithingStonesBalance: Int
        ) : BulkResult()

        object UserNotFound : BulkResult()
        data class NoEligibleEquipment(val maxRarity: Rarity) : BulkResult()
        object PersistFailed : BulkResult()
    }

    suspend fun preview(discordId: Long, equipmentInstanceId: Int): PreviewResult {
        val user = userRepository.getUserByDiscordId(discordId)
            ?: return PreviewResult.UserNotFound

        val equipment = cardInstanceRepository.getOwnedEquipmentWithDefinition(user.id, equipmentInstanceId)
            ?: return PreviewResult.EquipmentNotFound(equipmentInstanceId)

        val (instance, definition) = equipment
        if (definition.type != CardType.EQUIPMENT) {
            return PreviewResult.InvalidCardType(definition.id)
        }

        if (equippedCardsRepository.existsByCardInstanceId(instance.id)) {
            return PreviewResult.EquipmentEquipped(instance.id)
        }

        return PreviewResult.Ready(
            instanceId = instance.id,
            equipmentName = definition.name,
            reward = Reward(definition.rarity, rewardByRarity(definition.rarity)),
            currentSmithingStones = user.smithingStones
        )
    }

    suspend fun execute(discordId: Long, equipmentInstanceId: Int): Result {
        val user = userRepository.getUserByDiscordId(discordId)
            ?: return Result.UserNotFound

        val equipment = cardInstanceRepository.getOwnedEquipmentWithDefinition(user.id, equipmentInstanceId)
            ?: return Result.EquipmentNotFound(equipmentInstanceId)

        val (instance, definition) = equipment
        if (definition.type != CardType.EQUIPMENT) {
            return Result.InvalidCardType(definition.id)
        }

        if (equippedCardsRepository.existsByCardInstanceId(instance.id)) {
            return Result.EquipmentEquipped(instance.id)
        }

        val reward = Reward(definition.rarity, rewardByRarity(definition.rarity))
        val newStones = user.smithingStones + reward.smithingStones

        val deleted = cardInstanceRepository.deleteOwnedEquipmentInstance(user.id, instance.id)
        if (!deleted) return Result.PersistFailed

        val updatedStones = userRepository.updateSmithingStones(user.id, newStones)
        if (!updatedStones) return Result.PersistFailed

        return Result.Success(
            instanceId = instance.id,
            equipmentName = definition.name,
            reward = reward,
            newSmithingStonesBalance = newStones
        )
    }

    suspend fun previewByRarity(discordId: Long, maxRarity: Rarity): BulkPreviewResult {
        val user = userRepository.getUserByDiscordId(discordId)
            ?: return BulkPreviewResult.UserNotFound

        val eligible = getEligibleEquipments(user.id, maxRarity)
        if (eligible.isEmpty()) {
            return BulkPreviewResult.NoEligibleEquipment(maxRarity)
        }

        val totalReward = eligible.sumOf { (_, definition) -> rewardByRarity(definition.rarity) }

        return BulkPreviewResult.Ready(
            maxRarity = maxRarity,
            dismantleCount = eligible.size,
            totalSmithingStonesReward = totalReward,
            currentSmithingStones = user.smithingStones
        )
    }

    suspend fun executeByRarity(discordId: Long, maxRarity: Rarity): BulkResult {
        val user = userRepository.getUserByDiscordId(discordId)
            ?: return BulkResult.UserNotFound

        val eligible = getEligibleEquipments(user.id, maxRarity)
        if (eligible.isEmpty()) {
            return BulkResult.NoEligibleEquipment(maxRarity)
        }

        var dismantledCount = 0
        var totalReward = 0

        for ((instance, definition) in eligible) {
            val deleted = cardInstanceRepository.deleteOwnedEquipmentInstance(user.id, instance.id)
            if (!deleted) continue

            dismantledCount++
            totalReward += rewardByRarity(definition.rarity)
        }

        if (dismantledCount <= 0) {
            return BulkResult.PersistFailed
        }

        val newStonesBalance = user.smithingStones + totalReward
        val updated = userRepository.updateSmithingStones(user.id, newStonesBalance)
        if (!updated) {
            return BulkResult.PersistFailed
        }

        return BulkResult.Success(
            maxRarity = maxRarity,
            dismantledCount = dismantledCount,
            totalSmithingStonesReward = totalReward,
            newSmithingStonesBalance = newStonesBalance
        )
    }

    private suspend fun getEligibleEquipments(userId: Int, maxRarity: Rarity): List<Pair<me.igorunderplayer.kono.data.entities.CardInstance, me.igorunderplayer.kono.domain.card.CardDefinition>> {
        val equippedIds = equippedCardsRepository.getEquippedCardsForUser(userId)
            .map { it.cardInstanceId }
            .toSet()

        return cardInstanceRepository.getOwnedEquipmentsWithDefinition(userId)
            .filter { (instance, definition) ->
                instance.id !in equippedIds && isAtOrBelowRarity(definition.rarity, maxRarity)
            }
    }

    private fun isAtOrBelowRarity(rarity: Rarity, maxRarity: Rarity): Boolean {
        return rarityOrder.indexOf(rarity) <= rarityOrder.indexOf(maxRarity)
    }

    private fun rewardByRarity(rarity: Rarity): Int {
        return when (rarity) {
            Rarity.COMMON -> 1
            Rarity.RARE -> 3
            Rarity.EPIC -> 6
            Rarity.LEGENDARY -> 10
            Rarity.MYTHIC -> 14
            Rarity.KONO -> 18
        }
    }
}

