package me.igorunderplayer.kono.utils.interaction

import dev.kord.core.Kord
import dev.kord.core.event.interaction.SelectMenuInteractionCreateEvent
import dev.kord.core.on
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

suspend fun Kord.awaitStringSelectInteraction(
    customId: String,
    allowedUserId: Long,
    timeout: Duration = 60.seconds
): SelectMenuInteractionCreateEvent? {
    val selected = CompletableDeferred<SelectMenuInteractionCreateEvent>()

    val listener = on<SelectMenuInteractionCreateEvent> {
        val interaction = this.interaction

        if (interaction.component.customId != customId) return@on
        if (interaction.user.id.value.toLong() != allowedUserId) return@on

        selected.complete(this)
    }

    return try {
        withTimeoutOrNull(timeout) {
            selected.await()
        }
    } finally {
        listener.cancel()
    }
}

