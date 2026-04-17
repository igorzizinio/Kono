package me.igorunderplayer.kono.domain.card

import me.igorunderplayer.kono.domain.card.ability.Ability
import me.igorunderplayer.kono.domain.card.ability.AbilityTarget
import me.igorunderplayer.kono.domain.card.ability.AbilityTrigger
import me.igorunderplayer.kono.domain.card.ability.AbilityType
import me.igorunderplayer.kono.domain.card.ability.Effect

object CardCatalog {

    private val defaultStatsPerLevel = mapOf(
        Stat.HP to 5.0,
        Stat.ATK to 2.5,
        Stat.DEF to 2.0
    )

    val all: List<CardDefinition> = listOf(
        CardDefinition(
            id = "SLIME",
            name = "Slime",
            description = "Uma criatura gelatinosa basica. Muita durabilidade, porem pouco dano.",
            type = CardType.CHARACTER,
            rarity = Rarity.COMMON,
            faction = "slime",
            baseStats = mapOf(
                Stat.HP to 580.0,
                Stat.ATK to 38.0,
                Stat.DEF to 8.0,
                Stat.CRIT_CHANCE to 0.05,
                Stat.CRIT_DAMAGE to 1.25,
                Stat.SPEED to 80.0
            ),
            statsPerLevel = defaultStatsPerLevel,
            tags = setOf("slime", "starter"),
            abilities = listOf(
                Ability(
                    name = "Gelatina Instavel",
                    type = AbilityType.PASSIVE,
                    trigger = AbilityTrigger.OnAttack,
                    effects = listOf(
                        Effect.Damage(value = 12.0, target = AbilityTarget.SELF)
                    )
                )
            )
        ),
        CardDefinition(
            id = "IRON_ARMOR",
            name = "Iron Armor",
            description = "Armadura que reduz dano recebido.",
            type = CardType.EQUIPMENT,
            rarity = Rarity.COMMON,
            baseStats = mapOf(
                Stat.HP to 180.0,
                Stat.DEF to 15.0,
                Stat.SPEED to -5.0
            ),
            statsPerLevel = defaultStatsPerLevel,
            tags = setOf("armor", "defense"),
            abilities = emptyList()
        ),
        CardDefinition(
            id = "HEAVY_IRON_ARMOR",
            name = "Heavy Iron Armor",
            description = "Armadura pesada que reduz dano recebido.",
            type = CardType.EQUIPMENT,
            rarity = Rarity.RARE,
            baseStats = mapOf(
                Stat.HP to 110.0,
                Stat.DEF to 60.0,
                Stat.SPEED to -20.0
            ),
            statsPerLevel = defaultStatsPerLevel,
            tags = setOf("heavy", "armor", "defense"),
            abilities = emptyList()
        ),
        CardDefinition(
            id = "GAMBLER_CHARM",
            name = "Gambler Charm",
            description = "Um artefato caotico.",
            type = CardType.EQUIPMENT,
            rarity = Rarity.EPIC,
            baseStats = emptyMap(),
            statsPerLevel = defaultStatsPerLevel,
            tags = setOf("gambler", "chaos"),
            abilities = listOf(
                Ability(
                    name = "Roleta Caotica",
                    type = AbilityType.PASSIVE,
                    trigger = AbilityTrigger.OnTurnStart,
                    effects = listOf(Effect.Random(profile = "DEFAULT"))
                )
            )
        ),
        CardDefinition(
            id = "VAMPIRE_CORE",
            name = "Nucleo Vampirico",
            description = "Rouba vida ao atacar.",
            type = CardType.EQUIPMENT,
            rarity = Rarity.EPIC,
            faction = "vampire",
            baseStats = emptyMap(),
            statsPerLevel = defaultStatsPerLevel,
            tags = setOf("vampire", "lifesteal"),
            abilities = listOf(
                Ability(
                    name = "Sifao de Sangue",
                    type = AbilityType.PASSIVE,
                    trigger = AbilityTrigger.OnDamageDealt,
                    effects = listOf(Effect.Heal(value = 10.0, target = AbilityTarget.SELF))
                )
            )
        ),
        CardDefinition(
            id = "CRITFISH",
            name = "Critfish",
            description = "Item raro focado em critico.",
            type = CardType.EQUIPMENT,
            rarity = Rarity.LEGENDARY,
            baseStats = mapOf(
                Stat.CRIT_CHANCE to 0.05,
                Stat.CRIT_DAMAGE to 3.0
            ),
            statsPerLevel = defaultStatsPerLevel,
            tags = setOf("critfish", "critical"),
            abilities = emptyList()
        ),
        CardDefinition(
            id = "UNDEFINED",
            name = "undefined",
            description = "error: undefined is not an item",
            type = CardType.EQUIPMENT,
            rarity = Rarity.MYTHIC,
            baseStats = emptyMap(),
            statsPerLevel = defaultStatsPerLevel,
            tags = setOf("bug", "chaos"),
            abilities = listOf(
                Ability(
                    name = "Undefined Bug",
                    type = AbilityType.PASSIVE,
                    trigger = AbilityTrigger.OnTurnStart,
                    effects = listOf(Effect.Random(profile = "UNDEFINED_BUG"))
                )
            )
        ),
        CardDefinition(
            id = "JUNIOR_KNIGHT",
            name = "Cavaleirinho",
            description = "Um jovem aprendiz com armadura e espada de madeira.",
            type = CardType.CHARACTER,
            rarity = Rarity.COMMON,
            baseStats = mapOf(
                Stat.HP to 480.0,
                Stat.ATK to 32.0,
                Stat.DEF to 40.0,
                Stat.CRIT_CHANCE to 0.05,
                Stat.CRIT_DAMAGE to 1.25,
                Stat.SPEED to 70.0
            ),
            statsPerLevel = defaultStatsPerLevel,
            tags = setOf("starter"),
            abilities = emptyList()
        ),
        CardDefinition(
            id = "THIEF",
            name = "Bandido",
            description = "Agil e oportunista. Causa dano adicional ao acertar.",
            type = CardType.CHARACTER,
            rarity = Rarity.RARE,
            baseStats = mapOf(
                Stat.HP to 420.0,
                Stat.ATK to 36.0,
                Stat.DEF to 14.0,
                Stat.CRIT_CHANCE to 0.10,
                Stat.CRIT_DAMAGE to 1.4,
                Stat.SPEED to 115.0
            ),
            statsPerLevel = defaultStatsPerLevel,
            tags = setOf("starter"),
            abilities = listOf(
                Ability(
                    name = "Injusto",
                    type = AbilityType.PASSIVE,
                    trigger = AbilityTrigger.OnAttack,
                    effects = listOf(
                        Effect.Damage(value = 10.0, target = AbilityTarget.ENEMY)
                    )
                )
            )
        ),
        CardDefinition(
            id = "MARKUS",
            name = "Markus, Mestre das Apostas",
            description = "Cada turno vira uma aposta. Risco alto, recompensa alta.",
            type = CardType.CHARACTER,
            rarity = Rarity.LEGENDARY,
            faction = "gambler",
            baseStats = mapOf(
                Stat.HP to 720.0,
                Stat.ATK to 58.0,
                Stat.DEF to 16.0,
                Stat.CRIT_CHANCE to 0.15,
                Stat.CRIT_DAMAGE to 1.5,
                Stat.SPEED to 110.0
            ),
            statsPerLevel = defaultStatsPerLevel,
            tags = setOf("rng", "gambler", "risk", "chaos", "scaling", "boss"),
            abilities = listOf(
                Ability(
                    name = "Mestre da Mesa",
                    type = AbilityType.PASSIVE,
                    trigger = AbilityTrigger.OnTurnStart,
                    effects = listOf(
                        Effect.AddCoins(value = 2),
                        Effect.Random(profile = "MARKUS_GAMBLER")
                    )
                )
            )
        ),
        CardDefinition(
            id = "VEYN",
            name = "Veyn",
            description = "Besteiro de Markus. Quanto mais ritmo, maior a pressao.",
            type = CardType.CHARACTER,
            rarity = Rarity.EPIC,
            faction = "gambler",
            baseStats = mapOf(
                Stat.HP to 490.0,
                Stat.ATK to 42.0,
                Stat.DEF to 16.0,
                Stat.CRIT_CHANCE to 0.15,
                Stat.CRIT_DAMAGE to 1.25,
                Stat.SPEED to 120.0
            ),
            statsPerLevel = mapOf(
                Stat.HP to 2.5,
                Stat.ATK to 2.0,
                Stat.DEF to 1.4,
                Stat.CRIT_CHANCE to 0.01,
                Stat.SPEED to 1.5
            ),
            tags = setOf("rng", "gambler", "speed", "archer", "marksman", "risk"),
            abilities = listOf(
                Ability(
                    name = "Rajada Ritmica",
                    type = AbilityType.PASSIVE,
                    trigger = AbilityTrigger.OnAttack,
                    effects = listOf(
                        Effect.Damage(value = 8.0, target = AbilityTarget.ENEMY)
                    )
                ),
                Ability(
                    name = "Ritmo de Cassino",
                    type = AbilityType.PASSIVE,
                    trigger = AbilityTrigger.OnTurnStart,
                    effects = listOf(
                        Effect.AddCoins(value = 1),
                        Effect.Random(profile = "MARKUS_GAMBLER")
                    )
                )
            )
        ),
        CardDefinition(
            id = "WOODEN_SWORD",
            name = "Espada de Madeira",
            description = "Uma espada simples para iniciantes.",
            type = CardType.EQUIPMENT,
            rarity = Rarity.COMMON,
            baseStats = mapOf(Stat.ATK to 15.0),
            statsPerLevel = defaultStatsPerLevel,
            tags = setOf("starter"),
            abilities = emptyList()
        ),
        CardDefinition(
            id = "DEMON_HUNTER_CROSSBOW",
            name = "Besta da Cacadora de Demonios",
            description = "A cada poucos acertos, libera um golpe destrutivo.",
            type = CardType.EQUIPMENT,
            rarity = Rarity.LEGENDARY,
            baseStats = mapOf(
                Stat.SPEED to 15.0,
                Stat.ATK to 15.0
            ),
            statsPerLevel = mapOf(Stat.SPEED to 2.0),
            tags = setOf("speed"),
            abilities = listOf(
                Ability(
                    name = "Flecha de Prata",
                    type = AbilityType.PASSIVE,
                    trigger = AbilityTrigger.OnAttack,
                    effects = listOf(
                        Effect.Damage(value = 30.0, target = AbilityTarget.ENEMY)
                    )
                )
            )
        ),
        CardDefinition(
            id = "JORGE",
            name = "Jorge",
            description = "Escudeiro de Markus. Sustento e controle de ritmo.",
            type = CardType.CHARACTER,
            rarity = Rarity.EPIC,
            faction = "gambler",
            baseStats = mapOf(
                Stat.HP to 680.0,
                Stat.ATK to 28.0,
                Stat.DEF to 32.0,
                Stat.CRIT_CHANCE to 0.10,
                Stat.CRIT_DAMAGE to 1.5,
                Stat.SPEED to 70.0
            ),
            statsPerLevel = defaultStatsPerLevel,
            tags = setOf("rng", "gambler", "tank", "defense", "risk"),
            abilities = listOf(
                Ability(
                    name = "Apoio de Escudo",
                    type = AbilityType.PASSIVE,
                    trigger = AbilityTrigger.OnDamageTaken,
                    effects = listOf(
                        Effect.Heal(value = 5.0, target = AbilityTarget.ALL_ALLIES)
                    )
                ),
                Ability(
                    name = "Roleta de Protecao",
                    type = AbilityType.PASSIVE,
                    trigger = AbilityTrigger.OnTurnStart,
                    effects = listOf(
                        Effect.AddCoins(value = 1),
                        Effect.Random(profile = "MARKUS_GAMBLER")
                    )
                )
            )
        ),
        CardDefinition(
            id = "ALL_IN_EMBLEM",
            name = "Emblema do All-In",
            description = "Um emblema exclusivo de Markus. Pressao e risco no mesmo pacote.",
            type = CardType.EQUIPMENT,
            rarity = Rarity.LEGENDARY,
            faction = "markus_gang",
            baseStats = mapOf(
                Stat.ATK to 15.0,
                Stat.SPEED to 6.0,
                Stat.CRIT_CHANCE to 0.03
            ),
            statsPerLevel = defaultStatsPerLevel,
            tags = setOf("gambler", "markus", "signature", "weapon", "risk", "chaos"),
            abilities = listOf(
                Ability(
                    name = "Rajada All-In",
                    type = AbilityType.PASSIVE,
                    trigger = AbilityTrigger.OnAttack,
                    effects = listOf(
                        Effect.Damage(value = 16.0, target = AbilityTarget.ENEMY)
                    )
                ),
                Ability(
                    name = "A Casa Sempre Ganha",
                    type = AbilityType.PASSIVE,
                    trigger = AbilityTrigger.OnTurnStart,
                    effects = listOf(
                        Effect.AddCoins(value = 1),
                        Effect.Random(profile = "MARKUS_GAMBLER")
                    )
                )
            )
        ),
        CardDefinition(
            id = "KONO",
            name = "KONO",
            description = "Uma entidade única do sistema. Quando ela entra em combate, as regras do jogo começam a se adaptar sozinhas a sua presença.",
            type = CardType.CHARACTER,
            rarity = Rarity.KONO,
            faction = "system",
            baseStats = mapOf(
                Stat.HP to 1800.0,
                Stat.ATK to 140.0,
                Stat.DEF to 110.0,
                Stat.CRIT_CHANCE to 0.30,
                Stat.CRIT_DAMAGE to 2.8,
                Stat.SPEED to 150.0
            ),
            statsPerLevel = mapOf(
                Stat.HP to 40.0,
                Stat.ATK to 14.0,
                Stat.DEF to 12.0,
                Stat.SPEED to 6.0
            ),
            tags = setOf(
                "kono",
                "sistema",
                "unico",
                "boss",
                "realidade",
                "glitch"
            ),
            abilities = listOf(
                Ability(
                    name = "Resposta Paradoxal",
                    type = AbilityType.PASSIVE,
                    trigger = AbilityTrigger.OnDamageTaken,
                    effects = listOf(
                        Effect.Heal(
                            value = 70.0,
                            target = AbilityTarget.SELF
                        ),
                        Effect.BuffStat(stat = Stat.ATK, value = 10.0),
                        Effect.BuffStat(stat = Stat.DEF, value = 8.0)
                    )
                ),

                Ability(
                    name = "Janela de Reversão",
                    type = AbilityType.PASSIVE,
                    trigger = AbilityTrigger.OnTurnStart,
                    effects = listOf(
                        Effect.Heal(value = -120.0, target = AbilityTarget.ENEMY),
                        Effect.BuffStat(stat = Stat.SPEED, value = -40.0, target = AbilityTarget.ENEMY),
                        Effect.BuffStat(stat = Stat.ATK, value = -25.0, target = AbilityTarget.ENEMY),
                        Effect.Heal(value = 120.0, target = AbilityTarget.SELF),
                        Effect.BuffStat(stat = Stat.SPEED, value = 40.0),
                        Effect.BuffStat(stat = Stat.ATK, value = 25.0),
                    )
                )
            )
        )
    )

    private val byId = all.associateBy { it.id }

    fun getById(id: String): CardDefinition? = byId[id.uppercase()]
}
