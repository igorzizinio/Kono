package me.igorunderplayer.kono.commands.slash.lol.subcommand

import dev.kord.common.Color
import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import dev.kord.rest.builder.interaction.SubCommandBuilder
import dev.kord.rest.builder.interaction.string
import dev.kord.rest.builder.message.embed
import me.igorunderplayer.kono.Kono
import me.igorunderplayer.kono.commands.KonoSlashSubCommand
import me.igorunderplayer.kono.services.RiotService
import me.igorunderplayer.kono.services.UserService
import me.igorunderplayer.kono.utils.formatNumber
import no.stelar7.api.r4j.basic.constants.api.regions.LeagueShard
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class Points(): KonoSlashSubCommand, KoinComponent {
    override val name = "points"
    override val description = "exibe total de maestria de um jogador"

    private val riotService: RiotService by inject()
    private val userService: UserService by inject()

    override fun options(): SubCommandBuilder.() -> Unit {
        return {
            string("champion", "campeao") {
                required = true
            }

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
        val queryChampion = event.interaction.command.strings["champion"] ?: ""

        val leagueShard = LeagueShard.fromString(queryRegion).get()

        val user = userService.getUserByDiscordId(event.interaction.user.id.value.toLong())
        val (account, summoner) = riotService.getSummonerAndAccount(user, queryAccount, leagueShard)

        if (account == null || summoner == null) {
            response.respond {
                content = "conta não encontrada!"
            }
            return
        }

        val champion = riotService.getChampions().values.find {
            it.key.lowercase() == queryChampion.lowercase() || it.name.lowercase() == queryChampion.lowercase()
        }

        if (champion == null) {
            response.respond {
                content = "campeao nao encontrei"
            }
            return
        }

        val mastery = riotService.getChampionMastery(summoner.platform, account.puuid, champion.id)
        val summonerIcon = riotService.getProfileIcons()[summoner.profileIconId.toLong()]!!

        val masteryLevelI = if (mastery.championLevel > 7) 7 else mastery.championLevel
        val masteryLevel = if (masteryLevelI == 0) "default" else "$masteryLevelI"
        val emoji = Kono.emojis.firstOrNull { it.name == "mastery_icon_$masteryLevel" }
        val iconText = emoji?.mention ?: ""

        val latestVersion = riotService.getLatestVersion()

        response.respond {
            embed {
                author {
                    name = "${account.name} ${account.tag}"
                    icon = "http://ddragon.leagueoflegends.com/cdn/${latestVersion}/img/profileicon/${summonerIcon.image.full}"
                }

                thumbnail {
                    url = "http://ddragon.leagueoflegends.com/cdn/$latestVersion/img/champion/${champion.image.full}"
                }

                description = "$iconText ${formatNumber(mastery.championPoints)} pontos de maestria"
                color = getLevelColor(masteryLevelI)
            }
        }
    }


    private fun getLevelColor(level: Int): Color {
        return when (level) {
            5 -> Color(0xC28F2C)
            6 -> Color(0xB07FFF)
            7 -> Color(0x00C0FF)
            else -> Color(2895667)
        }
    }

}