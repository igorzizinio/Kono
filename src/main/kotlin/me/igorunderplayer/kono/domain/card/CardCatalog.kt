package me.igorunderplayer.kono.domain.card

import me.igorunderplayer.kono.domain.card.ability.*
import me.igorunderplayer.kono.domain.gameplay.CombatEvent
import me.igorunderplayer.kono.domain.gameplay.TemporaryStatModifier


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
        description = "Criatura gelatinosa. Aguenta bastante pancada, mas é fraca ofensivamente — e ainda sofre ao atacar.",
        type = CardType.CHARACTER,
        rarity = Rarity.COMMON,
        faction = "slime",
        baseStats = mapOf(
            Stat.HP to 520.0,
            Stat.ATK to 28.0,
            Stat.DEF to 5.0,
            Stat.SPEED to 72.0,
            Stat.CRIT_CHANCE to 0.04,
            Stat.CRIT_DAMAGE to 1.20
        ),
        statsPerLevel = mapOf(
            Stat.HP to 10.0,
            Stat.ATK to 2.5,
            Stat.DEF to 1.5
        ),
        tags = setOf("slime", "starter"),
        abilities = listOf(
            Ability(
                name = "Gelatina Instável",
                description = "Ao atacar, o Slime perde massa e sofre 12 de dano em si mesmo.",
                type = AbilityType.PASSIVE,
                trigger = AbilityTrigger.OnAttack,
                effects = listOf(Effect.Damage(value = 12.0, target = AbilityTarget.SELF))
            )
        )
    )

    private val juniorKnight = CardDefinition(
        id = "JUNIOR_KNIGHT",
        name = "Cavaleirinho",
        description = "Jovem aprendiz com armadura simples. Confiável, sem grandes surpresas.",
        type = CardType.CHARACTER,
        rarity = Rarity.COMMON,
        baseStats = mapOf(
            Stat.HP to 500.0,
            Stat.ATK to 34.0,
            Stat.DEF to 28.0,
            Stat.SPEED to 70.0,
            Stat.CRIT_CHANCE to 0.05,
            Stat.CRIT_DAMAGE to 1.20
        ),
        statsPerLevel = mapOf(
            Stat.HP to 8.0,
            Stat.ATK to 3.0,
            Stat.DEF to 2.0
        ),
        tags = setOf("starter"),
        abilities = emptyList()
    )

    // =========================================================================
    // RARE CHARACTERS
    // =========================================================================

    private val thief = CardDefinition(
        id = "THIEF",
        name = "Bandido",
        description = "Veloz e oportunista. Ataca antes de todos e surpreende com golpes extras a cada 2 ataques.",
        type = CardType.CHARACTER,
        rarity = Rarity.RARE,
        baseStats = mapOf(
            Stat.HP to 410.0,
            Stat.ATK to 44.0,
            Stat.DEF to 12.0,
            Stat.SPEED to 115.0,
            Stat.CRIT_CHANCE to 0.14,
            Stat.CRIT_DAMAGE to 1.40
        ),
        statsPerLevel = mapOf(
            Stat.HP to 5.0,
            Stat.ATK to 4.5,
            Stat.SPEED to 2.0
        ),
        tags = setOf("starter"),
        abilities = listOf(
            Ability(
                name = "Injusto",
                description = "A cada 2 ataques, o Bandido explora uma brecha na defesa do inimigo e causa 18 de dano extra.",
                type = AbilityType.PASSIVE,
                trigger = AbilityTrigger.OnAttackEvery(2),
                effects = listOf(Effect.Damage(value = 18.0, target = AbilityTarget.ENEMY))
            )
        )
    )

    // =========================================================================
    // EPIC CHARACTERS
    // =========================================================================

    private val jorge = CardDefinition(
        id = "JORGE",
        name = "Jorge",
        description = "Escudeiro jurado de Markus. Provoca inimigos, absorve dano pelos aliados e cresce em resistência a cada turno.",
        type = CardType.CHARACTER,
        rarity = Rarity.EPIC,
        faction = "markus_gang",
        baseStats = mapOf(
            Stat.HP to 720.0,
            Stat.ATK to 28.0,
            Stat.DEF to 44.0,
            Stat.SPEED to 65.0,
            Stat.CRIT_CHANCE to 0.10,
            Stat.CRIT_DAMAGE to 1.30
        ),
        statsPerLevel = mapOf(
            Stat.HP to 15.0,
            Stat.ATK to 1.5,
            Stat.DEF to 3.0
        ),
        tags = setOf("gambler", "tank", "defense", "protector", "frontline"),
        abilities = listOf(
            Ability(
                name = "Guarda Juramentada",
                description = "No início da batalha, Jorge provoca os inimigos e divide 25% do dano recebido pelos aliados.",
                type = AbilityType.PASSIVE,
                trigger = AbilityTrigger.OnBattleStart,
                effects = listOf(
                    Effect.Taunt,
                    Effect.ProtectAlliesDamageShare(sharePercent = 0.25)
                )
            ),
            Ability(
                name = "Fortaleza Viva",
                description = "Ao receber dano, Jorge se cura em 10 HP, sustentando sua presença na linha de frente.",
                type = AbilityType.PASSIVE,
                trigger = AbilityTrigger.OnDamageTaken,
                effects = listOf(Effect.Heal(value = 10.0, target = AbilityTarget.SELF))
            ),
            Ability(
                name = "Muralha do Esquadrão",
                description = "A cada turno, Jorge ganha +1 DEF permanente e cura todos os aliados em 4 HP.",
                type = AbilityType.PASSIVE,
                trigger = AbilityTrigger.OnTurnStart,
                effects = listOf(
                    Effect.BuffStat(stat = Stat.DEF, value = 1.0, target = AbilityTarget.SELF),
                    Effect.Heal(value = 4.0, target = AbilityTarget.ALL_ALLIES)
                )
            )
        )
    )

    private val veyn = CardDefinition(
        id = "VEYN",
        name = "Veyn",
        description = "Besteiro de Markus. Extremamente veloz e frágil — mas cada 2 disparos libera uma rajada devastadora.",
        type = CardType.CHARACTER,
        rarity = Rarity.EPIC,
        faction = "markus_gang",
        baseStats = mapOf(
            Stat.HP to 440.0,
            Stat.ATK to 44.0,
            Stat.DEF to 16.0,
            Stat.SPEED to 125.0,
            Stat.CRIT_CHANCE to 0.16,
            Stat.CRIT_DAMAGE to 1.30
        ),
        statsPerLevel = mapOf(
            Stat.HP to 5.0,
            Stat.ATK to 3.0,
            Stat.SPEED to 2.0,
            Stat.CRIT_CHANCE to 0.01
        ),
        tags = setOf("rng", "gambler", "speed", "archer", "marksman", "risk"),
        abilities = listOf(
            Ability(
                name = "Rajada Rítmica",
                description = "A cada 2 ataques, Veyn libera uma rajada precisa que causa 25 de dano extra ao alvo.",
                type = AbilityType.PASSIVE,
                trigger = AbilityTrigger.OnAttackEvery(2),
                effects = listOf(Effect.Damage(value = 25.0, target = AbilityTarget.ENEMY))
            ),
            Ability(
                name = "Ritmo de Cassino",
                description = "A cada turno, Veyn gera 1 moeda de cassino para a equipe e aposta para obter efeitos aleatórios.",
                type = AbilityType.PASSIVE,
                trigger = AbilityTrigger.OnTurnStart,
                effects = listOf(
                    Effect.AddCoins(value = 1, scaleWithGangSynergy = false),
                    Effect.Random(profile = "MARKUS_GAMBLER")
                )
            )
        )
    )

    private val aurum = CardDefinition(
        id = "AURUM",
        name = "Aurum",
        description = "Economista do grupo de Markus. Gera moedas continuamente e converte a economia do time em poder de combate.",
        type = CardType.CHARACTER,
        rarity = Rarity.EPIC,
        faction = "markus_gang",
        baseStats = mapOf(
            Stat.HP to 540.0,
            Stat.ATK to 50.0,
            Stat.DEF to 24.0,
            Stat.SPEED to 88.0,
            Stat.CRIT_CHANCE to 0.12,
            Stat.CRIT_DAMAGE to 1.40
        ),
        statsPerLevel = mapOf(
            Stat.HP to 8.0,
            Stat.ATK to 3.5,
            Stat.DEF to 2.0
        ),
        tags = setOf("gambler", "economy", "scaling", "support"),
        abilities = listOf(
            Ability(
                name = "Geração de Riqueza",
                description = "Gera 1 moeda por turno. Se houver pelo menos 1 aliado da facção gambler, gera 2. A cada 10 moedas acumuladas, gera +1 moeda adicional.",
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
                description = "Ganha bônus de ATK, DEF, SPEED e CRIT DMG conforme as moedas do time acumulam.",
                type = AbilityType.PASSIVE,
                trigger = AbilityTrigger.OnTurnStart,
                effects = listOf(
                    Effect.BuffStatByTeamCoins(stat = Stat.ATK, valuePerStack = 3.0, coinsPerStack = 10),
                    Effect.BuffStatByTeamCoins(stat = Stat.DEF, valuePerStack = 2.0, coinsPerStack = 10),
                    Effect.BuffStatByTeamCoins(stat = Stat.SPEED, valuePerStack = 1.5, coinsPerStack = 10),
                    Effect.BuffStatByTeamCoins(stat = Stat.CRIT_DAMAGE, valuePerStack = 0.05, coinsPerStack = 10)
                )
            )
        )
    )

    private val lumina = CardDefinition(
        id = "LUMINA",
        name = "Lumina",
        description = "Sacerdotisa da fé. Cura aliados todo turno, concede buffs em rotação e cresce em poder através de sua devoção.",
        type = CardType.CHARACTER,
        rarity = Rarity.EPIC,
        faction = "faith",
        baseStats = mapOf(
            Stat.HP to 660.0,
            Stat.ATK to 58.0,
            Stat.DEF to 36.0,
            Stat.SPEED to 90.0,
            Stat.CRIT_CHANCE to 0.10,
            Stat.CRIT_DAMAGE to 1.30
        ),
        statsPerLevel = mapOf(
            Stat.HP to 18.0,
            Stat.ATK to 5.0,
            Stat.DEF to 3.0
        ),
        abilities = listOf(
            Ability(
                name = "Graça Contínua",
                description = "A cada turno, Lumina cura todos os aliados vivos em 12% de seu ATK.",
                type = AbilityType.PASSIVE,
                trigger = AbilityTrigger.OnTurnStart,
                effects = listOf(
                    Effect.Custom("Heal allies 12% ATK") { self, _, state ->
                        val healAmount = (self.stats[Stat.ATK] ?: 0.0) * 0.12
                        if (healAmount <= 0) return@Custom
                        val team = state.teams.firstOrNull { it.units.contains(self) } ?: return@Custom
                        team.units.filter { it.hp > 0 }.forEach { ally ->
                            val maxHp = ally.stats[Stat.HP] ?: return@forEach
                            val before = ally.hp
                            ally.hp = (ally.hp + healAmount).coerceAtMost(maxHp)
                            val healed = ally.hp - before
                            if (healed > 0) state.combatLog += "✨ ${ally.card.name} foi curado em ${"%.1f".format(healed)} por Lumina."
                        }
                    }
                )
            ),
            Ability(
                name = "Bênção da Aurora",
                description = "A cada 3 turnos, Lumina concede a todos os aliados +20% ATK e +20% DEF temporários por 2 rodadas.",
                type = AbilityType.PASSIVE,
                trigger = AbilityTrigger.OnTurnEvery(3),
                effects = listOf(
                    Effect.Custom("Temp buff ATK+DEF 20%") { self, _, state ->
                        val atk = self.stats[Stat.ATK] ?: 0.0
                        val def = self.stats[Stat.DEF] ?: 0.0
                        val atkBuff = atk * 0.20
                        val defBuff = def * 0.20
                        val team = state.teams.firstOrNull { it.units.contains(self) } ?: return@Custom
                        team.units.filter { it.hp > 0 }.forEach { ally ->
                            ally.stats[Stat.ATK] = (ally.stats[Stat.ATK] ?: 0.0) + atkBuff
                            ally.stats[Stat.DEF] = (ally.stats[Stat.DEF] ?: 0.0) + defBuff
                            state.temporaryStatModifiers += TemporaryStatModifier(unitId = ally.id, stat = Stat.ATK, delta = atkBuff, remainingRounds = 2, source = "LUMINA_BUFF")
                            state.temporaryStatModifiers += TemporaryStatModifier(unitId = ally.id, stat = Stat.DEF, delta = defBuff, remainingRounds = 2, source = "LUMINA_BUFF")
                            state.combatLog += "🌅 ${ally.card.name} recebeu +${atkBuff.toInt()} ATK e +${defBuff.toInt()} DEF por 2 rodadas."
                        }
                    }
                )
            ),
            Ability(
                name = "Fé Crescente",
                description = "A cada 4 turnos, Lumina fortalece sua própria fé e ganha +6 ATK permanente.",
                type = AbilityType.PASSIVE,
                trigger = AbilityTrigger.OnTurnEvery(4),
                effects = listOf(
                    Effect.Custom("Self-scale ATK") { self, _, state ->
                        self.stats[Stat.ATK] = (self.stats[Stat.ATK] ?: 0.0) + 6.0
                        state.combatLog += "🙏 ${self.card.name} fortaleceu sua fé (+6 ATK)."
                    }
                )
            )
        )
    )

    // =========================================================================
    // LEGENDARY CHARACTERS
    // =========================================================================

    private val markus = CardDefinition(
        id = "MARKUS",
        name = "Markus, Mestre das Apostas",
        description = "O grande apostador. Gera moedas e aposta com elas todo turno — alto risco, alta recompensa. DEF fraca é seu preço.",
        type = CardType.CHARACTER,
        rarity = Rarity.LEGENDARY,
        faction = "markus_gang",
        baseStats = mapOf(
            Stat.HP to 760.0,
            Stat.ATK to 64.0,
            Stat.DEF to 20.0,
            Stat.SPEED to 115.0,
            Stat.CRIT_CHANCE to 0.18,
            Stat.CRIT_DAMAGE to 1.60
        ),
        statsPerLevel = mapOf(
            Stat.HP to 10.0,
            Stat.ATK to 5.5,
            Stat.DEF to 2.5,
            Stat.CRIT_CHANCE to 0.01,
            Stat.CRIT_DAMAGE to 0.02
        ),
        tags = setOf("rng", "gambler", "risk", "chaos", "scaling", "boss", "markus_gang", "malignant"),
        abilities = listOf(
            Ability(
                name = "Mestre da Mesa",
                description = "A cada turno, Markus gera 2 moedas de cassino para a equipe e aposta com elas para obter efeitos aleatórios escalados.",
                type = AbilityType.PASSIVE,
                trigger = AbilityTrigger.OnTurnStart,
                effects = listOf(
                    Effect.AddCoins(value = 2, scaleWithGangSynergy = false),
                    Effect.Random(profile = "MARKUS_GAMBLER")
                )
            )
        )
    )

    private val solarPaladin = CardDefinition(
        id = "SOLAR_PALADIN",
        name = "Paladino Solar",
        description = "Guerreiro sagrado que converte força em resistência. Lento, mas quase indestrutível em uma equipe de fé.",
        type = CardType.CHARACTER,
        rarity = Rarity.LEGENDARY,
        faction = "faith",
        baseStats = mapOf(
            Stat.HP to 900.0,
            Stat.ATK to 65.0,
            Stat.DEF to 68.0,
            Stat.SPEED to 72.0,
            Stat.CRIT_CHANCE to 0.16,
            Stat.CRIT_DAMAGE to 1.50
        ),
        statsPerLevel = mapOf(
            Stat.HP to 32.0,
            Stat.ATK to 6.0,
            Stat.DEF to 6.0
        ),
        tags = setOf("tank", "bruiser", "faith", "scaling", "sustain"),
        abilities = listOf(
            Ability(
                name = "Corpo Consagrado",
                description = "A cada turno, converte 22% do ATK em cura e 12% do ATK em DEF temporária (1 turno).",
                type = AbilityType.PASSIVE,
                trigger = AbilityTrigger.OnTurnStart,
                effects = listOf(
                    Effect.Custom("ATK to sustain") { self, _, state ->
                        val atk = self.stats[Stat.ATK] ?: 0.0
                        val heal = atk * 0.22
                        val defBonus = atk * 0.12
                        val maxHp = self.stats[Stat.HP] ?: return@Custom
                        val before = self.hp
                        self.hp = (self.hp + heal).coerceAtMost(maxHp)
                        self.stats[Stat.DEF] = (self.stats[Stat.DEF] ?: 0.0) + defBonus
                        state.temporaryStatModifiers += TemporaryStatModifier(unitId = self.id, stat = Stat.DEF, delta = defBonus, remainingRounds = 1, source = "PALADIN_CORPO")
                        val healed = self.hp - before
                        if (healed > 0) state.combatLog += "☀️ ${self.card.name} converteu força em ${"%.1f".format(healed)} de cura e +${defBonus.toInt()} DEF temporária."
                    }
                )
            ),
            Ability(
                name = "Égide da Fé",
                description = "A cada 3 turnos, cria um escudo equivalente a 100% do ATK atual.",
                type = AbilityType.PASSIVE,
                trigger = AbilityTrigger.OnTurnEvery(3),
                effects = listOf(
                    Effect.Custom("Shield from ATK") { self, _, state ->
                        val shield = self.stats[Stat.ATK] ?: 0.0
                        val maxHp = self.stats[Stat.HP] ?: return@Custom
                        val before = self.hp
                        self.hp = (self.hp + shield).coerceAtMost(maxHp)
                        val gained = self.hp - before
                        if (gained > 0) state.combatLog += "🛡️ ${self.card.name} ganhou ${gained.toInt()} de proteção divina."
                    }
                )
            ),
            Ability(
                name = "Juramento Sagrado",
                description = "Enquanto houver aliados da facção faith, o Paladino ganha DEF temporária (1 turno) e compartilha resistência com o time.",
                type = AbilityType.PASSIVE,
                trigger = AbilityTrigger.OnTurnStart,
                effects = listOf(
                    Effect.Custom("Faith synergy DEF share") { self, _, state ->
                        val team = state.teams.firstOrNull { it.units.contains(self) } ?: return@Custom
                        val faithAllies = team.units.count { it.card.faction == "faith" && it.hp > 0 }
                        if (faithAllies <= 1) return@Custom
                        val bonusDef = 6.0 * faithAllies
                        self.stats[Stat.DEF] = (self.stats[Stat.DEF] ?: 0.0) + bonusDef
                        state.temporaryStatModifiers += TemporaryStatModifier(unitId = self.id, stat = Stat.DEF, delta = bonusDef, remainingRounds = 1, source = "PALADIN_FAITH")
                        team.units.filter { it.id != self.id && it.hp > 0 }.forEach { ally ->
                            state.temporaryStatModifiers += TemporaryStatModifier(unitId = ally.id, stat = Stat.DEF, delta = bonusDef * 0.5, remainingRounds = 1, source = "PALADIN_FAITH")
                        }
                        state.combatLog += "✝️ ${self.card.name} fortaleceu o time com fé ($faithAllies aliados)."
                    }
                )
            )
        )
    )

    // =========================================================================
    // MYTHIC CHARACTERS
    // =========================================================================

    private val unleashedJuniorKnight = CardDefinition(
        id = "UNLEASHED_JUNIOR_KNIGHT",
        name = "Cavaleirinho, O Descendente de Deus",
        description = "Após sua mãe aprovar seu sonho, o Cavaleirinho treinou sem parar. Agora em sua forma final, é um guerreiro divino — de coração puro e força incomparável.",
        type = CardType.CHARACTER,
        rarity = Rarity.MYTHIC,
        faction = "faith",
        baseStats = mapOf(
            Stat.HP to 980.0,
            Stat.ATK to 115.0,
            Stat.DEF to 88.0,
            Stat.SPEED to 92.0,
            Stat.CRIT_CHANCE to 0.30,
            Stat.CRIT_DAMAGE to 2.50
        ),
        statsPerLevel = mapOf(
            Stat.HP to 22.0,
            Stat.ATK to 30.0,
            Stat.DEF to 22.0,
            Stat.SPEED to 4.0,
            Stat.CRIT_CHANCE to 0.05,
            Stat.CRIT_DAMAGE to 0.5
        ),
        tags = setOf("knight"),
        abilities = listOf(
            Ability(
                name = "Presença Inabalável",
                description = "Ao cair abaixo de 40% de vida, a determinação do Cavaleirinho o fortalece: +150% DEF e +40% ATK.",
                type = AbilityType.PASSIVE,
                trigger = AbilityTrigger.OnBellowHealth(0.40),
                effects = listOf(
                    Effect.StatIncreasePercent(stat = Stat.DEF, percent = 1.5),
                    Effect.StatIncreasePercent(stat = Stat.ATK, percent = 0.4)
                )
            ),
            Ability(
                name = "Bênção do Escudo Sagrado",
                description = "A cada 4 turnos, o escudo sagrado o abençoa, recuperando 260 HP.",
                type = AbilityType.PASSIVE,
                trigger = AbilityTrigger.OnTurnEvery(4),
                effects = listOf(Effect.Heal(value = 260.0, target = AbilityTarget.SELF))
            ),
            Ability(
                name = "Senso de Justiça",
                description = "Causa 35% de dano extra em inimigos com a tag 'malignant'.",
                type = AbilityType.PASSIVE,
                trigger = AbilityTrigger.OnAttackAgainstTag("malignant"),
                effects = listOf(Effect.DamageIncreasePercent(value = 0.35))
            ),
            Ability(
                name = "Explosão de Fogo Sagrado",
                description = "A cada 4 ataques, o fogo sagrado explode causando 22% do HP máximo do alvo como dano verdadeiro.",
                type = AbilityType.PASSIVE,
                trigger = AbilityTrigger.OnAttackEvery(4),
                effects = listOf(
                    Effect.DamageBasedOnStat(
                        stat = Stat.HP,
                        scaling = 0.22,
                        statSource = StatSource.TARGET,
                        target = AbilityTarget.ENEMY,
                        damageType = DamageType.TRUE
                    )
                )
            )
        )
    )

    // =========================================================================
    // KONO RARITY — SYSTEM CARDS (NOT OBTAINABLE VIA GACHA)
    // =========================================================================

    private val kono = CardDefinition(
        id = "KONO",
        name = "Kono",
        description = "Uma entidade do sistema. Quando entra em combate, as regras do jogo se adaptam à sua presença.",
        type = CardType.CHARACTER,
        rarity = Rarity.KONO,
        faction = "system",
        baseStats = mapOf(
            Stat.HP to 2500.0,
            Stat.ATK to 350.0,
            Stat.DEF to 190.0,
            Stat.CRIT_CHANCE to 0.50,
            Stat.CRIT_DAMAGE to 3.0,
            Stat.SPEED to 180.0
        ),
        statsPerLevel = mapOf(
            Stat.HP to 80.0,
            Stat.ATK to 28.0,
            Stat.DEF to 24.0,
            Stat.SPEED to 12.0
        ),
        tags = setOf("kono", "system", "boss"),
        abilities = listOf(
            Ability(
                name = "Resposta Paradoxal",
                description = "A cada golpe recebido, Kono distorce a realidade, se cura e fortalece.",
                type = AbilityType.PASSIVE,
                trigger = AbilityTrigger.OnDamageTaken,
                effects = listOf(
                    Effect.Heal(value = 40.0, target = AbilityTarget.SELF),
                    Effect.BuffStat(stat = Stat.ATK, value = 10.0),
                    Effect.BuffStat(stat = Stat.DEF, value = 8.0)
                )
            ),
            Ability(
                name = "Janela de Reversão",
                description = "A cada 3 turnos, Kono enfraquece inimigos e se fortalece ainda mais.",
                type = AbilityType.PASSIVE,
                trigger = AbilityTrigger.OnTurnEvery(3),
                effects = listOf(
                    Effect.Heal(value = -120.0, target = AbilityTarget.ENEMY),
                    Effect.BuffStat(stat = Stat.ATK, value = -25.0, target = AbilityTarget.ENEMY),
                    Effect.BuffStat(stat = Stat.SPEED, value = -25.0, target = AbilityTarget.ENEMY),
                    Effect.Heal(value = 120.0, target = AbilityTarget.SELF),
                    Effect.BuffStat(stat = Stat.ATK, value = 25.0),
                    Effect.BuffStat(stat = Stat.SPEED, value = 25.0)
                )
            ),
            Ability(
                name = "Ruptura de Realidade",
                description = "Ao cair abaixo de 15% de vida, Kono libera energia caótica devastadora nos inimigos. Ocorre uma única vez.",
                type = AbilityType.PASSIVE,
                trigger = AbilityTrigger.OnBellowHealth(0.15),
                once = true,
                effects = listOf(
                    Effect.Damage(value = 1000.0, target = AbilityTarget.ALL_ENEMIES),
                    Effect.BuffStat(stat = Stat.ATK, value = -30.0, target = AbilityTarget.ALL_ENEMIES),
                    Effect.BuffStat(stat = Stat.SPEED, value = -20.0, target = AbilityTarget.ALL_ENEMIES)
                )
            ),
            Ability(
                name = "Quebra de Código",
                description = "Ao morrer, Kono tenta levar o campo de batalha junto consigo.",
                type = AbilityType.PASSIVE,
                trigger = AbilityTrigger.OnDeath,
                once = true,
                effects = listOf(
                    Effect.Heal(value = 5000.0, target = AbilityTarget.SELF),
                    Effect.Damage(value = 1500.0, target = AbilityTarget.ALL_ENEMIES)
                )
            )
        )
    )

    private val dummy = CardDefinition(
        id = "DUMMY",
        name = "Boneco de Treino",
        description = "Um alvo inofensivo para testar builds. Após 25 turnos, encerra o combate.",
        type = CardType.CHARACTER,
        rarity = Rarity.KONO,
        baseStats = mapOf(
            Stat.HP to Double.MAX_VALUE,
            Stat.ATK to 0.0,
            Stat.DEF to 0.0,
            Stat.SPEED to 0.0
        ),
        statsPerLevel = emptyMap(),
        tags = emptySet(),
        abilities = listOf(
            Ability(
                name = "Limite de Treino",
                description = "Executa todos os inimigos após 25 turnos.",
                type = AbilityType.PASSIVE,
                trigger = AbilityTrigger.OnTurnEvery(25),
                effects = listOf(Effect.ExecuteBellowHealth(threshold = 1.0, target = AbilityTarget.ALL_ENEMIES))
            )
        )
    )

    // =========================================================================
    // COMMON EQUIPMENT
    // =========================================================================

    private val woodenSword = CardDefinition(
        id = "WOODEN_SWORD",
        name = "Espada de Madeira",
        description = "Uma espada simples para iniciantes. Sem truques.",
        type = CardType.EQUIPMENT,
        rarity = Rarity.COMMON,
        slot = EquipmentSlot.WEAPON,
        baseStats = mapOf(Stat.ATK to 14.0),
        statsPerLevel = mapOf(Stat.ATK to 2.5),
        tags = setOf("starter"),
        abilities = emptyList()
    )

    private val dagger = CardDefinition(
        id = "DAGGER",
        name = "Adaga",
        description = "Pequena e ágil. Oferece um toque de velocidade junto com dano modesto.",
        type = CardType.EQUIPMENT,
        rarity = Rarity.COMMON,
        slot = EquipmentSlot.WEAPON,
        baseStats = mapOf(
            Stat.ATK to 8.0,
            Stat.SPEED to 5.0
        ),
        statsPerLevel = mapOf(
            Stat.ATK to 1.5,
            Stat.SPEED to 1.0
        ),
        tags = setOf("starter"),
        abilities = emptyList()
    )

    private val ironArmor = CardDefinition(
        id = "IRON_ARMOR",
        name = "Armadura de Ferro Simples",
        description = "Armadura básica. Oferece proteção decente sem penalizar a velocidade.",
        type = CardType.EQUIPMENT,
        rarity = Rarity.COMMON,
        slot = EquipmentSlot.ARMOR,
        baseStats = mapOf(Stat.DEF to 26.0),
        statsPerLevel = mapOf(Stat.DEF to 3.5),
        tags = setOf("armor", "defense"),
        abilities = emptyList()
    )

    // =========================================================================
    // RARE EQUIPMENT
    // =========================================================================

    private val ironSword = CardDefinition(
        id = "IRON_SWORD",
        name = "Espada de Ferro",
        description = "Dano sólido com um leve custo de velocidade. Bom passo além da madeira.",
        type = CardType.EQUIPMENT,
        rarity = Rarity.RARE,
        slot = EquipmentSlot.WEAPON,
        baseStats = mapOf(
            Stat.ATK to 28.0,
            Stat.SPEED to -4.0
        ),
        statsPerLevel = mapOf(Stat.ATK to 3.5),
        tags = setOf("weapon", "starter", "iron"),
        abilities = emptyList()
    )

    private val ironShield = CardDefinition(
        id = "IRON_SHIELD",
        name = "Escudo de Ferro",
        description = "Troca ofensa por resistência. Para quem quer sobreviver mais.",
        type = CardType.EQUIPMENT,
        rarity = Rarity.RARE,
        slot = EquipmentSlot.SECONDARY,
        baseStats = mapOf(
            Stat.DEF to 35.0,
            Stat.HP to 70.0,
            Stat.ATK to -8.0
        ),
        statsPerLevel = mapOf(
            Stat.DEF to 3.0,
            Stat.HP to 12.0
        ),
        tags = setOf("shield", "starter", "iron"),
        abilities = emptyList()
    )

    private val heavyIronArmor = CardDefinition(
        id = "HEAVY_IRON_ARMOR",
        name = "Armadura de Ferro Pesada",
        description = "Proteção robusta com custo significativo de velocidade. Funciona melhor em unidades naturalmente lentas.",
        type = CardType.EQUIPMENT,
        rarity = Rarity.RARE,
        slot = EquipmentSlot.ARMOR,
        baseStats = mapOf(
            Stat.DEF to 50.0,
            Stat.SPEED to -20.0
        ),
        statsPerLevel = mapOf(Stat.DEF to 5.0),
        tags = setOf("heavy", "armor", "defense"),
        abilities = emptyList()
    )

    private val katana = CardDefinition(
        id = "KATANA",
        name = "Katana Simples",
        description = "Leve e rápida. Sacrifica um pouco de DEF para ganhar velocidade, crítico e um golpe extra em cada ataque.",
        type = CardType.EQUIPMENT,
        rarity = Rarity.RARE,
        slot = EquipmentSlot.WEAPON,
        baseStats = mapOf(
            Stat.ATK to 15.0,
            Stat.SPEED to 7.0,
            Stat.CRIT_CHANCE to 0.08,
            Stat.DEF to -6.0
        ),
        statsPerLevel = mapOf(
            Stat.ATK to 1.5,
            Stat.SPEED to 0.8,
            Stat.CRIT_CHANCE to 0.01
        ),
        tags = setOf("iron", "speed", "crit"),
        abilities = listOf(
            Ability(
                name = "Corte Rápido",
                description = "Cada ataque com a katana causa 5 de dano adicional ao alvo.",
                type = AbilityType.PASSIVE,
                trigger = AbilityTrigger.OnAttack,
                effects = listOf(Effect.Damage(value = 5.0, target = AbilityTarget.ENEMY))
            )
        )
    )

    private val vampireRing = CardDefinition(
        id = "VAMPIRE_RING",
        name = "Anel de Vampiro",
        description = "Um anel tomado de um vampiro. Rouba uma fração de vida a cada ataque.",
        type = CardType.EQUIPMENT,
        rarity = Rarity.RARE,
        slot = EquipmentSlot.TRINKET,
        baseStats = mapOf(Stat.LIFESTEAL to 0.12),
        statsPerLevel = mapOf(Stat.LIFESTEAL to 0.015),
        tags = setOf("lifesteal", "vampire"),
        abilities = emptyList()
    )

    // =========================================================================
    // EPIC EQUIPMENT
    // =========================================================================

    private val polishedKatana = CardDefinition(
        id = "POLISHED_KATANA",
        name = "Katana Polida",
        description = "Refinada, confiável e cortante. Versão superior da katana com mais ATK e crítico.",
        type = CardType.EQUIPMENT,
        rarity = Rarity.EPIC,
        slot = EquipmentSlot.WEAPON,
        baseStats = mapOf(
            Stat.ATK to 20.0,
            Stat.CRIT_CHANCE to 0.14,
            Stat.SPEED to 12.0
        ),
        statsPerLevel = mapOf(
            Stat.ATK to 2.5,
            Stat.SPEED to 1.0,
            Stat.CRIT_CHANCE to 0.015
        ),
        tags = setOf("steel", "speed", "crit", "polished"),
        abilities = listOf(
            Ability(
                name = "Corte Rápido",
                description = "Cada ataque com a katana polida causa 10 de dano adicional ao alvo.",
                type = AbilityType.PASSIVE,
                trigger = AbilityTrigger.OnAttack,
                effects = listOf(Effect.Damage(value = 10.0, target = AbilityTarget.ENEMY))
            )
        )
    )

    private val vampireCore = CardDefinition(
        id = "VAMPIRE_CORE",
        name = "Núcleo Vampírico",
        description = "Um núcleo de energia sombria. Confere ATK e roubo de vida substancial.",
        type = CardType.EQUIPMENT,
        rarity = Rarity.EPIC,
        slot = EquipmentSlot.TRINKET,
        faction = "vampire",
        baseStats = mapOf(
            Stat.ATK to 24.0,
            Stat.LIFESTEAL to 0.20
        ),
        statsPerLevel = mapOf(
            Stat.ATK to 3.0,
            Stat.LIFESTEAL to 0.015
        ),
        tags = setOf("vampire", "lifesteal"),
        abilities = emptyList()
    )

    private val greatsword = CardDefinition(
        id = "GREATSWORD",
        name = "Espada Grande",
        description = "Enorme e devastadora. Quem for forte o suficiente para erguê-la será recompensado com poder bruto.",
        type = CardType.EQUIPMENT,
        rarity = Rarity.EPIC,
        slot = EquipmentSlot.WEAPON,
        baseStats = mapOf(
            Stat.ATK to 44.0,
            Stat.SPEED to -20.0
        ),
        statsPerLevel = mapOf(Stat.ATK to 8.0),
        tags = setOf("iron", "great", "heavy"),
        abilities = listOf(
            Ability(
                name = "Golpes Pesados",
                description = "O peso da espada recompensa o portador com +22% de ATK permanente no início da batalha.",
                type = AbilityType.PASSIVE,
                trigger = AbilityTrigger.OnBattleStart,
                effects = listOf(Effect.StatIncreasePercent(stat = Stat.ATK, percent = 0.22))
            ),
            Ability(
                name = "Golpe Colossal",
                description = "A cada 3 ataques, desfere um golpe colossal que causa 120% do ATK atual como dano.",
                type = AbilityType.PASSIVE,
                trigger = AbilityTrigger.OnAttackEvery(3),
                effects = listOf(
                    Effect.DamageBasedOnStat(stat = Stat.ATK, scaling = 1.2, statSource = StatSource.SELF)
                )
            )
        )
    )

    private val gamblerCharm = CardDefinition(
        id = "GAMBLER_CHARM",
        name = "Talismã do Apostador",
        description = "Um artefato caótico. Aumenta o potencial crítico e desencadeia efeitos aleatórios a cada turno.",
        type = CardType.EQUIPMENT,
        rarity = Rarity.EPIC,
        slot = EquipmentSlot.SECONDARY,
        baseStats = mapOf(
            Stat.CRIT_CHANCE to 0.09,
            Stat.CRIT_DAMAGE to 0.22
        ),
        statsPerLevel = mapOf(
            Stat.CRIT_CHANCE to 0.015,
            Stat.CRIT_DAMAGE to 0.05
        ),
        tags = setOf("gambler", "chaos"),
        abilities = listOf(
            Ability(
                name = "Roleta Caótica",
                description = "A cada turno, a roleta caótica gira e pode curar, causar dano, ou modificar atributos.",
                type = AbilityType.PASSIVE,
                trigger = AbilityTrigger.OnTurnStart,
                effects = listOf(Effect.Random(profile = "GAMBLER_CHARM"))
            )
        )
    )

    private val devotionStaff = CardDefinition(
        id = "DEVOTION_STAFF",
        name = "Cetro da Devoção",
        description = "Cetro sagrado que amplifica a fé do portador. Cura e protege aliados — especialmente os da fé.",
        type = CardType.EQUIPMENT,
        rarity = Rarity.EPIC,
        slot = EquipmentSlot.WEAPON,
        faction = "faith",
        baseStats = mapOf(
            Stat.ATK to 44.0,
            Stat.SPEED to -14.0
        ),
        statsPerLevel = mapOf(Stat.ATK to 6.0),
        tags = setOf("faith", "support", "scaling"),
        abilities = listOf(
            Ability(
                name = "Conversão Divina",
                description = "A cada turno, cura todos os aliados: membros da fé recebem 11% do ATK, os demais 6%.",
                type = AbilityType.PASSIVE,
                trigger = AbilityTrigger.OnTurnStart,
                effects = listOf(
                    Effect.Custom("Faith heal scaling") { self, _, state ->
                        val atk = self.stats[Stat.ATK] ?: 0.0
                        val team = state.teams.firstOrNull { it.units.contains(self) } ?: return@Custom
                        team.units.filter { it.hp > 0 }.forEach { ally ->
                            val isFaith = ally.card.faction == "faith"
                            val healAmount = atk * (if (isFaith) 0.11 else 0.06)
                            val maxHp = ally.stats[Stat.HP] ?: return@forEach
                            val before = ally.hp
                            ally.hp = (ally.hp + healAmount).coerceAtMost(maxHp)
                            val healed = ally.hp - before
                            if (healed > 0) state.combatLog += "✨ ${ally.card.name} recebeu ${"%.1f".format(healed)} de cura (${if (isFaith) "fé" else "normal"})."
                        }
                    }
                )
            ),
            Ability(
                name = "Proteção Sagrada",
                description = "A cada 2 turnos, concede escudos temporários: membros da fé recebem 16% do ATK, os demais 8%.",
                type = AbilityType.PASSIVE,
                trigger = AbilityTrigger.OnTurnEvery(2),
                effects = listOf(
                    Effect.Custom("Faith shield") { self, _, state ->
                        val atk = self.stats[Stat.ATK] ?: 0.0
                        val team = state.teams.firstOrNull { it.units.contains(self) } ?: return@Custom
                        team.units.filter { it.hp > 0 }.forEach { ally ->
                            val isFaith = ally.card.faction == "faith"
                            val shieldValue = atk * (if (isFaith) 0.16 else 0.08)
                            state.temporaryStatModifiers += TemporaryStatModifier(unitId = ally.id, stat = Stat.HP, delta = shieldValue, remainingRounds = 1, source = "DEVOTION_SHIELD")
                            state.combatLog += "🛡️ ${ally.card.name} recebeu ${shieldValue.toInt()} de escudo (${if (isFaith) "fé" else "normal"})."
                        }
                    }
                )
            )
        )
    )

    // =========================================================================
    // LEGENDARY EQUIPMENT
    // =========================================================================

    // Easter egg item. Locks crit chance to 8% regardless of other sources, triples crit damage multiplier.
    private val critfish = CardDefinition(
        id = "CRITFISH",
        name = "Critfish",
        description = "Um peixe misterioso de origem desconhecida. Raramente acerta um crítico — mas quando acerta, a realidade treme.",
        type = CardType.EQUIPMENT,
        rarity = Rarity.LEGENDARY,
        slot = EquipmentSlot.SECONDARY,
        baseStats = mapOf(
            Stat.ATK to 10.0
        ),
        statsPerLevel = mapOf(
            Stat.ATK to 2.0
        ),
        tags = setOf("critfish", "easter-egg", "critical"),
        abilities = listOf(
            Ability(
                name = "Natureza do Peixe",
                description = "Trava a chance de crítico em 8%, independente de qualquer fonte. Em troca, triplica o multiplicador de dano crítico.",
                type = AbilityType.PASSIVE,
                trigger = AbilityTrigger.OnBattleStart,
                once = true,
                effects = listOf(
                    Effect.Custom("CRITFISH_LOCK") { self, _, state ->
                        val currentCritDmg = self.stats[Stat.CRIT_DAMAGE] ?: 1.5
                        self.stats[Stat.CRIT_CHANCE] = 0.08
                        self.stats[Stat.CRIT_DAMAGE] = currentCritDmg * 3.0
                        state.combatLog += "🐟 Critfish travou o crítico em 8% e triplicou o multiplicador (${currentCritDmg}x → ${"%.2f".format(currentCritDmg * 3.0)}x)."
                    }
                )
            )
        )
    )

    private val demonHunterCrossbow = CardDefinition(
        id = "DEMON_HUNTER_CROSSBOW",
        name = "Besta da Caçadora de Demônios",
        description = "A cada 3 ataques, dispara uma flecha de prata que causa dano verdadeiro com base no HP máximo do alvo. Excelente contra alvos com muita vida.",
        type = CardType.EQUIPMENT,
        rarity = Rarity.LEGENDARY,
        slot = EquipmentSlot.WEAPON,
        baseStats = mapOf(
            Stat.SPEED to 18.0,
            Stat.ATK to 18.0
        ),
        statsPerLevel = mapOf(Stat.SPEED to 4.0),
        tags = setOf("speed"),
        abilities = listOf(
            Ability(
                name = "Flecha de Prata",
                description = "A cada 3 ataques, dispara uma flecha que causa 7% do HP máximo do alvo como dano verdadeiro.",
                type = AbilityType.PASSIVE,
                trigger = AbilityTrigger.OnAttackEvery(3),
                effects = listOf(
                    Effect.DamageBasedOnStat(
                        stat = Stat.HP,
                        scaling = 0.07,
                        statSource = StatSource.TARGET,
                        target = AbilityTarget.ENEMY,
                        damageType = DamageType.TRUE
                    )
                )
            )
        )
    )

    private val allInEmblem = CardDefinition(
        id = "ALL_IN_EMBLEM",
        name = "Emblema do All-In",
        description = "Arma exclusiva de Markus. Cada ataque causa dano extra e gera moedas de cassino para apostas.",
        type = CardType.EQUIPMENT,
        rarity = Rarity.LEGENDARY,
        slot = EquipmentSlot.SECONDARY,
        faction = "markus_gang",
        baseStats = mapOf(
            Stat.ATK to 24.0,
            Stat.CRIT_CHANCE to 0.12
        ),
        statsPerLevel = mapOf(
            Stat.ATK to 5.0,
            Stat.CRIT_CHANCE to 0.015
        ),
        tags = setOf("gambler", "markus", "signature", "weapon", "risk", "chaos"),
        abilities = listOf(
            Ability(
                name = "Rajada All-In",
                description = "Cada ataque causa 18 de dano extra pela ousadia do portador.",
                type = AbilityType.PASSIVE,
                trigger = AbilityTrigger.OnAttack,
                effects = listOf(Effect.Damage(value = 18.0, target = AbilityTarget.ENEMY))
            ),
            Ability(
                name = "A Casa Sempre Ganha",
                description = "A cada turno, gera 1 moeda de cassino para a equipe e aposta para obter efeitos aleatórios.",
                type = AbilityType.PASSIVE,
                trigger = AbilityTrigger.OnTurnStart,
                effects = listOf(
                    Effect.AddCoins(value = 1, scaleWithGangSynergy = false),
                    Effect.Random(profile = "MARKUS_GAMBLER")
                )
            )
        )
    )

    // =========================================================================
    // MYTHIC EQUIPMENT
    // =========================================================================

    private val undefined = CardDefinition(
        id = "UNDEFINED",
        name = "undefined",
        description = "erro: undefined não é um item",
        type = CardType.EQUIPMENT,
        rarity = Rarity.MYTHIC,
        slot = EquipmentSlot.TRINKET,
        baseStats = mapOf(
            Stat.HP to 55.0,
            Stat.DEF to 55.0,
            Stat.SPEED to 6.0,
            Stat.ATK to 55.0,
            Stat.LIFESTEAL to 0.50,
            Stat.CRIT_CHANCE to 0.05,
            Stat.CRIT_DAMAGE to 1.50
        ),
        statsPerLevel = mapOf(
            Stat.HP to 25.0,
            Stat.DEF to 25.0,
            Stat.SPEED to 5.0,
            Stat.ATK to 25.0,
            Stat.LIFESTEAL to 0.025,
            Stat.CRIT_CHANCE to 0.025,
            Stat.CRIT_DAMAGE to 0.5
        ),
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
    )

    private val sunGodGreatsword = CardDefinition(
        id = "SUN_GOD_GREATSWORD",
        name = "A Grande Espada do Deus Sol",
        description = "Lâmina sagrada que cresce a cada golpe dado ou recebido. Sacrifica DEF e SPEED por escalada de ATK extrema e execução solar.",
        type = CardType.EQUIPMENT,
        rarity = Rarity.MYTHIC,
        slot = EquipmentSlot.WEAPON,
        faction = "god",
        baseStats = mapOf(
            Stat.ATK to 80.0,
            Stat.CRIT_CHANCE to 0.18,
            Stat.CRIT_DAMAGE to 0.60,
            Stat.SPEED to -25.0,
            Stat.DEF to -25.0
        ),
        statsPerLevel = mapOf(
            Stat.ATK to 32.0,
            Stat.CRIT_CHANCE to 0.05,
            Stat.CRIT_DAMAGE to 0.15,
            Stat.SPEED to 0.5
        ),
        abilities = listOf(
            Ability(
                name = "Fé Ardente",
                description = "Cada ataque fortalece a fé permanentemente: +3.5 ATK fixo.",
                type = AbilityType.PASSIVE,
                trigger = AbilityTrigger.OnAttack,
                effects = listOf(
                    Effect.BuffStat(stat = Stat.ATK, value = 3.5)
                )
            ),
            Ability(
                name = "Chama Divina",
                description = "Cada ataque aplica a chama divina ao alvo, causando 20% do ATK atual como dano verdadeiro adicional.",
                type = AbilityType.PASSIVE,
                trigger = AbilityTrigger.OnAttack,
                effects = listOf(
                    Effect.Custom("True damage 20% ATK") { self, unit, state ->
                        val damage = (self.stats[Stat.ATK] ?: 0.0) * 0.20
                        if (damage <= 0 || unit == null) return@Custom
                        state.combatLog += "🔥 A chama divina penetra em ${unit.card.name} causando ${"%.1f".format(damage)} de dano verdadeiro!"
                        unit.hp -= damage
                    }
                )
            ),
            Ability(
                name = "Provação Divina",
                description = "Ao receber dano, a fé é testada e o ATK cresce em +5 permanentemente.",
                type = AbilityType.PASSIVE,
                trigger = AbilityTrigger.OnDamageTaken,
                effects = listOf(Effect.BuffStat(stat = Stat.ATK, value = 5.0))
            ),
            Ability(
                name = "Chama do Escolhido",
                description = "Abaixo de 40% de vida, o poder do deus sol explode: +65% ATK e +22% roubo de vida.",
                type = AbilityType.PASSIVE,
                trigger = AbilityTrigger.OnBellowHealth(0.40),
                effects = listOf(
                    Effect.StatIncreasePercent(stat = Stat.ATK, percent = 0.65),
                    Effect.BuffStat(stat = Stat.LIFESTEAL, value = 0.22)
                )
            ),
            Ability(
                name = "Execução Solar",
                description = "Ao causar dano, executa inimigos abaixo de 18% de vida.",
                type = AbilityType.PASSIVE,
                trigger = AbilityTrigger.OnDamageDealt,
                effects = listOf(Effect.ExecuteBellowHealth(threshold = 0.18))
            )
        )
    )

    // =========================================================================
    // NEW — COMMON CHARACTERS
    // =========================================================================

    // Fast offensive common. Trades all durability for ATK and SPEED.
    // Max lv6: HP ~350, ATK ~59, DEF 4. EHP ~364 — the squishiest common.
    private val goblin = CardDefinition(
        id = "GOBLIN",
        name = "Goblin",
        description = "Rápido, fraco e agressivo. Ataca antes da maioria, mas cai com poucos golpes.",
        type = CardType.CHARACTER,
        rarity = Rarity.COMMON,
        baseStats = mapOf(
            Stat.HP to 330.0,
            Stat.ATK to 38.0,
            Stat.DEF to 4.0,
            Stat.SPEED to 98.0,
            Stat.CRIT_CHANCE to 0.06,
            Stat.CRIT_DAMAGE to 1.15
        ),
        statsPerLevel = mapOf(
            Stat.HP to 7.0,
            Stat.ATK to 4.5
        ),
        tags = setOf("starter", "fast"),
        abilities = emptyList()
    )

    // =========================================================================
    // NEW — RARE CHARACTERS
    // =========================================================================

    // Defensive rare. Grows harder to kill every 2 turns.
    // Max lv10: HP ~692, DEF ~91. EHP base ~1322, grows further with ability.
    private val ironGuardian = CardDefinition(
        id = "IRON_GUARDIAN",
        name = "Guardião de Ferro",
        description = "Tanque lento e sólido. Cresce em resistência a cada dois turnos — quanto mais a batalha dura, mais difícil é de derrubar.",
        type = CardType.CHARACTER,
        rarity = Rarity.RARE,
        baseStats = mapOf(
            Stat.HP to 620.0,
            Stat.ATK to 30.0,
            Stat.DEF to 55.0,
            Stat.SPEED to 68.0,
            Stat.CRIT_CHANCE to 0.06,
            Stat.CRIT_DAMAGE to 1.20
        ),
        statsPerLevel = mapOf(
            Stat.HP to 14.0,
            Stat.DEF to 6.0
        ),
        tags = setOf("tank", "defense"),
        abilities = listOf(
            Ability(
                name = "Postura Defensiva",
                description = "A cada 2 turnos, o Guardião endurece sua armadura, ganhando +3 DEF permanente.",
                type = AbilityType.PASSIVE,
                trigger = AbilityTrigger.OnTurnEvery(2),
                effects = listOf(Effect.BuffStat(stat = Stat.DEF, value = 3.0, target = AbilityTarget.SELF))
            )
        )
    )

    // =========================================================================
    // NEW — EPIC CHARACTERS
    // =========================================================================

    // Speed assassin. Virtually no DEF, very high ATK and SPEED. Executes low-HP targets.
    // Max lv14: ATK ~120, SPEED ~164 (1.64 avg attacks/turn), HP ~396. EHP ~428.
    private val shadow = CardDefinition(
        id = "SHADOW",
        name = "Sombra",
        description = "Assassina ultrarrápida. Não aguenta punição, mas dificilmente dá chance para o inimigo reagir.",
        type = CardType.CHARACTER,
        rarity = Rarity.EPIC,
        baseStats = mapOf(
            Stat.HP to 370.0,
            Stat.ATK to 68.0,
            Stat.DEF to 8.0,
            Stat.SPEED to 138.0,
            Stat.CRIT_CHANCE to 0.22,
            Stat.CRIT_DAMAGE to 1.55
        ),
        statsPerLevel = mapOf(
            Stat.HP to 5.0,
            Stat.ATK to 6.0,
            Stat.SPEED to 2.5,
            Stat.CRIT_CHANCE to 0.01
        ),
        tags = setOf("fast", "assassin"),
        abilities = listOf(
            Ability(
                name = "Abertura",
                description = "No início da batalha, Sombra aproveita o elemento surpresa e ganha +20% de ATK permanente.",
                type = AbilityType.PASSIVE,
                trigger = AbilityTrigger.OnBattleStart,
                effects = listOf(Effect.StatIncreasePercent(stat = Stat.ATK, percent = 0.20))
            ),
            Ability(
                name = "Lâmina Sombria",
                description = "Ao causar dano, executa alvos com menos de 15% de vida.",
                type = AbilityType.PASSIVE,
                trigger = AbilityTrigger.OnDamageDealt,
                effects = listOf(Effect.ExecuteBellowHealth(threshold = 0.15))
            )
        )
    )

    // Rage tank. Grows stronger with every hit received. High risk/reward.
    // Max lv14: HP ~658, ATK ~91. After 10 hits of rage: +80 ATK = 171 total.
    private val berserker = CardDefinition(
        id = "BERSERKER",
        name = "Berserker",
        description = "Quanto mais apanha, mais perigoso fica. Fraco de início, devastador no final.",
        type = CardType.CHARACTER,
        rarity = Rarity.EPIC,
        baseStats = mapOf(
            Stat.HP to 580.0,
            Stat.ATK to 52.0,
            Stat.DEF to 14.0,
            Stat.SPEED to 96.0,
            Stat.CRIT_CHANCE to 0.15,
            Stat.CRIT_DAMAGE to 1.40
        ),
        statsPerLevel = mapOf(
            Stat.HP to 10.0,
            Stat.ATK to 4.5,
            Stat.SPEED to 1.5
        ),
        tags = setOf("rage", "bruiser"),
        abilities = listOf(
            Ability(
                name = "Fúria",
                description = "A cada golpe recebido, o Berserker entra em fúria e ganha +5 de ATK permanente.",
                type = AbilityType.PASSIVE,
                trigger = AbilityTrigger.OnDamageTaken,
                effects = listOf(Effect.BuffStat(stat = Stat.ATK, value = 5.0, target = AbilityTarget.SELF))
            ),
            Ability(
                name = "Última Resistência",
                description = "Ao cair abaixo de 50% de vida, a raiva explode: +35% ATK e +20% SPEED.",
                type = AbilityType.PASSIVE,
                trigger = AbilityTrigger.OnBellowHealth(0.50),
                effects = listOf(
                    Effect.StatIncreasePercent(stat = Stat.ATK, percent = 0.35),
                    Effect.StatIncreasePercent(stat = Stat.SPEED, percent = 0.20)
                )
            )
        )
    )

    // =========================================================================
    // NEW — LEGENDARY CHARACTERS
    // =========================================================================

    // Counter tank. Extremely high DEF. Retaliates with magic AoE on every hit received.
    // Max lv18: HP ~1095, DEF ~170 (229 after Pele de Pedra). Counter: DEF*0.20 magic AoE per hit.
    private val ironGargoyle = CardDefinition(
        id = "IRON_GARGOYLE",
        name = "Gárgula de Ferro",
        description = "Um guardião de pedra. Praticamente indestrutível — e cada golpe recebido devolve energia sombria a todos os inimigos.",
        type = CardType.CHARACTER,
        rarity = Rarity.LEGENDARY,
        baseStats = mapOf(
            Stat.HP to 840.0,
            Stat.ATK to 46.0,
            Stat.DEF to 85.0,
            Stat.SPEED to 62.0,
            Stat.CRIT_CHANCE to 0.08,
            Stat.CRIT_DAMAGE to 1.25
        ),
        statsPerLevel = mapOf(
            Stat.HP to 22.0,
            Stat.DEF to 7.0,
            Stat.ATK to 2.5
        ),
        tags = setOf("tank", "defense", "counter"),
        abilities = listOf(
            Ability(
                name = "Pele de Pedra",
                description = "No início da batalha, a pele da gárgula endurece completamente, ganhando +35% de DEF.",
                type = AbilityType.PASSIVE,
                trigger = AbilityTrigger.OnBattleStart,
                effects = listOf(Effect.StatIncreasePercent(stat = Stat.DEF, percent = 0.35))
            ),
            Ability(
                name = "Regeneração Lenta",
                description = "A cada 2 turnos, a pedra se reconstrói sozinha, curando 40 HP.",
                type = AbilityType.PASSIVE,
                trigger = AbilityTrigger.OnTurnEvery(2),
                effects = listOf(Effect.Heal(value = 40.0, target = AbilityTarget.SELF))
            ),
            Ability(
                name = "Contra-Ataque de Pedra",
                description = "Ao receber qualquer dano, emite uma onda de pedra que causa 20% da sua DEF atual como dano mágico a todos os inimigos.",
                type = AbilityType.PASSIVE,
                trigger = AbilityTrigger.OnDamageTaken,
                effects = listOf(
                    Effect.DamageBasedOnStat(
                        stat = Stat.DEF,
                        scaling = 0.20,
                        statSource = StatSource.SELF,
                        target = AbilityTarget.ALL_ENEMIES,
                        damageType = DamageType.MAGIC
                    )
                )
            )
        )
    )

    // Pure glass cannon mage. Lowest DEF of all legendaries. AoE magic every 2 turns + huge burst when low.
    // Max lv18: ATK ~190, DEF 15 (unchanged). AoE = 190*0.60 = 114 magic to all enemies every 2 turns.
    private val voidMage = CardDefinition(
        id = "VOID_MAGE",
        name = "Mago do Vazio",
        description = "Poder mágico puro e devastador. Praticamente sem defesa — um único golpe bem dado pode encerrá-lo.",
        type = CardType.CHARACTER,
        rarity = Rarity.LEGENDARY,
        baseStats = mapOf(
            Stat.HP to 520.0,
            Stat.ATK to 68.0,
            Stat.DEF to 15.0,
            Stat.SPEED to 102.0,
            Stat.CRIT_CHANCE to 0.18,
            Stat.CRIT_DAMAGE to 1.5
        ),
        statsPerLevel = mapOf(
            Stat.HP to 10.0,
            Stat.ATK to 8.0,
            Stat.SPEED to 2.5,
            Stat.CRIT_CHANCE to 0.01
        ),
        tags = setOf("mage", "aoe", "glass-cannon"),
        abilities = listOf(
            Ability(
                name = "Explosão de Vazio",
                description = "A cada 2 turnos, o Mago libera uma explosão de energia do vazio causando 60% do ATK como dano mágico a todos os inimigos.",
                type = AbilityType.PASSIVE,
                trigger = AbilityTrigger.OnTurnEvery(2),
                effects = listOf(
                    Effect.DamageBasedOnStat(
                        stat = Stat.ATK,
                        scaling = 0.60,
                        statSource = StatSource.SELF,
                        target = AbilityTarget.ALL_ENEMIES,
                        damageType = DamageType.MAGIC
                    )
                )
            ),
            Ability(
                name = "Convergência do Vazio",
                description = "Ao cair abaixo de 30% de vida, o vazio converge em poder máximo: +55% ATK e +30% SPEED.",
                type = AbilityType.PASSIVE,
                trigger = AbilityTrigger.OnBellowHealth(0.30),
                effects = listOf(
                    Effect.StatIncreasePercent(stat = Stat.ATK, percent = 0.55),
                    Effect.StatIncreasePercent(stat = Stat.SPEED, percent = 0.30)
                )
            )
        )
    )

    // =========================================================================
    // NEW — RARE EQUIPMENT
    // =========================================================================

    // Pure speed item. No abilities. -20 HP as trade-off.
    // Max lv10: SPEED +48, HP -20.
    private val quickBoots = CardDefinition(
        id = "QUICK_BOOTS",
        name = "Botas Velozes",
        description = "Botas leves como vento. Concede um bônus expressivo de velocidade ao custo de um pouco de resistência.",
        type = CardType.EQUIPMENT,
        rarity = Rarity.RARE,
        slot = EquipmentSlot.BOOTS,
        baseStats = mapOf(
            Stat.SPEED to 25.0,
            Stat.HP to -10.0
        ),
        statsPerLevel = mapOf(Stat.SPEED to 3.5),
        tags = setOf("speed", "light"),
        abilities = emptyList()
    )

    // Magic AoE item. Sacrifices DEF for periodic magic blasts.
    // Max lv10: ATK +33, -8 DEF. Pulse: 30 magic to ALL enemies every 3 turns.
    private val boneStaff = CardDefinition(
        id = "BONE_STAFF",
        name = "Cajado de Osso",
        description = "Um cajado arcano talhado de ossos. Pulsa energia sombria a cada três turnos, atingindo todos os inimigos.",
        type = CardType.EQUIPMENT,
        rarity = Rarity.RARE,
        slot = EquipmentSlot.WEAPON,
        baseStats = mapOf(
            Stat.ATK to 20.0,
            Stat.DEF to -8.0
        ),
        statsPerLevel = mapOf(Stat.ATK to 2.5),
        tags = setOf("magic", "aoe"),
        abilities = listOf(
            Ability(
                name = "Pulso Sombrio",
                description = "A cada 3 turnos, o cajado emite um pulso de energia sombria causando 30 de dano mágico a todos os inimigos.",
                type = AbilityType.PASSIVE,
                trigger = AbilityTrigger.OnTurnEvery(3),
                effects = listOf(
                    Effect.Damage(value = 30.0, target = AbilityTarget.ALL_ENEMIES, damageType = DamageType.MAGIC)
                )
            )
        )
    )

    // =========================================================================
    // NEW — EPIC EQUIPMENT
    // =========================================================================

    // Thorns-on-hit. Every time the wearer is struck, deals magic AoE to all enemies.
    // Max lv14: DEF +84, HP +125. Combined DEF*0.25 magic AoE per hit.
    private val thornmail = CardDefinition(
        id = "THORNMAIL",
        name = "Malha de Espinhos",
        description = "Cada golpe recebido devolve energia mágica cortante a todos os inimigos. Quanto maior a DEF do portador, maior o contra-ataque.",
        type = CardType.EQUIPMENT,
        rarity = Rarity.EPIC,
        slot = EquipmentSlot.ARMOR,
        baseStats = mapOf(
            Stat.HP to 60.0,
            Stat.DEF to 48.0,
            Stat.ATK to -16.0,
            Stat.SPEED to -12.0
        ),
        statsPerLevel = mapOf(
            Stat.DEF to 11.0,
            Stat.HP to 16.0
        ),
        tags = setOf("defense", "counter", "thorns"),
        abilities = listOf(
            Ability(
                name = "Espinhos de Ferro",
                description = "Ao receber dano, emite espinhos mágicos causando 25% da DEF atual como dano mágico a todos os inimigos.",
                type = AbilityType.PASSIVE,
                trigger = AbilityTrigger.OnDamageTaken,
                effects = listOf(
                    Effect.DamageBasedOnStat(
                        stat = Stat.DEF,
                        scaling = 0.25,
                        statSource = StatSource.SELF,
                        target = AbilityTarget.ALL_ENEMIES,
                        damageType = DamageType.MAGIC
                    )
                )
            )
        )
    )

    // Sustain item. Lifesteal + periodic heal. No ATK contribution.
    // Max lv14: HP +184, LIFESTEAL 0.25. Every 3 turns: +50 HP heal.
    private val elixirVial = CardDefinition(
        id = "ELIXIR_VIAL",
        name = "Vial de Elixir",
        description = "Um frasco de elixir que regenera o portador ao longo do combate.",
        type = CardType.EQUIPMENT,
        rarity = Rarity.EPIC,
        slot = EquipmentSlot.TRINKET,
        baseStats = mapOf(
            Stat.HP to 80.0,
            Stat.LIFESTEAL to 0.12
        ),
        statsPerLevel = mapOf(
            Stat.HP to 24.0,
            Stat.LIFESTEAL to 0.015
        ),
        tags = setOf("sustain", "healing"),
        abilities = listOf(
            Ability(
                name = "Poção Vital",
                description = "A cada 3 turnos, cura 50 HP.",
                type = AbilityType.PASSIVE,
                trigger = AbilityTrigger.OnTurnEvery(3),
                effects = listOf(
                    Effect.Heal(value = 50.0, target = AbilityTarget.SELF)
                )
            )
        )
    )

    // =========================================================================
    // NEW — LEGENDARY EQUIPMENT
    // =========================================================================

    // Armor shredder. Percentage-based DEF shred — scales with enemy armor, cap ~28% total.
    // Ruptura: -15% DEF opener to all enemies. Corrosivo: -5% per hit, 3 stacks max per enemy.
    private val siegebreaker = CardDefinition(
        id = "SIEGEBREAKER",
        name = "Quebra-Muralhas",
        description = "Uma arma de cerco que corrói a armadura inimiga proporcionalmente. Quanto mais blindado o inimigo, mais ele sofre.",
        type = CardType.EQUIPMENT,
        rarity = Rarity.LEGENDARY,
        slot = EquipmentSlot.WEAPON,
        baseStats = mapOf(
            Stat.ATK to 48.0,
            Stat.HP to 120.0
        ),
        statsPerLevel = mapOf(
            Stat.ATK to 4.5,
        ),
        tags = setOf("weapon", "armor-break"),
        abilities = listOf(
            Ability(
                name = "Ruptura",
                description = "No início da batalha, corrói 15% da DEF atual de todos os inimigos.",
                type = AbilityType.PASSIVE,
                trigger = AbilityTrigger.OnBattleStart,
                effects = listOf(
                    Effect.Custom("SIEGEBREAKER_RUPTURA") { self, _, state ->
                        val ownerTeam = state.teams.firstOrNull { t -> t.units.any { u -> u.id == self.id } } ?: return@Custom
                        val enemies = state.teams.filter { it != ownerTeam }.flatMap { it.units }
                        for (enemy in enemies) {
                            val def = enemy.stats[Stat.DEF] ?: 0.0
                            val reduction = def * 0.15
                            if (reduction <= 0) continue
                            enemy.stats[Stat.DEF] = def - reduction
                            state.combatLog += "🔩 Ruptura corroeu a armadura de ${enemy.card.name}: -${reduction.toInt()} DEF (-15%)"
                        }
                    }
                )
            ),
            Ability(
                name = "Golpe Corrosivo",
                description = "Cada ataque corrói 5% da DEF atual do alvo. Máximo de 3 acumulações por inimigo (~14% adicional).",
                type = AbilityType.PASSIVE,
                trigger = AbilityTrigger.OnAttack,
                effects = listOf(
                    Effect.Custom("SIEGEBREAKER_CORROSIVE") { _, target, state ->
                        if (target == null) return@Custom
                        val stackKey = "sbc:${target.id}"
                        val currentStacks = (state.globalFlags[stackKey] as? Int) ?: 0
                        if (currentStacks >= 3) return@Custom
                        val def = target.stats[Stat.DEF] ?: 0.0
                        if (def <= 0.0) return@Custom
                        val reduction = def * 0.05
                        target.stats[Stat.DEF] = def - reduction
                        state.globalFlags[stackKey] = currentStacks + 1
                        state.combatLog += "🔩 Golpe Corrosivo: ${target.card.name} perdeu ${reduction.toInt()} DEF (${currentStacks + 1}/3 stacks)"
                    }
                )
            )
        )
    )

    private val twinFangKatana = CardDefinition(
        id = "TWIN_FANG_KATANA",
        name = "Katana Bipartida",
        description = "Uma lâmina que se move em dois tempos. Todo crítico libera um segundo corte automático — rápido demais para ser bloqueado.",
        type = CardType.EQUIPMENT,
        rarity = Rarity.LEGENDARY,
        slot = EquipmentSlot.WEAPON,
        baseStats = mapOf(
            Stat.ATK to 22.0,
            Stat.SPEED to 12.0,
            Stat.CRIT_CHANCE to 0.16,
            Stat.CRIT_DAMAGE to 0.35
        ),
        statsPerLevel = mapOf(
            Stat.ATK to 3.5,
            Stat.SPEED to 1.5,
            Stat.CRIT_CHANCE to 0.015
        ),
        tags = setOf("katana", "crit", "speed"),
        abilities = listOf(
            Ability(
                name = "Corte Bipartido",
                description = "Ao acertar um crítico, desfere imediatamente um segundo golpe que causa 65% do ATK como dano físico.",
                type = AbilityType.PASSIVE,
                trigger = AbilityTrigger.OnCrit,
                effects = listOf(
                    Effect.Custom("Double strike 65% ATK") { self, target, state ->
                        if (target == null || target.hp <= 0) return@Custom
                        val damage = (self.stats[Stat.ATK] ?: 0.0) * 0.65
                        state.combatLog += "⚡ ${self.card.name} disparou um segundo corte!"
                        state.queue.add(
                            CombatEvent.BeforeDamage(
                                source = self,
                                target = target,
                                damage = damage,
                                damageType = DamageType.PHYSICAL,
                                canCrit = false
                            )
                        )
                    }
                )
            )
        )
    )

    // =========================================================================
    // NEW — MYTHIC EQUIPMENT
    // =========================================================================

    // AoE scaling orb. Huge ATK/HP gains, -35 DEF penalty. Grows indefinitely via Vortex.
    // Max lv20: ATK +152, HP +228. Singularidade: +30% ATK/HP on start. Pulse: 50% ATK magic AoE every 2 turns.
    private val cosmicOrb = CardDefinition(
        id = "COSMIC_ORB",
        name = "Orbe Cósmico",
        description = "Uma esfera de poder cósmico. Amplifica imensamente o ATK e HP — mas corrói completamente a capacidade defensiva do portador.",
        type = CardType.EQUIPMENT,
        rarity = Rarity.MYTHIC,
        slot = EquipmentSlot.TRINKET,
        baseStats = mapOf(
            Stat.ATK to 60.0,
            Stat.HP to 100.0,
            Stat.SPEED to 25.0,
            Stat.DEF to -35.0
        ),
        statsPerLevel = mapOf(
            Stat.ATK to 12.0,
            Stat.HP to 18.0
        ),
        tags = setOf("cosmic", "aoe", "scaling"),
        abilities = listOf(
            Ability(
                name = "Singularidade",
                description = "No início da batalha, a singularidade amplifica o portador: +30% ATK e +30% HP máximo.",
                type = AbilityType.PASSIVE,
                trigger = AbilityTrigger.OnBattleStart,
                effects = listOf(
                    Effect.StatIncreasePercent(stat = Stat.ATK, percent = 0.30),
                    Effect.StatIncreasePercent(stat = Stat.HP, percent = 0.30)
                )
            ),
            Ability(
                name = "Pulso Cósmico",
                description = "A cada 2 turnos, o orbe pulsa energia cósmica causando 50% do ATK como dano mágico a todos os inimigos.",
                type = AbilityType.PASSIVE,
                trigger = AbilityTrigger.OnTurnEvery(2),
                effects = listOf(
                    Effect.DamageBasedOnStat(
                        stat = Stat.ATK,
                        scaling = 0.50,
                        statSource = StatSource.SELF,
                        target = AbilityTarget.ALL_ENEMIES,
                        damageType = DamageType.MAGIC
                    )
                )
            ),
            Ability(
                name = "Vórtice de Poder",
                description = "A cada 4 turnos, o vórtice se intensifica, concedendo +30 ATK e +8 SPEED permanentes.",
                type = AbilityType.PASSIVE,
                trigger = AbilityTrigger.OnTurnEvery(4),
                effects = listOf(
                    Effect.BuffStat(stat = Stat.ATK, value = 30.0),
                    Effect.BuffStat(stat = Stat.SPEED, value = 8.0)
                )
            )
        )
    )

    private val royalCrossbowman = CardDefinition(
        id = "ROYAL_CROSSBOWMAN",
        name = "Besteiro Real",
        description = "Besteiro treinado pela guarda real. Constante e preciso — a cada três disparos, perfura a armadura do alvo definitivamente.",
        type = CardType.CHARACTER,
        rarity = Rarity.RARE,
        baseStats = mapOf(
            Stat.HP to 460.0,
            Stat.ATK to 42.0,
            Stat.DEF to 18.0,
            Stat.SPEED to 88.0,
            Stat.CRIT_CHANCE to 0.12,
            Stat.CRIT_DAMAGE to 1.35
        ),
        statsPerLevel = mapOf(
            Stat.HP to 7.0,
            Stat.ATK to 3.0,
            Stat.SPEED to 1.0
        ),
        tags = setOf("archer", "marksman"),
        abilities = listOf(
            Ability(
                name = "Flecha Perfurante",
                description = "A cada 3 ataques, dispara uma flecha carregada que causa 20 de dano extra e reduz permanentemente a DEF do alvo em 6.",
                type = AbilityType.PASSIVE,
                trigger = AbilityTrigger.OnAttackEvery(3),
                effects = listOf(
                    Effect.Damage(value = 20.0, target = AbilityTarget.ENEMY),
                    Effect.BuffStat(stat = Stat.DEF, value = -6.0, target = AbilityTarget.ENEMY)
                )
            )
        )
    )

    // =========================================================================
    // CATALOG
    // =========================================================================

    val all: List<CardDefinition> = listOf(
        // Characters — Common
        slime,
        juniorKnight,
        goblin,
        // Characters — Rare
        thief,
        ironGuardian,
        royalCrossbowman,
        // Characters — Epic
        jorge,
        veyn,
        aurum,
        lumina,
        shadow,
        berserker,
        // Characters — Legendary
        markus,
        solarPaladin,
        ironGargoyle,
        voidMage,
        // Characters — Mythic
        unleashedJuniorKnight,

        // Characters — Kono (system, not pullable)
        kono,
        dummy,

        // Equipment — Common
        woodenSword,
        dagger,
        ironArmor,
        // Equipment — Rare
        ironSword,
        ironShield,
        heavyIronArmor,
        katana,
        vampireRing,
        quickBoots,
        boneStaff,
        // Equipment — Epic
        polishedKatana,
        vampireCore,
        greatsword,
        gamblerCharm,
        devotionStaff,
        thornmail,
        elixirVial,
        // Equipment — Legendary
        critfish,
        demonHunterCrossbow,
        allInEmblem,
        siegebreaker,
        twinFangKatana,
        // Equipment — Mythic
        undefined,
        sunGodGreatsword,
        cosmicOrb
    )

    private val byId = all.associateBy { it.id }

    fun getById(id: String): CardDefinition? = byId[id.uppercase()]
}
