package me.igorunderplayer.kono.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class CardSummaryDTO(
    val id: String,
    val name: String,
    val description: String,
    val type: String,
    val rarity: String,
    val attackCost: Int,
    val apCost: Int,
    val baseStats: Map<String, Double>,
    val tags: List<String>
)

@Serializable
data class CreateMatchRequest(
    val playerOneId: String? = null,
    val playerTwoId: String? = null,
    val playerOneCards: List<String> = listOf("SLIME", "CAVALEIRINHO", "GOBLIN"),
    val playerTwoCards: List<String> = listOf("SLIME", "CAVALEIRINHO", "GOBLIN")
)

@Serializable
data class MatchSummaryDTO(
    val id: String,
    val status: String,
    val turn: Int,
    val playerOneUnitIds: List<String>,
    val playerTwoUnitIds: List<String>,
    val playerOneHp: Map<String, Double>,
    val playerTwoHp: Map<String, Double>,
    val ap: Int,
    val hand: List<String>,
    val discard: List<String>,
    val log: List<String>
)

@Serializable
data class MatchCreatedDTO(
    val match: MatchSummaryDTO
)

@Serializable
data class MatchActionRequest(
    val playerId: String? = null,
    val action: String,
    val targetId: String? = null,
    val cardId: String? = null,
    val abilityName: String? = null
)

@Serializable
data class MatchActionResponse(
    val accepted: Boolean,
    val error: String? = null,
    val match: MatchSummaryDTO
)
