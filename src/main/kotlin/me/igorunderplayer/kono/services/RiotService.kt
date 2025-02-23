package me.igorunderplayer.kono.services

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.igorunderplayer.kono.data.entities.User
import no.stelar7.api.r4j.basic.APICredentials
import no.stelar7.api.r4j.basic.constants.api.regions.LeagueShard
import no.stelar7.api.r4j.basic.constants.api.regions.RegionShard
import no.stelar7.api.r4j.basic.constants.types.lol.GameQueueType
import no.stelar7.api.r4j.basic.constants.types.lol.MatchlistMatchType
import no.stelar7.api.r4j.impl.R4J
import no.stelar7.api.r4j.pojo.lol.championmastery.ChampionMastery
import no.stelar7.api.r4j.pojo.lol.match.v5.LOLMatch
import no.stelar7.api.r4j.pojo.lol.staticdata.champion.StaticChampion
import no.stelar7.api.r4j.pojo.lol.staticdata.profileicon.ProfileIconDetails
import no.stelar7.api.r4j.pojo.lol.summoner.Summoner
import no.stelar7.api.r4j.pojo.shared.RiotAccount

class RiotService(apiKey: String) {
    private val riot = R4J(APICredentials(apiKey))


    suspend fun getAccountByPUUID(region: RegionShard, puuid: String): RiotAccount? = withContext(Dispatchers.IO) {
        riot.accountAPI.getAccountByPUUID(region, puuid)
    }

    suspend fun getAccountByRiotId(regionShard: RegionShard, name: String, tag: String): RiotAccount? = withContext(Dispatchers.IO) {
        riot.accountAPI.getAccountByTag(regionShard, name, tag)
    }

    suspend fun getSummonerByPUUID(leagueShard: LeagueShard, puuid: String): Summoner? = withContext(Dispatchers.IO) {
        riot.loLAPI.summonerAPI.getSummonerByPUUID(leagueShard, puuid)
    }

    suspend fun getChampionMastery(leagueShard: LeagueShard, puuid: String, championId: Int): ChampionMastery = withContext(Dispatchers.IO) {
        riot.loLAPI.masteryAPI.getChampionMastery(leagueShard, puuid, championId)
    }


    suspend fun getMatchList(region: RegionShard, puuid: String, queue: GameQueueType?,type: MatchlistMatchType?, beginIndex: Int, count: Int, startTime: Long?, endTime: Long?): List<String> = withContext(Dispatchers.IO) {
        riot.loLAPI.matchAPI.getMatchList(region, puuid, queue, type, beginIndex, count, startTime, endTime)
    }

    suspend fun getMatch(region: RegionShard, matchId: String): LOLMatch? = withContext(Dispatchers.IO) {
        riot.loLAPI.matchAPI.getMatch(region, matchId)
    }

    suspend fun getChampions(): Map<Int, StaticChampion> = withContext(Dispatchers.IO) {
        riot.dDragonAPI.champions
    }

    suspend fun getChampions(version: String, locale: String): Map<Int, StaticChampion> = withContext(Dispatchers.IO) {
        riot.dDragonAPI.getChampions(version, locale)
    }

    suspend fun getProfileIcons(): Map<Long, ProfileIconDetails> = withContext(Dispatchers.IO) {
        riot.dDragonAPI.profileIcons
    }

    suspend fun getLatestVersion(): String = withContext(Dispatchers.IO) {
        getVersions().first()
    }


    suspend fun getVersions(): List<String> = withContext(Dispatchers.IO) {
        riot.dDragonAPI.versions
    }

    suspend fun getSummonerAndAccount(
        user: User?,
        queryAccount: String,
        leagueShard: LeagueShard?
    ): Pair<RiotAccount?, Summoner?> {
        var account: RiotAccount? = null
        var summoner: Summoner? = null

        if (queryAccount.isBlank()) {
            val shard = user?.riotRegion?.let { LeagueShard.fromString(it).get() }
            val puuid = user?.riotPuuid

            if (shard != null && !puuid.isNullOrBlank()) {
                account = getAccountByPUUID(shard.toRegionShard(), puuid)
                summoner = getSummonerByPUUID(shard, puuid)
            }
        } else {
            val split = queryAccount.split("#")
            val queryName = split.getOrNull(0)
            val queryTag = split.getOrNull(1)
            if (leagueShard == null || queryName == null || queryTag == null) return Pair(null, null)

            account = getAccountByRiotId(leagueShard.toRegionShard(), queryName, queryTag)
            val puuid = account?.puuid

            if (!puuid.isNullOrBlank()) {
                summoner = getSummonerByPUUID(leagueShard, puuid)
            }
        }

        return Pair(account, summoner)
    }
}