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
import me.igorunderplayer.kono.utils.formatNumber
import no.stelar7.api.r4j.basic.constants.api.regions.LeagueShard
import no.stelar7.api.r4j.basic.constants.types.lol.GameQueueType

class Profile: KonoSlashSubCommand {
    override val name = "profile"
    override val description = "mostra perfil de alguem"

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
        }
    }

    override suspend fun run(event: ChatInputCommandInteractionCreateEvent) {
        val response = event.interaction.deferPublicResponse()
        val queryAccount = event.interaction.command.strings["riot-id"]
        val queryRegion = event.interaction.command.strings["region"] ?: "NA1"

        if (queryAccount.isNullOrBlank()) {
            response.respond {
                content = "Insira o Riot ID do invocador desejado"
            }

            return
        }

        val blank = "<:transparent:1142620050952556616>"
        val champions = Kono.riot.dDragonAPI.champions

        val queryName = queryAccount.split('#').first()
        var queryTag = queryAccount.split('#').getOrNull(1)

        if (queryTag.isNullOrBlank()) {
            queryTag = queryRegion
        }

        val leagueShard = LeagueShard.fromString(queryRegion).get()
        val account = Kono.riot.accountAPI.getAccountByTag(leagueShard.toRegionShard(), queryName, queryTag)
        val summoner = Kono.riot.loLAPI.summonerAPI.getSummonerByPUUID(leagueShard, account.puuid)

        if (summoner == null) {
            response.respond {
                content = "Invocador não encontrado!"
            }

            return
        }

        val summonerIcon = Kono.riot.dDragonAPI.profileIcons[summoner.profileIconId.toLong()]!!

        response.respond {
            embed {
                color = Color(Colors.RED)
                author {
                    name = "${account.name}#${account.tag} - ${summoner.platform}"
                    icon = "http://ddragon.leagueoflegends.com/cdn/${Kono.riot.dDragonAPI.versions[0]}/img/profileicon/${summonerIcon.image.full}"
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