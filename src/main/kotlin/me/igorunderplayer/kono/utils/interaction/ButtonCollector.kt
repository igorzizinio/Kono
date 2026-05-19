package me.igorunderplayer.kono.utils.interaction

import dev.kord.core.Kord
import dev.kord.core.event.interaction.ButtonInteractionCreateEvent
import dev.kord.core.on
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

@Suppress("unused")
suspend fun Kord.awaitButtonInteraction(
    customId: String,
    allowedUserId: Long,
    timeout: Duration = 60.seconds
): ButtonInteractionCreateEvent? {
    val pressed = CompletableDeferred<ButtonInteractionCreateEvent>()

    val listener = on<ButtonInteractionCreateEvent> {
        val interaction = this.interaction

        if (interaction.component.customId != customId) return@on
        if (interaction.user.id.value.toLong() != allowedUserId) return@on

        pressed.complete(this)
    }

    return try {
        withTimeoutOrNull(timeout) {
            pressed.await()
        }
    } finally {
        listener.cancel()
    }
}

suspend fun Kord.awaitFirstButtonInteraction(
    ids: Collection<String>,
    allowedUserId: Long,
    timeout: Duration = 60.seconds
): Pair<String, ButtonInteractionCreateEvent>? {
    val result = CompletableDeferred<Pair<String, ButtonInteractionCreateEvent>>()

    val listener = on<ButtonInteractionCreateEvent> {
        val customId = interaction.component.customId ?: return@on
        if (customId !in ids) return@on
        if (interaction.user.id.value.toLong() != allowedUserId) return@on
        if (!result.isCompleted) result.complete(customId to this)
    }

    return try {
        withTimeoutOrNull(timeout) { result.await() }
    } finally {
        listener.cancel()
    }
}


