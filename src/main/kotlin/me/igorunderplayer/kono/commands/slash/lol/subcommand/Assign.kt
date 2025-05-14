package me.igorunderplayer.kono.commands.slash.lol.subcommand

import dev.kord.common.entity.ApplicationCommandOption
import dev.kord.common.entity.ApplicationCommandOptionType
import dev.kord.common.entity.Choice
import dev.kord.common.entity.optional.Optional
import dev.kord.common.entity.optional.OptionalBoolean
import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import me.igorunderplayer.kono.commands.KonoSlashSubCommand
import me.igorunderplayer.kono.services.RiotService
import me.igorunderplayer.kono.services.UserService
import no.stelar7.api.r4j.basic.constants.api.regions.LeagueShard
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class Assign: KonoSlashSubCommand, KoinComponent {

    override val name = "assign"
    override val description = "fa"

    private val userService: UserService by inject()
    private val riotService: RiotService by inject()

    override val options = listOf(
        ApplicationCommandOption(
            name = "riot-id",
            description = "account riot-id",
            required = OptionalBoolean.Value(true),
            type = ApplicationCommandOptionType.String
        ),
        ApplicationCommandOption(
            name = "region",
            description = "account region",
            required = OptionalBoolean.Value(true),
            type = ApplicationCommandOptionType.String,
            choices = Optional(
                LeagueShard.entries.map {
                    Choice.StringChoice(
                        name = it.prettyName(), nameLocalizations = Optional(), value = it.value,
                    )
                }
            )
        ),
    )

    override suspend fun run(event: ChatInputCommandInteractionCreateEvent) {
        val response = event.interaction.deferPublicResponse()
        val riotId = event.interaction.command.strings["riot-id"]
        val region = event.interaction.command.strings["region"]

        if (riotId.isNullOrBlank()) {
            response.respond {
                content = "RiotID inválido!"
            }

            return
        }

        val split = riotId.split("#")
        val name = split.getOrNull(0)
        val tag = split.getOrNull(1)

        if (name.isNullOrBlank() || tag.isNullOrBlank()) {
            response.respond {
                content = "RiotID inválido!"
            }

            return
        }

        val leagueShard = LeagueShard.fromString(region).get()
        val account = riotService.getAccountByRiotId(leagueShard.toRegionShard(), name, tag)
        val summoner = riotService.getSummonerByPUUID(leagueShard, account?.puuid ?: "")

        if (account == null || summoner == null) {
            response.respond {
                content = "Não foi possivel encontrar a conta"
            }

            return
        }

        val user =  userService.getUserByDiscordId(event.interaction.user.id.value.toLong())

        if (user == null) {
            response.respond {
                content = "vc ainda nao foi registrado no database!!!"
            }

            return
        }

        userService.assignRiotAccountToUser(user.id, account.puuid, leagueShard)

        response.respond {
            content = "sua conta foi linkada, ebaaaa"
        }
    }
}