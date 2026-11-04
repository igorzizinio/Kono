package me.igorunderplayer.kono.duel

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import me.igorunderplayer.kono.domain.card.CardCatalog
import me.igorunderplayer.kono.domain.card.CardType
import me.igorunderplayer.kono.domain.card.Rarity
import me.igorunderplayer.kono.domain.card.Stat
import me.igorunderplayer.kono.domain.card.ability.Ability
import me.igorunderplayer.kono.domain.card.ability.AbilityTarget
import me.igorunderplayer.kono.domain.card.ability.AbilityTrigger
import me.igorunderplayer.kono.domain.card.ability.AbilityType
import me.igorunderplayer.kono.domain.card.ability.Effect
import me.igorunderplayer.kono.domain.card.CardDefinition
import me.igorunderplayer.kono.domain.gameplay.DeckRules
import me.igorunderplayer.kono.domain.gameplay.DuelRulesConfig
import me.igorunderplayer.kono.engine.combat.basicDamage
import me.igorunderplayer.kono.domain.gameplay.CombatState
import me.igorunderplayer.kono.domain.gameplay.Unit
import me.igorunderplayer.kono.domain.team.TeamState

class DuelRulesTest {
    @Test
    fun `deck rules enforce size copies and legendary mythic cap`() {
        val common = CardCatalog.getById("SLIME")!!
        val mythic = CardDefinition(
            id = "TEST_MYTHIC",
            name = "Test Mythic",
            description = "",
            type = CardType.ACTION,
            rarity = Rarity.MYTHIC,
            baseStats = emptyMap(),
            abilities = emptyList()
        )
        val errors = DeckRules().validate(List(DuelRulesConfig.DECK_SIZE) { common } + listOf(mythic, mythic))
        assertTrue(errors.any { "exactly" in it })
        assertTrue(errors.any { "maximum is" in it })
        assertTrue(errors.any { "Legendary/Mythic" in it })
    }

    @Test
    fun `damage rounds up and never falls below one`() {
        assertEquals(1.0, basicDamage(1.1, 0.9))
        assertEquals(3.0, basicDamage(2.01, 0.0))
        assertEquals(1.0, basicDamage(2.0, 9.0))
    }

    @Test
    fun `state limits mulligan and hand size`() {
        val unit = Unit("u", CardCatalog.getById("SLIME")!!, 10.0, mutableMapOf(Stat.HP to 10.0), mutableListOf(), emptySet())
        val state = CombatState(
            teams = listOf(TeamState("p1", mutableListOf(unit))),
            deck = ArrayDeque((1..10).map { "CARD_$it" }),
            hand = mutableListOf("A", "B", "C", "D", "E")
        )
        state.turnOwnerTeamId = "p1"
        state.activeCharacter = unit
        assertTrue(state.mulligan(listOf("A", "B")))
        assertFalse(state.mulligan(listOf("C")))
        repeat(10) { state.hand += "X" }
        state.discardExcessHand()
        assertEquals(DuelRulesConfig.MAX_HAND_SIZE, state.hand.size)
    }
}
