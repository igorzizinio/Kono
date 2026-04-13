package me.igorunderplayer.kono.services

import me.igorunderplayer.kono.data.repositories.UserRepository
import java.time.Duration
import java.time.ZoneId
import java.time.ZonedDateTime

const val DAILY_AMOUNT = 50
const val WEEKLY_BONUS = 180

sealed class DailyResult {

    object UserNotFound : DailyResult()
    object Error : DailyResult()

    data class AlreadyClaimed(
        val nextReset: ZonedDateTime
    ) : DailyResult()

    data class Success(
        val reward: Int,
        val streak: Int,
        val balance: Int,
        val bonusApplied: Boolean,
        val nextReset: ZonedDateTime
    ) : DailyResult()
}

class DailyService(
    private val userRepository: UserRepository
) {

    private val RESET_HOUR = 3
    private val ZONE = ZoneId.of("UTC")

    suspend fun claimDaily(discordId: Long): DailyResult {
        val user = userRepository.getUserByDiscordId(discordId)
            ?: return DailyResult.UserNotFound

        val now = ZonedDateTime.now(ZONE)

        val currentReset = getCurrentReset(now)
        val lastClaim = user.dailyRewardClaimedAt?.atZone(ZONE)

        // já pegou nesse ciclo
        if (lastClaim != null && lastClaim.isAfter(currentReset)) {
            return DailyResult.AlreadyClaimed(currentReset.plusDays(1))
        }

        val newStreak = calculateStreak(lastClaim, currentReset, user.dailyStreak)
        val (reward, bonusApplied) = calculateReward(newStreak)

        val newEssence = user.essence + reward

        val success = userRepository.updateDaily(
            userId = user.id,
            money = newEssence,
            streak = newStreak,
            claimedAt = now.toInstant(),
            currentReset = currentReset.toInstant()
        )

        if (!success) return DailyResult.Error

        return DailyResult.Success(
            reward = reward,
            streak = newStreak,
            balance = newEssence    ,
            bonusApplied = bonusApplied,
            nextReset = currentReset.plusDays(1)
        )
    }

    private fun getCurrentReset(now: ZonedDateTime): ZonedDateTime {
        val todayReset = now
            .withHour(RESET_HOUR)
            .withMinute(0)
            .withSecond(0)
            .withNano(0)

        return if (now.isBefore(todayReset)) {
            todayReset.minusDays(1)
        } else {
            todayReset
        }
    }

    private fun calculateStreak(
        lastClaim: ZonedDateTime?,
        currentReset: ZonedDateTime,
        currentStreak: Int
    ): Int {
        if (lastClaim == null) return 1

        val lastReset = getCurrentReset(lastClaim)
        val daysBetween = Duration.between(lastReset, currentReset).toDays()

        val nextStreak = when {
            daysBetween <= 1L -> currentStreak + 1
            daysBetween == 2L -> currentStreak + 1 // tolerância de 1 dia
            else -> 1
        }

        return if (nextStreak > 7) 1 else nextStreak
    }

    private fun calculateReward(streak: Int): Pair<Int, Boolean> {
        var reward = DAILY_AMOUNT
        val isWeekly = streak == 7

        if (isWeekly) {
            reward += WEEKLY_BONUS
        }

        return reward to isWeekly
    }
}
