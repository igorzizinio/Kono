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

        const val BASE_COPIES_REQUIRED = 2
        const val COPIES_EXP_GROWTH = 1.18

        const val COMMON_LEVEL_CAP = 6
        const val RARE_LEVEL_CAP = 10
        const val EPIC_LEVEL_CAP = 14
        const val LEGENDARY_LEVEL_CAP = 18
        const val MYTHIC_LEVEL_CAP = 20
    }

    data class UpgradeCost(
        val currentLevel: Int,
        val nextLevel: Int,
        val konosCost: Int,
        val copiesRequired: Int,
        val maxLevel: Int
    )

    sealed class PreviewResult {
        data class Ready(
            val instanceId: Int,
            val equipmentName: String,
            val cost: UpgradeCost,
            val currentKonos: Int,
            val availableCopies: Int
        ) : PreviewResult()

        object UserNotFound : PreviewResult()
        data class EquipmentNotFound(val instanceId: Int) : PreviewResult()
        data class InvalidCardType(val definitionId: String) : PreviewResult()
        data class MaxLevelReached(val currentLevel: Int, val levelCap: Int) : PreviewResult()
        data class NotEnoughKonos(val required: Int, val current: Int) : PreviewResult()
        data class NotEnoughCopies(val required: Int, val current: Int) : PreviewResult()
    }

    sealed class Result {
        data class Success(
            val instanceId: Int,
            val equipmentName: String,
            val previousLevel: Int,
            val newLevel: Int,
            val konosSpent: Int,
            val copiesSpent: Int,
            val remainingKonos: Int
        ) : Result()

        object UserNotFound : Result()
        data class EquipmentNotFound(val instanceId: Int) : Result()
        data class InvalidCardType(val definitionId: String) : Result()
        data class MaxLevelReached(val currentLevel: Int, val levelCap: Int) : Result()
        data class NotEnoughKonos(val required: Int, val current: Int) : Result()
        data class NotEnoughCopies(val required: Int, val current: Int) : Result()
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

        val cost = resolveUpgradeCost(currentLevel, levelCap)

        if (user.konos < cost.konosCost) {
            return PreviewResult.NotEnoughKonos(required = cost.konosCost, current = user.konos)
        }

        val availableCopies = cardInstanceRepository.countUnequippedDefinitionCopiesForUpgrade(
            userId = user.id,
            definitionId = instance.definitionId,
            exceptInstanceId = instance.id
        )

        if (availableCopies < cost.copiesRequired) {
            return PreviewResult.NotEnoughCopies(required = cost.copiesRequired, current = availableCopies)
        }

        return PreviewResult.Ready(
            instanceId = instance.id,
            equipmentName = definition.name,
            cost = cost,
            currentKonos = user.konos,
            availableCopies = availableCopies
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

        val cost = resolveUpgradeCost(currentLevel, levelCap)
        if (user.konos < cost.konosCost) {
            return Result.NotEnoughKonos(required = cost.konosCost, current = user.konos)
        }

        val availableCopies = cardInstanceRepository.countUnequippedDefinitionCopiesForUpgrade(
            userId = user.id,
            definitionId = instance.definitionId,
            exceptInstanceId = instance.id
        )

        if (availableCopies < cost.copiesRequired) {
            return Result.NotEnoughCopies(required = cost.copiesRequired, current = availableCopies)
        }

        val consumedCopies = cardInstanceRepository.consumeUnequippedDefinitionCopies(
            userId = user.id,
            definitionId = instance.definitionId,
            exceptInstanceId = instance.id,
            amount = cost.copiesRequired
        )

        if (consumedCopies != cost.copiesRequired) {
            return Result.PersistFailed
        }

        val newKonos = user.konos - cost.konosCost
        val konosUpdated = userRepository.updateKonos(user.id, newKonos)
        if (!konosUpdated) {
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
            copiesSpent = cost.copiesRequired,
            remainingKonos = newKonos
        )
    }

    private fun resolveKonosCost(currentLevel: Int): Int {
        val exponent = (currentLevel - 1).coerceAtLeast(0)
        return (BASE_KONOS_COST * KONOS_EXP_GROWTH.pow(exponent)).roundToInt().coerceAtLeast(BASE_KONOS_COST)
    }

    private fun resolveCopiesRequired(currentLevel: Int): Int {
        val exponent = (currentLevel - 1).coerceAtLeast(0)
        return (BASE_COPIES_REQUIRED * COPIES_EXP_GROWTH.pow(exponent)).roundToInt().coerceAtLeast(1)
    }

    private fun resolveUpgradeCost(currentLevel: Int, levelCap: Int): UpgradeCost {
        return UpgradeCost(
            currentLevel = currentLevel,
            nextLevel = currentLevel + 1,
            konosCost = resolveKonosCost(currentLevel),
            copiesRequired = resolveCopiesRequired(currentLevel),
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
}

