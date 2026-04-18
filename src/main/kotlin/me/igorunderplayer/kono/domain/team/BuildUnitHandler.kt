package me.igorunderplayer.kono.domain.team

import me.igorunderplayer.kono.data.repositories.CardInstanceRepository
import me.igorunderplayer.kono.data.repositories.EquippedCardsRepository
import me.igorunderplayer.kono.data.repositories.UserRepository
import me.igorunderplayer.kono.domain.card.Stat
import me.igorunderplayer.kono.domain.card.ability.Ability
import me.igorunderplayer.kono.domain.gameplay.Unit

class BuildUnitHandler(
    private val userRepository: UserRepository,
    private val cardInstanceRepository: CardInstanceRepository,
    private val equippedCardsRepository: EquippedCardsRepository
) {

    sealed class Result {
        data class Success(val unit: Unit): Result()
        object UserNotFound: Result()
        object NoActiveCard: Result()
        data class CharacterNotFound(val activeCharacterId: Int): Result()

    }

    suspend fun executeByDiscordId(userId: Long): Result {
        val userRow = userRepository.getUserByDiscordId(userId)
            ?: return Result.UserNotFound

        val activeCharacterId = userRow.activeCharacterInstanceId
            ?: return Result.NoActiveCard

        val character = cardInstanceRepository.getOwnedCharacterWithDefinition(userRow.id, activeCharacterId)
            ?: return Result.CharacterNotFound(activeCharacterId)

        val (charInst, charDef) = character

        val equippedWithLevel = equippedCardsRepository.getEquippedDefinitionsWithLevelForCharacter(charInst.id)
        val equips = equippedWithLevel.map { it.definition }

        // stats base
        val stats = charDef.baseStats.toMutableMap()

        // aumenta de acordo com nivel
        try {
            val statsPerLevel = charDef.statsPerLevel
            for ((stat, value) in stats) {
                stats[stat] = value + ((statsPerLevel[stat] ?: 0.0) * (charInst.level - 1))
            }
        } catch (e: Exception) {
            println("Error calculating stats per level: ${e.message}")
            e.printStackTrace()
        }

        // soma stats dos equips
        for (equipData in equippedWithLevel) {
            val equip = equipData.definition
            val equipLevel = equipData.level

            for ((stat, baseValue) in equip.baseStats) {
                val scaledValue = baseValue + ((equip.statsPerLevel[stat] ?: 0.0) * (equipLevel - 1))
                stats[stat] = (stats[stat] ?: 0.0) + scaledValue
            }
        }

        // abilities
        val abilities = mutableListOf<Ability>()
        abilities += charDef.abilities
        equips.forEach { abilities += it.abilities }

        // tags
        val tags = mutableSetOf<String>()
        tags += charDef.tags
        equips.forEach { tags += it.tags }

        return Result.Success(
            Unit(
                id = charInst.id.toString(),
                card = charDef,
                hp = stats[Stat.HP] ?: 100.0,
                stats = stats,
                abilities = abilities,
                equipments = equips,
                tags = tags
            )
        )
    }
}
