package me.igorunderplayer.kono.domain.team

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.igorunderplayer.kono.data.DatabaseManager
import me.igorunderplayer.kono.data.entities.CardDefinitions
import me.igorunderplayer.kono.data.entities.CardInstances
import me.igorunderplayer.kono.data.entities.EquippedCards
import me.igorunderplayer.kono.data.entities.Users
import me.igorunderplayer.kono.domain.card.Stat
import me.igorunderplayer.kono.domain.card.ability.Ability
import me.igorunderplayer.kono.domain.gameplay.Unit
import org.ktorm.dsl.eq
import org.ktorm.dsl.from
import org.ktorm.dsl.innerJoin
import org.ktorm.dsl.map
import org.ktorm.dsl.select
import org.ktorm.dsl.where

class BuildUnitHandler(
    private val databaseManager: DatabaseManager
) {

    private val database
        get() = databaseManager.db

    suspend fun executeByDiscordId(userId: Long): Unit = withContext(Dispatchers.IO) {
        val character = database
            .from(Users)
            .innerJoin(CardInstances, on = Users.activeCharacterInstanceId eq CardInstances.id)
            .innerJoin(CardDefinitions, on = CardInstances.definitionId eq CardDefinitions.id)
            .select()
            .where { Users.discordId eq userId }
            .map { row ->

                val def = CardDefinitions.createEntity(row)
                val inst = CardInstances.createEntity(row)

                Pair(def, inst)
            }
            .firstOrNull()
            ?: error("Nenhum personagem ativo")

        val (charDef, charInst) = character

        // equipamentos
        val equips = database
            .from(EquippedCards)
            .innerJoin(CardInstances, on = EquippedCards.cardInstanceId eq CardInstances.id)
            .innerJoin(CardDefinitions, on = CardInstances.definitionId eq CardDefinitions.id)
            .select()
            .where { EquippedCards.characterInstanceId eq charInst.id }
            .map { row ->
                Pair(
                    CardDefinitions.createEntity(row),
                    row[EquippedCards.slot] ?: Int.MAX_VALUE
                )
            }
            .sortedBy { it.second }
            .map { it.first }

        // stats base
        val stats = charDef.baseStats.toMutableMap()

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

        Unit(
            id = charInst.id.toString(),
            card = charDef,
            hp = stats[Stat.HP] ?: 100.0,
            stats = stats,
            abilities = abilities,
            tags = tags
        )
    }
}
