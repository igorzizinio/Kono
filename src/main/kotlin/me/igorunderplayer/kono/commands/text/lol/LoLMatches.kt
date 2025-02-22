package me.igorunderplayer.kono.commands.text.lol

import dev.kord.core.behavior.reply
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.rest.builder.message.EmbedBuilder
import dev.kord.rest.builder.message.embed
import me.igorunderplayer.kono.Kono
import me.igorunderplayer.kono.commands.BaseCommand
import me.igorunderplayer.kono.commands.CommandCategory
import me.igorunderplayer.kono.services.RiotService
import no.stelar7.api.r4j.basic.constants.api.regions.LeagueShard
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class LoLMatches : BaseCommand(
    "lolmatches",
    "mostra partidas de tal user",
    category = CommandCategory.LoL
), KoinComponent {

    private val riotService: RiotService by inject()

    override suspend fun run(event: MessageCreateEvent, args: Array<String>) {

        // TODO: Embed pagination

        val riotId = args.getOrNull(0)
        val region = args.getOrNull(1)

        if (riotId.isNullOrBlank() || region.isNullOrBlank()) {
            event.message.reply {
                content = "Please insert the required information (RiotID and Region) \n" +
                        "Example: `lolmatches duduelista#BR1 br1` \n\n" +
                        "_tag is optional_"
            }

            return
        }

        val queryName =  riotId.split('#').first()
        var queryTag = riotId.split('#').getOrNull(1)


        if (queryTag.isNullOrBlank()) {
            queryTag = region
        }

        val leagueShard = LeagueShard.fromString(region).get()
        val account = riotService.getAccountByRiotId(leagueShard.toRegionShard(), queryName, queryTag)
        val summoner = riotService.getSummonerByPUUID(leagueShard, account?.puuid ?: "")

        if (summoner == null) {
            event.message.reply {
                content = "nao encontrei o dito player"
            }
            return
        }

        val summonerIcon = riotService.getProfileIcons()[summoner.profileIconId.toLong()]!!
        val matches = riotService.getMatchList(leagueShard.toRegionShard(), summoner.puuid, null, null, 0, 5, null, null)

        val embedFields = matches.map { matchId ->
            val field = EmbedBuilder.Field()
            val match = riotService.getMatch(leagueShard.toRegionShard(), matchId)
            val self = match.participants.find { it.puuid == summoner.puuid }!!

            val emoji = Kono.emojis.firstOrNull { it.name == "lolchampion_${self.championName}" }
            val csScore = self.totalMinionsKilled + self.neutralMinionsKilled

            val emojiText = emoji?.mention ?: self.championName

            field.name = if (self.didWin()) "✔ Vitória" else "❌ Derrota"
            field.value = "$emojiText | ${self.kills}/${self.assists}/${self.deaths}  - $csScore CS \n" +
                    "> ${match.gameDurationAsDuration.toMinutes()}min - `${match.gameMode.prettyName()} (${match.queue.prettyName()})`"

            field
        }.toMutableList()

        event.message.reply {
            content = "é isso ae"
            embed {
                author {
                    name = "${summoner.name} - ${summoner.platform}"
                    icon = "http://ddragon.leagueoflegends.com/cdn/${riotService.getLatestVersion()}/img/profileicon/${summonerIcon.image.full}"
                }
                fields = embedFields
            }
        }
    }
}