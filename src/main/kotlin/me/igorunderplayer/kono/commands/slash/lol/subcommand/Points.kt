package me.igorunderplayer.kono.commands.slash.lol.subcommand

import dev.kord.common.Color
import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import dev.kord.rest.builder.interaction.SubCommandBuilder
import dev.kord.rest.builder.interaction.string
import dev.kord.rest.builder.message.embed
import me.igorunderplayer.kono.Kono
import me.igorunderplayer.kono.commands.KonoSlashSubCommand
import me.igorunderplayer.kono.utils.formatNumber
import no.stelar7.api.r4j.basic.constants.api.regions.LeagueShard

class Points(): KonoSlashSubCommand {
    override val name = "points"
    override val description = "exibe total de maestria de um jogador"


    override fun options(): SubCommandBuilder.() -> Unit {
        return {
            string("riot-id", "summoner's riot id") {
                required = true
            }

            string("region", "summoner's region") {
                for (shard in LeagueShard.entries) {
                    choice(shard.prettyName(), shard.value)
                }

                required = true
            }

            string("champion", "campeao") {
                required = true
            }
        }
    }

    override suspend fun run(event: ChatInputCommandInteractionCreateEvent) {
        val response = event.interaction.deferPublicResponse()
        val queryAccount = event.interaction.command.strings["riot-id"]
        val queryRegion = event.interaction.command.strings["region"] ?: "NA1"
        val queryChampion = event.interaction.command.strings["champion"] ?: ""

        if (queryAccount.isNullOrBlank()) {
            response.respond {
                content = "Insira o Riot ID do invocador desejado"
            }

            return
        }

        val queryName = queryAccount.split('#').first()
        var queryTag = queryAccount.split('#').getOrNull(1)

        if (queryTag.isNullOrBlank()) {
            queryTag = queryRegion
        }

        val leagueShard = LeagueShard.fromString(queryRegion).get()
        val account = Kono.riot.accountAPI.getAccountByTag(leagueShard.toRegionShard(), queryName, queryTag)
        val summoner = Kono.riot.loLAPI.summonerAPI.getSummonerByPUUID(leagueShard, account.puuid)

        val champion = Kono.riot.dDragonAPI.champions.values.find {
            it.key.lowercase() == queryChampion.lowercase() || it.name.lowercase() == queryChampion.lowercase()
        }

        if (champion == null) {
            response.respond {
                content = "campeao nao encontrei"
            }
            return
        }

        val mastery = Kono.riot.loLAPI.masteryAPI.getChampionMastery(leagueShard, account.puuid, champion.id)
        val summonerIcon = Kono.riot.dDragonAPI.profileIcons[summoner.profileIconId.toLong()]!!

        val masteryLevel = if (mastery.championLevel == 0) "default" else "${mastery.championLevel}"
        val emoji = Kono.emojis.firstOrNull { it.name == "mastery_icon_$masteryLevel" }
        val iconText = emoji?.mention ?: ""

        val latestVersion = Kono.riot.dDragonAPI.versions[0]


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
                color = Color(2895667)
            }
        }
    }
}