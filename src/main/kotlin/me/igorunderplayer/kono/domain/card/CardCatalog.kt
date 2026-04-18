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
                    description = "A cada ataque, Slime perde um pouco de sua gorma e recebe dano de si mesmo.",
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
            description = "Um artefato caotico.",
            type = CardType.EQUIPMENT,
            rarity = Rarity.EPIC,
            baseStats = emptyMap(),
            statsPerLevel = defaultStatsPerLevel,
            tags = setOf("gambler", "chaos"),
            abilities = listOf(
                Ability(
                    name = "Roleta Caotica",
                    description = "A cada turno, um efeito aleatório acontece. Pode ser algo bom... ou ruim.",
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
                    description = "Cada ataque cura o portador por em 10% do dano causado.",
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
                    name = "Bug",
                    description = "`fatal error: could not get description for NULL BUG`",
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
                Stat.CRIT_DAMAGE to 1.3,
                Stat.SPEED to 115.0
            ),
            statsPerLevel = defaultStatsPerLevel,
            tags = setOf("starter"),
            abilities = listOf(
                Ability(
                    name = "Injusto",
                    description = "Bandido vê uma brecha na defesa do inimigo e a explora, causando dano adicional.",
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
            statsPerLevel = mapOf(
                Stat.HP to 6.0,
                Stat.ATK to 4.0,
                Stat.DEF to 2.0,
                Stat.CRIT_CHANCE to 0.01,
                Stat.CRIT_DAMAGE to 0.02,
            ),
            tags = setOf("rng", "gambler", "risk", "chaos", "scaling", "boss"),
            abilities = listOf(
                Ability(
                    name = "Mestre da Mesa",
                    description = "A cada rodada, Markus gera moedas de cassino para a equipe, e aposta usando elas para obter efeitos aleatórios. Quanto mais moedas, melhores os efeitos serão.",
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
                    description = "Cada disparo de Veyn aumenta o ritmo de sua arma, causando 2 ataques adicionais a cada 2 tiros.",
                    type = AbilityType.PASSIVE,
                    trigger = AbilityTrigger.OnAttackEvery(2),
                    effects = listOf(
                        Effect.Damage(value = 14.0, target = AbilityTarget.ENEMY)
                    )
                ),
                Ability(
                    name = "Ritmo de Cassino",
                    description = "A cada turno, Veyn gera moedas de cassino para a equipe, e aposta usando elas para obter efeitos aleatórios. Quanto mais moedas, melhores os efeitos serão.",
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
            baseStats = mapOf(Stat.ATK to 12.0),
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
                    description = "Cada 3 ataques, a besta dispara uma flecha de prata que causa dano extra ignorando a defesa do inimigo.",
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
                    description = "Quando Jorge recebe dano, ele usa seu escudo e concede cura a todos os seus aliados próximos.",
                    type = AbilityType.PASSIVE,
                    trigger = AbilityTrigger.OnDamageTaken,
                    effects = listOf(
                        Effect.Heal(value = 5.0, target = AbilityTarget.ALL_ALLIES)
                    )
                ),
                Ability(
                    name = "Roleta de Protecao",
                    description = "A cada turno, Jorge gera moedas de cassino para a equipe, e aposta usando elas para obter efeitos aleatórios. Quanto mais moedas, melhores os efeitos serão.",
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
                    description = "Cada ataque, dá um dano extra por sua ousadia e coragem.",
                    type = AbilityType.PASSIVE,
                    trigger = AbilityTrigger.OnAttack,
                    effects = listOf(
                        Effect.Damage(value = 16.0, target = AbilityTarget.ENEMY)
                    )
                ),
                Ability(
                    name = "A Casa Sempre Ganha",
                    description = "A cada turno, o portador do emblema gera moedas de cassino para a equipe, e aposta usando elas para obter efeitos aleatórios. Quanto mais moedas, melhores os efeitos serão.",
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
            name = "Kono",
            description = "Uma entidade única do sistema. Quando ela entra em combate, as regras do jogo começam a se adaptar sozinhas a sua presença.",
            type = CardType.CHARACTER,
            rarity = Rarity.KONO,
            faction = "system",
            baseStats = mapOf(
                Stat.HP to 2450.0,
                Stat.ATK to 270.0,
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
                    description = "Kono se adapta a cada ataque recebido, curando parte do dano e fortalecendo-se para resistir melhor a ataques futuros.",
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
                    description = "A cada 3 turnos, Kono ativa uma janela de reversão temporal, onde ele reverte o estado do combate para o que era 2 turnos atrás, mas com as condições atuais de vida e buffs/debuffs. Isso pode ser usado para desfazer danos recebidos ou ataques falhos, mas também pode reverter buffs benéficos ou curas recebidas.",
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
                    description = "Quando Kono fica abaixo de 15% de vida, ele sofre uma ruptura de realidade, onde a linha entre o real e o virtual se torna tênue. Ele causa uma explosão de energia caótica que causa dano massivo a todos os inimigos, e aplica um debuff que reduz significativamente o ataque e a velocidade dos inimigos por um tempo limitado. Essa habilidade só pode ser ativada uma vez por combate.",
                    type = AbilityType.PASSIVE,
                    trigger = AbilityTrigger.OnBellowHealth(0.15),
                    once = true,
                    effects = listOf(
                        Effect.Damage(800.0, AbilityTarget.ALL_ENEMIES),
                        Effect.BuffStat(Stat.ATK, -30.0, target = AbilityTarget.ALL_ENEMIES),
                        Effect.BuffStat(Stat.SPEED, -20.0, target = AbilityTarget.ALL_ENEMIES)
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
            description = "Após sua mãe finalmente aprovar seu sonho de se tornar um cavaleiro, o Cavaleirinho treinou incansavelmente.\n" +
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
                    description = "O Cavaleirinho se torna mais resistente quando está com pouca vida, sua determinação e coragem o fortalecem para resistir a ataques que poderiam derrubá-lo.",
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
                    description = "A cada 4 turnos, o Cavaleirinho recebe a benção do escudo sagrado, curando uma parte de sua vida se fortalecendo. A proteção divina do escudo sagrado ajuda o Cavaleirinho a resistir a ataques poderosos e a se manter firme em batalha.",
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
                    description = "Causa 20% de dano extra em inimigos \"malignos\"",
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
                Stat.ATK to 32.0,
                Stat.SPEED to -12.0
            ),
            statsPerLevel = mapOf(
                Stat.ATK to 4.0
            ),
            tags = setOf("iron", "great", "heavy"),
            abilities = listOf(
                Ability(
                    name = "Golpes pesados",
                    description = "O peso dessa enorme espada recompensa aqueles fortes suficientes para ergue-lá, ganhando aumento de 20% de ataque no inicio da batalha",
                    trigger = AbilityTrigger.OnBattleStart,
                    type = AbilityType.PASSIVE,
                    effects = listOf(
                        Effect.StatIncreasePercent(Stat.ATK, 0.2)
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
        )
    )

    private val byId = all.associateBy { it.id }

    fun getById(id: String): CardDefinition? = byId[id.uppercase()]
}
