package me.igorunderplayer.kono.domain.team

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.igorunderplayer.kono.data.DatabaseManager
import me.igorunderplayer.kono.data.entities.CardDefinitions
import me.igorunderplayer.kono.data.entities.CardInstances
import me.igorunderplayer.kono.data.entities.Users
import me.igorunderplayer.kono.domain.card.CardType
import org.ktorm.dsl.and
import org.ktorm.dsl.eq
import org.ktorm.dsl.from
import org.ktorm.dsl.innerJoin
import org.ktorm.dsl.map
import org.ktorm.dsl.select
import org.ktorm.dsl.update
import org.ktorm.dsl.where

class SetActiveCharacterHandler(
    private val databaseManager: DatabaseManager
) {

    private val database
        get() = databaseManager.db

    sealed class Result {
        data class Success(val instanceId: Int, val characterName: String) : Result()
        data class CharacterNotFound(val instanceId: Int) : Result()
        object UserNotFound : Result()
    }

    suspend fun execute(discordId: Long, instanceId: Int): Result = withContext(Dispatchers.IO) {
        val user = database
            .from(Users)
            .select()
            .where { Users.discordId eq discordId }
            .map { Users.createEntity(it) }
            .firstOrNull()
            ?: return@withContext Result.UserNotFound

        val character = database
            .from(CardInstances)
            .innerJoin(CardDefinitions, on = CardInstances.definitionId eq CardDefinitions.id)
            .select()
            .where {
                (CardInstances.userId eq user.id) and
                        (CardInstances.id eq instanceId) and
                        (CardDefinitions.type eq CardType.CHARACTER)
            }
            .map { row ->
                val instance = CardInstances.createEntity(row)
                val definition = CardDefinitions.createEntity(row)
                Pair(instance, definition)
            }
            .firstOrNull()
            ?: return@withContext Result.CharacterNotFound(instanceId)

        val (instance, definition) = character

        database.update(Users) {
            set(it.activeCharacterInstanceId, instance.id)
            where { it.id eq user.id }
        }

        Result.Success(instance.id, definition.name)
    }
}
