package me.igorunderplayer.kono.domain.team

import me.igorunderplayer.kono.data.repositories.CardInstanceRepository
import me.igorunderplayer.kono.data.repositories.UserRepository

class SetActiveCharacterHandler(
    private val userRepository: UserRepository,
    private val cardInstanceRepository: CardInstanceRepository
) {

    sealed class Result {
        data class Success(val instanceId: Int, val characterName: String) : Result()
        data class CharacterNotFound(val instanceId: Int) : Result()
        object UserNotFound : Result()
    }

    suspend fun execute(discordId: Long, instanceId: Int): Result {
        val user = userRepository.getUserByDiscordId(discordId)
            ?: return Result.UserNotFound

        val character = cardInstanceRepository.getOwnedCharacterWithDefinition(user.id, instanceId)
            ?: return Result.CharacterNotFound(instanceId)

        val (instance, definition) = character

        userRepository.updateActiveCharacter(user.id, instance.id)

        return Result.Success(instance.id, definition.name)
    }
}
