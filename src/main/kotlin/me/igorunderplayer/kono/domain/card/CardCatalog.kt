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
            description = "Uma criatura gelatinosa e básica. Aguenta bastante pancada, mas causa pouco dano.",
            type = CardType.CHARACTER,
            rarity = Rarity.COMMON,
            faction = "slime",
            baseStats = mapOf(
                Stat.HP to 590.0,
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
                    description = "Sempre que ataca, o Slime perde um pouco de sua massa gelatinosa e sofre dano em si mesmo.",
                    type = AbilityType.PASSIVE,
                    trigger = AbilityTrigger.OnAttack,
                    effects = listOf(
                        Effect.Damage(value = 8.0, target = AbilityTarget.SELF)
                    )
                )
            )
        ),
        CardDefinition(
            id = "IRON_ARMOR",
            name = "Armadura de ferro simples",
            description = "Armadura simples que reduz dano recebido.",
            type = CardType.EQUIPMENT,
            rarity = Rarity.COMMON,
            baseStats = mapOf(
                Stat.HP to 60.0,
                Stat.DEF to 15.0,
                Stat.SPEED to -5.0
            ),
            statsPerLevel = defaultStatsPerLevel,
            tags = setOf("armor", "defense"),
            abilities = emptyList()
        ),
        CardDefinition(
            id = "HEAVY_IRON_ARMOR",
            name = "Armadura de ferro pesada",
            description = "Armadura pesada que reduz dano recebido.",
            type = CardType.EQUIPMENT,
            rarity = Rarity.RARE,
            baseStats = mapOf(
                Stat.HP to 90.0,
                Stat.DEF to 40.0,
                Stat.SPEED to -20.0
            ),
            statsPerLevel = defaultStatsPerLevel,
            tags = setOf("heavy", "armor", "defense"),
            abilities = emptyList()
        ),
        CardDefinition(
            id = "GAMBLER_CHARM",
            name = "Talismã do apostador",
            description = "Um artefato caótico que recompensa quem confia no acaso.",
            type = CardType.EQUIPMENT,
            rarity = Rarity.EPIC,
            baseStats = mapOf(
                Stat.CRIT_CHANCE to 0.05,
                Stat.CRIT_DAMAGE to 0.15
            ),
            statsPerLevel = mapOf(
                Stat.CRIT_CHANCE to 0.01,
                Stat.CRIT_DAMAGE to 0.02
            ),
            tags = setOf("gambler", "chaos"),
            abilities = listOf(
                Ability(
                    name = "Roleta Caotica",
                    description = "A cada turno, a roleta caótica é ativada e pode causar dano ou curar o portador.",
                    type = AbilityType.PASSIVE,
                    trigger = AbilityTrigger.OnTurnStart,
                    effects = listOf(Effect.Random(profile = "GAMBLER_CHARM"))
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
            baseStats = mapOf(
                Stat.ATK to 16.0,
                Stat.LIFESTEAL to .15,
            ),
            statsPerLevel = mapOf(
                Stat.ATK to 2.0,
                Stat.LIFESTEAL to .01
            ),
            tags = setOf("vampire", "lifesteal"),
            abilities = emptyList()
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
            description = "erro: undefined não é um item",
            type = CardType.EQUIPMENT,
            rarity = Rarity.MYTHIC,
            baseStats = emptyMap(),
            statsPerLevel = defaultStatsPerLevel,
            tags = setOf("bug", "chaos"),
            abilities = listOf(
                Ability(
                    name = "Bug",
                    description = "`fatal error: não foi possível obter a descrição do NULL BUG`",
                    type = AbilityType.PASSIVE,
                    trigger = AbilityTrigger.OnTurnStart,
                    effects = listOf(Effect.Random(profile = "UNDEFINED_BUG"))
                )
            )
        ),
        CardDefinition(
            id = "JUNIOR_KNIGHT",
            name = "Cavaleirinho",
            description = "Um jovem aprendiz com armadura simples e uma espada de madeira.",
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
            description = "Ágil e oportunista. Ganha vantagem em aberturas e pressiona com golpes extras.",
            type = CardType.CHARACTER,
            rarity = Rarity.RARE,
            baseStats = mapOf(
                Stat.HP to 420.0,
                Stat.ATK to 36.0,
                Stat.DEF to 14.0,
                Stat.CRIT_CHANCE to 0.10,
                Stat.CRIT_DAMAGE to 1.3,
                Stat.SPEED to 115.0
            ),
            statsPerLevel = defaultStatsPerLevel,
            tags = setOf("starter"),
            abilities = listOf(
                Ability(
                    name = "Injusto",
                    description = "O Bandido vê uma brecha na defesa do inimigo e a explora, causando dano adicional.",
                    type = AbilityType.PASSIVE,
                    trigger = AbilityTrigger.OnAttack,
                    effects = listOf(
                        Effect.Damage(value = 8.0, target = AbilityTarget.ENEMY)
                    )
                )
            )
        ),
        CardDefinition(
            id = "MARKUS",
            name = "Markus, Mestre das Apostas",
            description = "Cada turno vira uma aposta. Risco alto, recompensa ainda maior.",
            type = CardType.CHARACTER,
            rarity = Rarity.LEGENDARY,
            faction = "markus_gang",
            baseStats = mapOf(
                Stat.HP to 720.0,
                Stat.ATK to 58.0,
                Stat.DEF to 16.0,
                Stat.CRIT_CHANCE to 0.15,
                Stat.CRIT_DAMAGE to 1.5,
                Stat.SPEED to 110.0
            ),
            statsPerLevel = mapOf(
                Stat.HP to 6.0,
                Stat.ATK to 4.0,
                Stat.DEF to 2.0,
                Stat.CRIT_CHANCE to 0.01,
                Stat.CRIT_DAMAGE to 0.02,
            ),
            tags = setOf("rng", "gambler", "risk", "chaos", "scaling", "boss", "markus_gang", "malignant"),
            abilities = listOf(
                Ability(
                    name = "Mestre da Mesa",
                    description = "A cada rodada, Markus gera 2 moedas de cassino para a equipe e aposta com elas para obter efeitos aleatórios. Quanto mais moedas, melhores os efeitos.",
                    type = AbilityType.PASSIVE,
                    trigger = AbilityTrigger.OnTurnStart,
                    effects = listOf(
                        Effect.AddCoins(value = 2, scaleWithGangSynergy = false),
                        Effect.Random(profile = "MARKUS_GAMBLER")
                    )
                )
            )
        ),
        CardDefinition(
            id = "VEYN",
            name = "Veyn",
            description = "Besteiro de Markus. Quanto mais ritmo, maior a pressão sobre o alvo.",
            type = CardType.CHARACTER,
            rarity = Rarity.EPIC,
            faction = "markus_gang",
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
                    description = "A cada 2 disparos, Veyn libera uma rajada rítmica e causa um ataque extra no alvo.",
                    type = AbilityType.PASSIVE,
                    trigger = AbilityTrigger.OnAttackEvery(2),
                    effects = listOf(
                        Effect.Damage(value = 14.0, target = AbilityTarget.ENEMY)
                    )
                ),
                Ability(
                    name = "Ritmo de Cassino",
                    description = "A cada turno, Veyn gera 1 moeda de cassino para a equipe e aposta com ela para obter efeitos aleatórios.",
                    type = AbilityType.PASSIVE,
                    trigger = AbilityTrigger.OnTurnStart,
                    effects = listOf(
                        Effect.AddCoins(value = 1, scaleWithGangSynergy = false),
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
            baseStats = mapOf(Stat.ATK to 12.0),
            statsPerLevel = defaultStatsPerLevel,
            tags = setOf("starter"),
            abilities = emptyList()
        ),
        CardDefinition(
            id = "DEMON_HUNTER_CROSSBOW",
            name = "Besta da Cacadora de Demonios",
            description = "A cada poucos acertos, libera um disparo destrutivo.",
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
                    description = "A cada 3 ataques, a besta dispara uma flecha de prata que causa dano extra ao inimigo.",
                    type = AbilityType.PASSIVE,
                    trigger = AbilityTrigger.OnAttackEvery(3),
                    effects = listOf(
                        Effect.Damage(value = 30.0, target = AbilityTarget.ENEMY)
                    )
                )
            )
        ),
        CardDefinition(
            id = "JORGE",
            name = "Jorge",
            description = "Escudeiro de Markus. Um guardião que protege aliados, intercepta dano e segura a linha de frente.",
            type = CardType.CHARACTER,
            rarity = Rarity.EPIC,
            faction = "markus_gang",
            baseStats = mapOf(
                Stat.HP to 840.0,
                Stat.ATK to 24.0,
                Stat.DEF to 48.0,
                Stat.CRIT_CHANCE to 0.10,
                Stat.CRIT_DAMAGE to 1.35,
                Stat.SPEED to 62.0
            ),
            statsPerLevel = defaultStatsPerLevel,
            tags = setOf("gambler", "tank", "defense", "protector", "frontline"),
            abilities = listOf(
                Ability(
                    name = "Guarda Juramentada",
                    description = "No início da batalha, Jorge assume a linha de frente, provoca os inimigos e protege o time dividindo parte do dano recebido.",
                    type = AbilityType.PASSIVE,
                    trigger = AbilityTrigger.OnBattleStart,
                    effects = listOf(
                        Effect.Taunt,
                        Effect.ProtectAlliesDamageShare(sharePercent = 0.30)
                    )
                ),
                Ability(
                    name = "Fortaleza Viva",
                    description = "Quando Jorge recebe dano, ele fortalece sua resistência e se cura para continuar protegendo o time.",
                    type = AbilityType.PASSIVE,
                    trigger = AbilityTrigger.OnDamageTaken,
                    effects = listOf(
                        Effect.Heal(value = 12.0, target = AbilityTarget.SELF)
                    )
                ),
                Ability(
                    name = "Muralha do Esquadrão",
                    description = "A cada turno, Jorge reforça sua defesa e cura todos os aliados do time.",
                    type = AbilityType.PASSIVE,
                    trigger = AbilityTrigger.OnTurnStart,
                    effects = listOf(
                        Effect.BuffStat(stat = Stat.DEF, value = 4.0, target = AbilityTarget.SELF),
                        Effect.Heal(value = 6.0, target = AbilityTarget.ALL_ALLIES)
                    )
                )
            )
        ),
        CardDefinition(
            id = "ALL_IN_EMBLEM",
            name = "Emblema do All-In",
            description = "Um emblema exclusivo de Markus. Pressão e risco no mesmo pacote.",
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
                    description = "Cada ataque causa dano extra pela ousadia do portador.",
                    type = AbilityType.PASSIVE,
                    trigger = AbilityTrigger.OnAttack,
                    effects = listOf(
                        Effect.Damage(value = 16.0, target = AbilityTarget.ENEMY)
                    )
                ),
                Ability(
                    name = "A Casa Sempre Ganha",
                    description = "A cada turno, o portador do emblema gera 1 moeda de cassino para a equipe e aposta com ela para obter efeitos aleatórios.",
                    type = AbilityType.PASSIVE,
                    trigger = AbilityTrigger.OnTurnStart,
                    effects = listOf(
                        Effect.AddCoins(value = 1, scaleWithGangSynergy = false),
                        Effect.Random(profile = "MARKUS_GAMBLER")
                    )
                )
            )
        ),
        CardDefinition(
            id = "KONO",
            name = "Kono",
            description = "Uma entidade única do sistema. Quando entra em combate, as regras do jogo se adaptam à sua presença.",
            type = CardType.CHARACTER,
            rarity = Rarity.KONO,
            faction = "system",
            baseStats = mapOf(
                Stat.HP to 2450.0,
                Stat.ATK to 320.0,
                Stat.DEF to 180.0,
                Stat.CRIT_CHANCE to 0.5,
                Stat.CRIT_DAMAGE to 3.0,
                Stat.SPEED to 180.0
            ),
            statsPerLevel = mapOf(
                Stat.HP to 80.0,
                Stat.ATK to 28.0,
                Stat.DEF to 24.0,
                Stat.SPEED to 12.0
            ),
            tags = setOf(
                "kono",
                "system",
                "boss"
            ),
            abilities = listOf(
                Ability(
                    name = "Resposta Paradoxal",
                    description = "A cada golpe recebido, Kono distorce a realidade, se cura e fortalece o próprio poder.",
                    type = AbilityType.PASSIVE,
                    trigger = AbilityTrigger.OnDamageTaken,
                    effects = listOf(
                        Effect.Heal(
                            value = 40.0,
                            target = AbilityTarget.SELF
                        ),
                        Effect.BuffStat(stat = Stat.ATK, value = 10.0),
                        Effect.BuffStat(stat = Stat.DEF, value = 8.0)
                    )
                ),
                Ability(
                    name = "Janela de Reversão",
                    description = "A cada 3 turnos, Kono reescreve o fluxo do combate, enfraquecendo inimigos e se fortalecendo.",
                    type = AbilityType.PASSIVE,
                    trigger = AbilityTrigger.OnTurnEvery(3),
                    effects = listOf(
                        Effect.Heal(value = -120.0, target = AbilityTarget.ENEMY),
                        Effect.BuffStat(stat = Stat.SPEED, value = -25.0, target = AbilityTarget.ENEMY),
                        Effect.BuffStat(stat = Stat.ATK, value = -25.0, target = AbilityTarget.ENEMY),
                        Effect.Heal(value = 120.0, target = AbilityTarget.SELF),
                        Effect.BuffStat(stat = Stat.SPEED, value = 25.0),
                        Effect.BuffStat(stat = Stat.ATK, value = 25.0),
                    )
                ),
                Ability(
                    name = "Ruptura de Realidade",
                    description = "Ao cair abaixo de 15% de vida, Kono rompe a realidade, libera energia caótica e enfraquece profundamente os inimigos. Só pode ocorrer uma vez.",
                    type = AbilityType.PASSIVE,
                    trigger = AbilityTrigger.OnBellowHealth(0.15),
                    once = true,
                    effects = listOf(
                        Effect.Damage(1000.0, AbilityTarget.ALL_ENEMIES),
                        Effect.BuffStat(Stat.ATK, -30.0, target = AbilityTarget.ALL_ENEMIES),
                        Effect.BuffStat(Stat.SPEED, -20.0, target = AbilityTarget.ALL_ENEMIES)
                    )
                ),
                Ability(
                    name = "Quebra de código",
                    description = "Ao morrer, Kono quebra o código do combate e tenta levar o campo de batalha junto com ele.",
                    type = AbilityType.PASSIVE,
                    trigger = AbilityTrigger.OnDeath,
                    once = true,
                    effects = listOf(
                        Effect.Heal(value = 5000.0, target = AbilityTarget.SELF),
                        Effect.Damage(value = 1500.0, target = AbilityTarget.ALL_ENEMIES),
                    )
                )
            )
        ),
        CardDefinition(
            id = "IRON_SWORD",
            name = "Espada de Ferro",
            type = CardType.EQUIPMENT,
            rarity = Rarity.RARE,
            description = "Uma espada de ferro simples, concede um dano bom",
            baseStats = mapOf(
                Stat.ATK to 22.0,
                Stat.SPEED to -2.0,
            ),
            statsPerLevel = mapOf(
                Stat.ATK to 2.0
            ),
            tags = setOf("weapon", "starter", "iron"),
            abilities = emptyList()
        ),
        CardDefinition(
            id = "IRON_SHIELD",
            name = "Escudo de Ferro",
            type = CardType.EQUIPMENT,
            rarity = Rarity.RARE,
            description = "Um escudo relativamente pesado, feito de ferro com madeira",
            baseStats = mapOf(
                Stat.DEF to 26.0,
                Stat.SPEED to -6.0,
                Stat.ATK to -4.0,
                Stat.HP to 80.0,
            ),
            statsPerLevel = mapOf(
                Stat.DEF to 2.0,
                Stat.HP to 8.0,
            ),
            tags = setOf("shield", "starter", "iron"),
            abilities = emptyList()
        ),
        CardDefinition(
            id = "KATANA",
            name = "Katana Simples",
            type = CardType.EQUIPMENT,
            rarity = Rarity.RARE,
            description = "Uma velha katana. Leve, rápida porém com pouco dano.",
            baseStats = mapOf(
                Stat.ATK to 12.0,
                Stat.SPEED to 4.0,
                Stat.CRIT_CHANCE to 0.08,
                Stat.DEF to -4.0,
            ),
            statsPerLevel = mapOf(
                Stat.ATK to 0.6,
                Stat.SPEED to 0.2,
                Stat.CRIT_CHANCE to 0.01
            ),
            tags = setOf("iron", "speed", "crit"),
            abilities = listOf(
                Ability(
                    name = "Corte rapido",
                    description = "Cada ataque com a katana causa um ataque extra aplicando um dano fixo ao inimigo.",
                    type = AbilityType.PASSIVE,
                    trigger = AbilityTrigger.OnAttack,
                    effects = listOf(
                        Effect.Damage(4.0)
                    )
                )
            )
        ),
        CardDefinition(
            id = "UNLEASHED_JUNIOR_KNIGHT",
            name = "Cavaleirinho, O Descendente de Deus",
            rarity = Rarity.MYTHIC,
            description = "Após sua mãe finalmente aprovar seu sonho de se tornar cavaleiro, o Cavaleirinho treinou sem parar.\n" +
                    "Agora, em sua forma final, ele se ergue como um guerreiro nobre — de coração puro e determinação inabalável.",
            type = CardType.CHARACTER,
            baseStats = mapOf(
                Stat.HP to 980.0,
                Stat.DEF to 88.0,
                Stat.ATK to 112.0,
                Stat.SPEED to 90.0,
                Stat.CRIT_CHANCE to 0.3,
                Stat.CRIT_DAMAGE to 2.0,
            ),
            statsPerLevel = mapOf(
                Stat.ATK to 16.0,
                Stat.SPEED to 4.0,
                Stat.CRIT_CHANCE to 0.02,
                Stat.DEF to 14.0,
            ),
            tags = setOf("knight"),
            abilities = listOf(
                Ability(
                    name = "Presença inabalável",
                    description = "O Cavaleirinho fica mais resistente quando está com pouca vida; sua determinação o fortalece para resistir a golpes que poderiam derrubá-lo.",
                    type = AbilityType.PASSIVE,
                    trigger = AbilityTrigger.OnBellowHealth(0.4),
                    effects = listOf(
                        Effect.StatIncreaseWhileBelowHealth(
                            stat = Stat.DEF,
                            value = 88.0,
                            threshold = 0.4
                        )
                    )
                ),
                Ability(
                    name = "Benção do Escudo Sagrado", // a cada 4 rounds
                    description = "A cada 4 turnos, o Cavaleirinho recebe a bênção do escudo sagrado, recuperando vida e se fortalecendo. A proteção divina ajuda a resistir a ataques poderosos e a se manter firme em batalha.",
                    type = AbilityType.PASSIVE,
                    trigger = AbilityTrigger.OnTurnEvery(4),
                    effects = listOf(
                        Effect.Heal(
                            value = 240.0
                        )
                    )
                ),
                Ability(
                    name = "Senso de justiça",
                    description = "Causa 20% de dano extra em inimigos malignos.",
                    type = AbilityType.PASSIVE,
                    trigger = AbilityTrigger.OnAttackAgainstTag("malignant"),
                    effects = listOf(
                        Effect.DamageIncreasePercent(value = 0.2)
                    )
                )
            )
        ),
        CardDefinition(
            id = "POLISHED_KATANA",
            name = "Katana Polida",
            description = "Uma Katana nova, polída, parece extremamente ágil e confiavel",
            type = CardType.EQUIPMENT,
            rarity = Rarity.EPIC,
            baseStats = mapOf(
                Stat.ATK to 15.0,
                Stat.CRIT_CHANCE to 0.1,
                Stat.SPEED to 8.0
            ),
            tags = setOf("steel", "speed", "crit", "polished"),
            statsPerLevel = mapOf(
                Stat.ATK to 1.5,
                Stat.SPEED to 0.4,
                Stat.CRIT_CHANCE to 0.01
            ),
            abilities = listOf(
                Ability(
                    name = "Corte rapido",
                    description = "Cada ataque com a katana causa um ataque extra aplicando um dano fixo ao inimigo.",
                    type = AbilityType.PASSIVE,
                    trigger = AbilityTrigger.OnAttack,
                    effects = listOf(
                        Effect.Damage(8.0)
                    )
                )
            )
        ),
        CardDefinition(
            id = "GREATSWORD",
            name = "Espada Grande",
            rarity = Rarity.EPIC,
            description = "Uma espada grande e pesada, extremamente pesada, mas efetiva",
            type = CardType.EQUIPMENT,
            baseStats = mapOf(
                Stat.ATK to 38.0,
                Stat.SPEED to -12.0
            ),
            statsPerLevel = mapOf(
                Stat.ATK to 6.0
            ),
            tags = setOf("iron", "great", "heavy"),
            abilities = listOf(
                Ability(
                    name = "Golpes pesados",
                    description = "O peso dessa enorme espada recompensa aqueles fortes suficientes para ergue-lá, ganhando aumento de 20% de ataque no inicio da batalha",
                    trigger = AbilityTrigger.OnBattleStart,
                    type = AbilityType.PASSIVE,
                    effects = listOf(
                        Effect.StatIncreasePercent(Stat.ATK, 0.25)
                    )
                ),
                Ability(
                    name = "Golpe Colossal",
                    description = "Cada 3 ataques, o portador da espada desferi um golpe colossal, causando um dano massivo que escala com seu ataque atual.",
                    trigger = AbilityTrigger.OnAttackEvery(3),
                    type = AbilityType.PASSIVE,
                    effects = listOf(
                        Effect.DamageBasedOnStat(Stat.ATK, 1.4)
                    )
                )
            )
        ),
        CardDefinition(
            id = "AURUM",
            name = "Aurum",
            description = "Converte a economia do time em poder crescente ao longo do combate.",
            type = CardType.CHARACTER,
            faction = "markus_gang",
            rarity = Rarity.EPIC,
            tags = setOf("gambler", "economy", "scaling", "support"),
            baseStats = mapOf(
                Stat.HP to 5.0,
                Stat.ATK to 48.0,
                Stat.DEF to 22.0,
                Stat.CRIT_CHANCE to 0.12,
                Stat.CRIT_DAMAGE to 1.4,
                Stat.SPEED to 90.0
            ),
            statsPerLevel = defaultStatsPerLevel,
            abilities = listOf(
                Ability(
                    name = "Geração de Riqueza",
                    description = "Gera 1 moeda por turno; se houver pelo menos um aliado da facção gambler, passa a gerar 2. A cada 10 moedas acumuladas, gera +1 moeda adicional.",
                    type = AbilityType.PASSIVE,
                    trigger = AbilityTrigger.OnTurnStart,
                    effects = listOf(
                        Effect.AddCoinsScaling(
                            base = 1,
                            coinsPerStack = 10,
                            bonusPerStack = 1,
                            allyFactionForBaseBonus = "markus_gang",
                            requiredAlliesForBaseBonus = 1,
                            baseBonus = 1
                        )
                    )
                ),
                Ability(
                    name = "Investimento Crescente",
                    description = "Ganha bônus de ATK, SPEED, DEF e DANO CRÍTICO com base na quantidade de moedas acumuladas pelo time.",
                    type = AbilityType.PASSIVE,
                    trigger = AbilityTrigger.OnTurnStart,
                    effects = listOf(
                        Effect.BuffStatByTeamCoins(
                            stat = Stat.ATK,
                            valuePerStack = 3.0,
                            coinsPerStack = 10
                        ),
                        Effect.BuffStatByTeamCoins(
                            stat = Stat.DEF,
                            valuePerStack = 2.0,
                            coinsPerStack = 10
                        ),
                        Effect.BuffStatByTeamCoins(
                            stat = Stat.SPEED,
                            valuePerStack = 1.5,
                            coinsPerStack = 10
                        ),
                        Effect.BuffStatByTeamCoins(
                            stat = Stat.CRIT_DAMAGE,
                            valuePerStack = 0.05,
                            coinsPerStack = 10
                        )
                    )
                )
            ),
        )
    )

    private val byId = all.associateBy { it.id }

    fun getById(id: String): CardDefinition? = byId[id.uppercase()]
}
