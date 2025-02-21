package me.igorunderplayer.kono.services

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import no.stelar7.api.r4j.basic.APICredentials
import no.stelar7.api.r4j.basic.constants.api.regions.LeagueShard
import no.stelar7.api.r4j.basic.constants.api.regions.RegionShard
import no.stelar7.api.r4j.impl.R4J
import no.stelar7.api.r4j.pojo.lol.championmastery.ChampionMastery
import no.stelar7.api.r4j.pojo.lol.staticdata.champion.StaticChampion
import no.stelar7.api.r4j.pojo.lol.summoner.Summoner
import no.stelar7.api.r4j.pojo.shared.RiotAccount

class RiotService(apiKey: String) {
    private val riot = R4J(APICredentials(apiKey))

    suspend fun getAccountByRiotId(regionShard: RegionShard, name: String, tag: String): RiotAccount? = withContext(Dispatchers.IO) {
        riot.accountAPI.getAccountByTag(regionShard, name, tag)
    }

    suspend fun getSummonerByPUUID(leagueShard: LeagueShard, puuid: String): Summoner? = withContext(Dispatchers.IO) {
        riot.loLAPI.summonerAPI.getSummonerByPUUID(leagueShard, puuid)
    }

    suspend fun getChampionMastery(leagueShard: LeagueShard, puuid: String, championId: Int): ChampionMastery = withContext(Dispatchers.IO) {
        riot.loLAPI.masteryAPI.getChampionMastery(leagueShard, puuid, championId)
    }

    suspend fun getChampions(): Map<Int, StaticChampion> = withContext(Dispatchers.IO) {
        riot.dDragonAPI.champions
    }
}