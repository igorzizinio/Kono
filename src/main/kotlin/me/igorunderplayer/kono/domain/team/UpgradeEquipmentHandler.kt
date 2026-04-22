package me.igorunderplayer.kono.domain.team

import me.igorunderplayer.kono.data.repositories.CardInstanceRepository
import me.igorunderplayer.kono.data.repositories.UserRepository
import me.igorunderplayer.kono.domain.card.CardType
import me.igorunderplayer.kono.domain.card.Rarity
import kotlin.math.pow
import kotlin.math.roundToInt

class UpgradeEquipmentHandler(
    private val userRepository: UserRepository,
    private val cardInstanceRepository: CardInstanceRepository
) {

    companion object {
        const val BASE_KONOS_COST = 150
        const val KONOS_EXP_GROWTH = 1.33

        const val BASE_SMITHING_STONES_REQUIRED = 2
        const val SMITHING_STONES_EXP_GROWTH = 1.18

        const val COMMON_LEVEL_CAP = 6
        const val RARE_LEVEL_CAP = 10
        const val EPIC_LEVEL_CAP = 14
        const val LEGENDARY_LEVEL_CAP = 18
        const val MYTHIC_LEVEL_CAP = 20
    }

    data class UpgradeCost(
        val currentLevel: Int,
        val nextLevel: Int,
        val konosCost: Long,
        val smithingStonesRequired: Int,
        val maxLevel: Int
    )

    sealed class PreviewResult {
        data class Ready(
            val instanceId: Int,
            val equipmentName: String,
            val cost: UpgradeCost,
            val currentKonos: Long,
            val currentSmithingStones: Int
        ) : PreviewResult()

        object UserNotFound : PreviewResult()
        data class EquipmentNotFound(val instanceId: Int) : PreviewResult()
        data class InvalidCardType(val definitionId: String) : PreviewResult()
        data class MaxLevelReached(val currentLevel: Int, val levelCap: Int) : PreviewResult()
        data class NotEnoughKonos(val required: Long, val current: Long) : PreviewResult()
        data class NotEnoughSmithingStones(val required: Int, val current: Int) : PreviewResult()
    }

    sealed class Result {
        data class Success(
            val instanceId: Int,
            val equipmentName: String,
            val previousLevel: Int,
            val newLevel: Int,
            val konosSpent: Long,
            val smithingStonesSpent: Int,
            val remainingKonos: Long,
            val remainingSmithingStones: Int
        ) : Result()

        object UserNotFound : Result()
        data class EquipmentNotFound(val instanceId: Int) : Result()
        data class InvalidCardType(val definitionId: String) : Result()
        data class MaxLevelReached(val currentLevel: Int, val levelCap: Int) : Result()
        data class NotEnoughKonos(val required: Long, val current: Long) : Result()
        data class NotEnoughSmithingStones(val required: Int, val current: Int) : Result()
        object PersistFailed : Result()
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

        val currentLevel = instance.level
        val levelCap = resolveLevelCap(definition.rarity)
        if (currentLevel >= levelCap) {
            return PreviewResult.MaxLevelReached(currentLevel, levelCap)
        }

        val cost = resolveUpgradeCost(currentLevel, levelCap, definition.rarity)

        if (user.konos < cost.konosCost) {
            return PreviewResult.NotEnoughKonos(required = cost.konosCost, current = user.konos)
        }

        val currentSmithingStones = user.smithingStones

        if (currentSmithingStones < cost.smithingStonesRequired) {
            return PreviewResult.NotEnoughSmithingStones(required = cost.smithingStonesRequired, current = currentSmithingStones)
        }

        return PreviewResult.Ready(
            instanceId = instance.id,
            equipmentName = definition.name,
            cost = cost,
            currentKonos = user.konos,
            currentSmithingStones = currentSmithingStones
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

        val currentLevel = instance.level
        val levelCap = resolveLevelCap(definition.rarity)
        if (currentLevel >= levelCap) {
            return Result.MaxLevelReached(currentLevel, levelCap)
        }

        val cost = resolveUpgradeCost(currentLevel, levelCap, definition.rarity)
        if (user.konos < cost.konosCost) {
            return Result.NotEnoughKonos(required = cost.konosCost, current = user.konos)
        }

        val currentSmithingStones = user.smithingStones
        if (currentSmithingStones < cost.smithingStonesRequired) {
            return Result.NotEnoughSmithingStones(required = cost.smithingStonesRequired, current = currentSmithingStones)
        }

        val newKonos = user.konos - cost.konosCost
        val konosUpdated = userRepository.updateKonos(user.id, newKonos)
        if (!konosUpdated) {
            return Result.PersistFailed
        }

        val newSmithingStones = currentSmithingStones - cost.smithingStonesRequired
        val smithingUpdated = userRepository.updateSmithingStones(user.id, newSmithingStones)
        if (!smithingUpdated) {
            return Result.PersistFailed
        }

        val nextLevel = currentLevel + 1
        val levelUpdated = cardInstanceRepository.updateEquipmentLevel(instance.id, nextLevel)
        if (!levelUpdated) {
            return Result.PersistFailed
        }

        return Result.Success(
            instanceId = instance.id,
            equipmentName = definition.name,
            previousLevel = currentLevel,
            newLevel = nextLevel,
            konosSpent = cost.konosCost,
            smithingStonesSpent = cost.smithingStonesRequired,
            remainingKonos = newKonos,
            remainingSmithingStones = newSmithingStones
        )
    }

    private fun resolveKonosCost(currentLevel: Int, rarity: Rarity): Long {
        val exponent = (currentLevel - 1).coerceAtLeast(0)

        val base = BASE_KONOS_COST * KONOS_EXP_GROWTH.pow(exponent)
        val scaled = base * rarityMultiplier(rarity)

        return scaled.roundToInt().toLong().coerceAtLeast(BASE_KONOS_COST.toLong())
    }

    private fun resolveSmithingStonesRequired(currentLevel: Int, rarity: Rarity): Int {
        val exponent = (currentLevel - 1).coerceAtLeast(0)

        val base = BASE_SMITHING_STONES_REQUIRED * SMITHING_STONES_EXP_GROWTH.pow(exponent)
        val scaled = base * rarityMultiplier(rarity)

        return scaled.roundToInt().coerceAtLeast(1)
    }

    private fun resolveUpgradeCost(currentLevel: Int, levelCap: Int, rarity: Rarity): UpgradeCost {
        return UpgradeCost(
            currentLevel = currentLevel,
            nextLevel = currentLevel + 1,
            konosCost = resolveKonosCost(currentLevel, rarity),
            smithingStonesRequired = resolveSmithingStonesRequired(currentLevel, rarity),
            maxLevel = levelCap
        )
    }

    private fun resolveLevelCap(rarity: Rarity): Int {
        return when (rarity) {
            Rarity.COMMON -> COMMON_LEVEL_CAP
            Rarity.RARE -> RARE_LEVEL_CAP
            Rarity.EPIC -> EPIC_LEVEL_CAP
            Rarity.LEGENDARY -> LEGENDARY_LEVEL_CAP
            Rarity.MYTHIC -> MYTHIC_LEVEL_CAP
            Rarity.KONO -> MYTHIC_LEVEL_CAP
        }
    }

    private fun rarityMultiplier(rarity: Rarity): Double {
        return when (rarity) {
            Rarity.COMMON -> 1.0
            Rarity.RARE -> 1.25
            Rarity.EPIC -> 1.6
            Rarity.LEGENDARY -> 2.2
            Rarity.MYTHIC -> 3.0
            Rarity.KONO -> 4.0
        }
    }
}

