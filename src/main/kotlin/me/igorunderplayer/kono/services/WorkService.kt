package me.igorunderplayer.kono.services

import me.igorunderplayer.kono.data.repositories.UserRepository
import java.time.Duration
import java.time.Instant
import kotlin.random.Random

const val WORK_MIN = 40
const val WORK_MAX = 80
const val WORK_COOLDOWN = 60L // minutos

sealed class WorkResult {

    object UserNotFound : WorkResult()
    object Error : WorkResult()

    data class OnCooldown(
        val remaining: Duration
    ) : WorkResult()

    data class Success(
        val amount: Int,
        val balance: Int,
        val nextAvailable: Instant
    ) : WorkResult()
}

class WorkService(
    private val userRepository: UserRepository
) {

    suspend fun work(discordId: Long): WorkResult {
        val user = userRepository.getUserByDiscordId(discordId)
            ?: return WorkResult.UserNotFound

        val now = Instant.now()
        val lastWork = user.lastWorkAt

        // ⏱ cooldown check
        if (lastWork != null) {
            val elapsed = Duration.between(lastWork, now)
            val cooldown = Duration.ofMinutes(WORK_COOLDOWN)

            if (elapsed < cooldown) {
                return WorkResult.OnCooldown(cooldown.minus(elapsed))
            }
        }

        // 💰 recompensa aleatória
        val amount = Random.nextInt(WORK_MIN, WORK_MAX + 1)

        val newBalance = user.konos + amount

        val success = userRepository.updateWork(
            userId = user.id,
            konos = newBalance,
            workedAt = now
        )

        if (!success) return WorkResult.Error

        return WorkResult.Success(
            amount = amount,
            balance = newBalance,
            nextAvailable = now.plusSeconds(WORK_COOLDOWN * 60)
        )
    }
}
