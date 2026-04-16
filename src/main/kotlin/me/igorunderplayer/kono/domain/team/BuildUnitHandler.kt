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

    suspend fun executeByDiscordId(userId: Long): Unit {
        val userRow = userRepository.getUserByDiscordId(userId)
            ?: error("Usuário não encontrado")

        val activeCharacterId = userRow.activeCharacterInstanceId
            ?: error("Nenhum personagem ativo selecionado. Use: setactive <instance_id>")

        val character = cardInstanceRepository.getOwnedCharacterWithDefinition(userRow.id, activeCharacterId)
            ?: error("Personagem ativo (#$activeCharacterId) não encontrado no banco de dados")

        val (charInst, charDef) = character

        val equips = equippedCardsRepository.getEquippedDefinitionsForCharacter(charInst.id)

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
        for (equip in equips) {
            for ((stat, value) in equip.baseStats) {
                stats[stat] = (stats[stat] ?: 0.0) + value
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

        return Unit(
            id = charInst.id.toString(),
            card = charDef,
            hp = stats[Stat.HP] ?: 100.0,
            stats = stats,
            abilities = abilities,
            tags = tags
        )
    }
}
