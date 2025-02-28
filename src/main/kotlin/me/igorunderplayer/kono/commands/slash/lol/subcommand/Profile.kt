package me.igorunderplayer.kono.commands.slash.lol.subcommand

import dev.kord.common.Color
import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import dev.kord.rest.builder.interaction.SubCommandBuilder
import dev.kord.rest.builder.interaction.string
import dev.kord.rest.builder.message.embed
import me.igorunderplayer.kono.Kono
import me.igorunderplayer.kono.commands.KonoSlashSubCommand
import me.igorunderplayer.kono.common.Colors
import me.igorunderplayer.kono.services.RiotService
import me.igorunderplayer.kono.services.UserService
import me.igorunderplayer.kono.utils.formatNumber
import no.stelar7.api.r4j.basic.constants.api.regions.LeagueShard
import no.stelar7.api.r4j.basic.constants.types.lol.GameQueueType
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.jvm.optionals.getOrNull

class Profile: KonoSlashSubCommand, KoinComponent {
    override val name = "profile"
    override val description = "mostra perfil de alguem"

    private val riotService: RiotService by inject()
    private val userService: UserService by inject()

    override fun options(): SubCommandBuilder.() -> Unit {
        return {
            string("riot-id", "summoner's riot id") {
                required = false
            }

            string("region", "summoner's region") {
                for (shard in LeagueShard.entries) {
                    choice(shard.prettyName(), shard.value)
                }

                required = false
            }
        }
    }

    override suspend fun run(event: ChatInputCommandInteractionCreateEvent) {
        val response = event.interaction.deferPublicResponse()
        val queryAccount = event.interaction.command.strings["riot-id"] ?: ""
        val queryRegion = event.interaction.command.strings["region"] ?: ""


        val blank = "<:transparent:1142620050952556616>"
        val champions = riotService.getChampions()


        val leagueShard = LeagueShard.fromString(queryRegion).getOrNull()


        val user = userService.getUserByDiscordId(event.interaction.user.id.value.toLong())
        val (account, summoner) = riotService.getSummonerAndAccount(user, queryAccount, leagueShard)

        if (account == null || summoner == null) {
            response.respond {
                content = "conta não encontrada! \n" +
                        "_não esqueça que para utilizar esse comando vc precisa passar seu riot-id e região, ou utilizar `/riot assign` para linkar uma conta riot ao seu discord_"
            }
            return
        }

        val summonerIcon = riotService.getProfileIcons()[summoner.profileIconId.toLong()]!!

        response.respond {
            embed {
                color = Color(Colors.RED)
                author {
                    name = "${account.name}#${account.tag} - ${summoner.platform}"
                    icon = "http://ddragon.leagueoflegends.com/cdn/${riotService.getLatestVersion()}/img/profileicon/${summonerIcon.image.full}"
                }

                field {
                    name = "Ranqueado"
                    inline = true
                    value = summoner.leagueEntry
                        .filter { it.queueType != GameQueueType.CHERRY }
                        .joinToString("\n") { leagueEntry ->
                            val type = leagueEntry.queueType.prettyName().replace("5v5", "")
                            val rank = leagueEntry.rank
                            val rankTier = leagueEntry.tier
                            val pdl = leagueEntry.leaguePoints

                            "${type}: $rankTier $rank ($pdl PDL) $blank"
                        }
                }

                field {
                    name = "Melhores campeões"
                    inline = true
                    value = summoner.championMasteries.slice(IntRange(0, 2)).joinToString("\n") { championMastery ->
                        val champion = champions[championMastery.championId]!!
                        val emoji = Kono.emojis.firstOrNull { it.name == "lolchampion_${champion.key}" }
                        val iconText = emoji?.mention ?: ""
                        "$iconText ${champion.name} - ${formatNumber(championMastery.championPoints)}"
                    }
                }
            }
        }
    }
}