package me.igorunderplayer.kono.commands.text

import CardRepository
import dev.kord.core.event.message.MessageCreateEvent
import me.igorunderplayer.kono.commands.BaseCommand
import me.igorunderplayer.kono.data.repositories.CardInstanceRepository
import me.igorunderplayer.kono.data.repositories.UserRepository

class PullCommand(
    private val userRepository: UserRepository,
    private val cardRepository: CardRepository,
    private val cardInstanceRepository: CardInstanceRepository
) : BaseCommand(
    name = "pull",
    description = "Pull a random card using essence!",
) {

    private val COST = 1

    override suspend fun run(event: MessageCreateEvent, args: Array<String>) {
        val discordId = event.message.author?.id?.value?.toLong() ?: return

        val user = userRepository.getUserByDiscordId(discordId)

        if (user == null) {
            event.message.channel.createMessage("Você precisa se registrar primeiro.")
            return
        }

        if (user.essence < COST) {
            event.message.channel.createMessage("Você não tem essence suficiente 💎")
            return
        }

        val pool = cardRepository.getAll()

        if (pool.isEmpty()) {
            event.message.channel.createMessage("Nenhuma carta disponível.")
            return
        }

        val card = pool.random()

        val success = cardInstanceRepository.insert(
            userId = user.id,
            definitionId = card.id
        )

        if (!success) {
            event.message.channel.createMessage("Erro ao obter carta.")
            return
        }

        // desconta essence
        userRepository.updateEssence(user.id, user.essence - COST)

        event.message.channel.createMessage(
            "🎰 Você puxou: **${card.name}** (${card.rarity})"
        )
    }
}
