package me.igorunderplayer.kono.domain.card

import me.igorunderplayer.kono.domain.card.ability.*
import me.igorunderplayer.kono.domain.gameplay.CombatEvent
import me.igorunderplayer.kono.domain.gameplay.TemporaryStatModifier
import kotlin.random.Random


// =============================================================================
// TIER RULES
// =============================================================================
// COMMON     — trash-tier. One weak passive at most. Usable only when you have nothing else.
// RARE       — usable by newcomers. Solid stats, one simple ability or a clear trade-off.
// EPIC       — the backbone of most builds. Real trade-offs, clear role, 1-3 synergistic abilities.
// LEGENDARY  — unique and impactful, but not auto-win. One noticeable weakness.
// MYTHIC     — divine or easter-egg level. Extreme stats/scaling; only 0.2% pull rate.
// KONO       — system rarity. Not obtainable via gacha. KonoBot-exclusive cards.
// =============================================================================

object CardCatalog {

    // =========================================================================
    // COMMON CHARACTERS
    // =========================================================================

    private val slime = CardDefinition(
        id = "SLIME",
        name = "Slime",
        description = "Criatura gelatinosa. Aguenta pancada, mas é fraca ofensivamente — e ainda sofre ao atacar.",
        type = CardType.CHARACTER,
        rarity = Rarity.COMMON,
        faction = "slime",
        baseStats = mapOf(
            Stat.HP to 14.0,
            Stat.ATK to 1.0,
            Stat.DEF to 0.0
        ),
        tags = setOf("slime", "starter"),
        abilities = listOf(
            Ability(
                name = "Gelatina Instável",
                description = "Ao atacar, o Slime perde massa e sofre 1 de dano.",
                type = AbilityType.PASSIVE,
                trigger = AbilityTrigger.OnAttack(),
                effects = listOf(Effect.Damage(value = 1.0, target = AbilityTarget.SELF))
            ),
            Ability(
                name = "Recompor",
                description = "Gasta 1 PA para se regenerar e curar 3 HP. 1x por turno.",
                type = AbilityType.ACTIVE,
                trigger = AbilityTrigger.Manual,
                apCost = 2,
                usesPerTurn = 1,
                effects = listOf(Effect.Heal(value = 3.0, target = AbilityTarget.SELF))
            )
        )
    )

    private val cavaleirinho = CardDefinition(
        id = "CAVALEIRINHO",
        name = "Cavaleirinho",
        description = "Um defensor simples e confiável.",
        type = CardType.CHARACTER,
        rarity = Rarity.COMMON,
        faction = "human",
        baseStats = mapOf(
            Stat.HP to 20.0,
            Stat.ATK to 3.0,
            Stat.DEF to 2.0
        ),
        tags = setOf("human", "starter", "defender"),
        abilities = listOf(
            Ability(
                name = "Postura Defensiva",
                description = "Recebe +2 DEF até o fim do turno.",
                type = AbilityType.ACTIVE,
                trigger = AbilityTrigger.Manual,
                apCost = 1,
                usesPerTurn = 1,
                effects = listOf(Effect.BuffStat(Stat.DEF, 2.0))
            )
        )
    )

    private val goblin = CardDefinition(
        id = "GOBLIN",
        name = "Goblin",
        description = "Frágil, mas agressivo e barato para começar.",
        type = CardType.CHARACTER,
        rarity = Rarity.COMMON,
        faction = "goblin",
        baseStats = mapOf(
            Stat.HP to 12.0,
            Stat.ATK to 4.0,
            Stat.DEF to 0.0
        ),
        tags = setOf("goblin", "starter", "aggressive"),
        abilities = listOf(
            Ability(
                name = "Ataque Sujo",
                description = "Causa 2 de dano adicional ao inimigo.",
                type = AbilityType.ACTIVE,
                trigger = AbilityTrigger.Manual,
                apCost = 2,
                usesPerTurn = 1,
                effects = listOf(Effect.Damage(2.0))
            )
        )
    )

    private val pocao = CardDefinition(
        id = "POCAO",
        name = "Poção",
        description = "Cura 5 HP do personagem ativo.",
        type = CardType.ACTION,
        rarity = Rarity.COMMON,
        apCost = 1,
        baseStats = emptyMap(),
        tags = setOf("action", "heal", "starter"),
        abilities = listOf(
            Ability(
                name = "Beber",
                description = "Cura 5 HP.",
                type = AbilityType.ACTIVE,
                trigger = AbilityTrigger.Manual,
                apCost = 1,
                effects = listOf(Effect.Heal(5.0))
            )
        )
    )

    private val pocaoMaior = CardDefinition(
        id = "POCAO_MAIOR",
        name = "Poção Maior",
        description = "Cura 10 HP do personagem ativo.",
        type = CardType.ACTION,
        rarity = Rarity.RARE,
        apCost = 2,
        baseStats = emptyMap(),
        tags = setOf("action", "heal"),
        abilities = listOf(
            Ability(
                name = "Beber Poção Maior",
                description = "Cura 10 HP.",
                type = AbilityType.ACTIVE,
                trigger = AbilityTrigger.Manual,
                apCost = 2,
                effects = listOf(Effect.Heal(10.0))
            )
        )
    )

    private val fogo = CardDefinition(
        id = "BOLA_DE_FOGO",
        name = "Bola de Fogo",
        description = "Causa 6 de dano mágico a um inimigo.",
        type = CardType.ACTION,
        rarity = Rarity.COMMON,
        apCost = 2,
        baseStats = emptyMap(),
        tags = setOf("action", "damage", "magic"),
        abilities = listOf(
            Ability(
                name = "Explosão",
                description = "Causa 6 de dano mágico.",
                type = AbilityType.ACTIVE,
                trigger = AbilityTrigger.Manual,
                apCost = 2,
                effects = listOf(Effect.Damage(6.0, damageType = DamageType.MAGIC))
            )
        )
    )

    private val energia = CardDefinition(
        id = "ENERGIA_EXTRA",
        name = "Energia Extra",
        description = "Gera 1 PA imediatamente.",
        type = CardType.ACTION,
        rarity = Rarity.RARE,
        apCost = 0,
        baseStats = emptyMap(),
        tags = setOf("action", "resource"),
        abilities = listOf(
            Ability(
                name = "Recuperar Energia",
                description = "Ganha 1 PA.",
                type = AbilityType.ACTIVE,
                trigger = AbilityTrigger.Manual,
                apCost = 0,
                effects = listOf(Effect.GainAp(1))
            )
        )
    )

    private val curaRapida = action(
                "CURA_RAPIDA", "Cura Rápida", "Cura 3 HP do personagem ativo.", Rarity.COMMON, 1,
                Effect.Heal(3.0)
            )
    private val raio = action(
                "RAIO", "Raio", "Causa 4 de dano mágico a um inimigo.", Rarity.COMMON, 1,
                Effect.Damage(4.0, damageType = DamageType.MAGIC)
            )
    private val ondaDeChoque = action(
                "ONDA_DE_CHOQUE", "Onda de Choque", "Causa 2 de dano a todos os inimigos.", Rarity.RARE, 2,
                Effect.Damage(2.0, target = AbilityTarget.ALL_ENEMIES)
            )
    private val compraExtra = action(
                "COMPRA_EXTRA", "Compra Extra", "Compra duas cartas.", Rarity.RARE, 1,
                Effect.DrawCards(2)
            )
    private val limparMal = action(
                "LIMPAR_MAL", "Limpar Mal", "Remove os buffs temporários do personagem ativo.", Rarity.RARE, 1,
                Effect.RemoveBuff
            )
    private val trocaGratuita = action(
                "TROCA_GRATUITA", "Troca Gratuita", "A próxima troca não custa AP.", Rarity.COMMON, 0,
                Effect.FreeSwap
            )
    private val barreira = action(
                "BARREIRA", "Barreira", "Ganha +2 DEF por uma rodada.", Rarity.RARE, 1,
                Effect.BuffStat(Stat.DEF, 2.0, durationRounds = 1)
            )
    private val golpePesado = action(
                "GOLPE_PESADO", "Golpe Pesado", "Causa 5 de dano físico.", Rarity.EPIC, 2,
                Effect.Damage(5.0)
            )
    private val drenagem = action(
                "DRENAGEM", "Drenagem", "Causa 3 de dano e cura 3 HP.", Rarity.EPIC, 2,
                Effect.Damage(3.0), Effect.Heal(3.0)
            )
    private val inspiracao = action(
                "INSPIRACAO", "Inspiração", "Ganha 1 AP e compra uma carta.", Rarity.EPIC, 1,
                Effect.GainAp(1), Effect.DrawCards(1)

    )


    // =========================================================================
    // CATALOG
    // =========================================================================

    val all: List<CardDefinition> = listOf(
        slime,
        cavaleirinho,
        goblin,
        pocao,
        pocaoMaior,
        fogo,
        energia,
        curaRapida,
        raio,
        ondaDeChoque,
        compraExtra,
        limparMal,
        trocaGratuita,
        barreira,
        golpePesado,
        drenagem,
        inspiracao
    )

    private fun action(
        id: String,
        name: String,
        description: String,
        rarity: Rarity,
        apCost: Int,
        vararg effects: Effect
    ) = CardDefinition(
        id = id,
        name = name,
        description = description,
        type = CardType.ACTION,
        rarity = rarity,
        apCost = apCost,
        baseStats = emptyMap(),
        tags = setOf("action"),
        abilities = listOf(
            Ability(
                name = name,
                description = description,
                type = AbilityType.ACTIVE,
                trigger = AbilityTrigger.Manual,
                apCost = 0,
                effects = effects.toList()
            )
        )
    )

    private val byId = all.associateBy { it.id }

    fun getById(id: String): CardDefinition? = byId[id.uppercase()]
}
