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
        description = "Criatura gelatinosa. Aguenta bastante pancada, mas é fraca ofensivamente — e ainda sofre ao atacar.",
        type = CardType.CHARACTER,
        rarity = Rarity.COMMON,
        faction = "slime",
        baseStats = mapOf(
            Stat.HP to 580.0,
            Stat.ATK to 28.0,
            Stat.DEF to 5.0,
            Stat.SPEED to 80.0,
            Stat.CRIT_CHANCE to 0.15,
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
            Stat.DEF to 46.0,
            Stat.SPEED to 70.0,
            Stat.CRIT_CHANCE to 0.15,
            Stat.CRIT_DAMAGE to 1.30
        ),
        statsPerLevel = mapOf(
            Stat.HP to 15.0,
            Stat.ATK to 8.0,
            Stat.DEF to 12.0
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
                trigger = AbilityTrigger.OnDamageTaken(),
                effects = listOf(Effect.Heal(value = 10.0, target = AbilityTarget.SELF))
            ),
            Ability(
                name = "Golpe do Guardião",
                description = "Jorge converte parte de sua resistência em um golpe pesado.",
                type = AbilityType.ACTIVE,
                trigger = AbilityTrigger.Manual,
                effects = listOf(
                    Effect.DamageBasedOnStat(
                        stat = Stat.DEF,
                        scaling = 1.2,
                        statSource = StatSource.SELF,
                        target = AbilityTarget.ENEMY,
                        damageType = DamageType.PHYSICAL
                    )
                )
            )
        )
    )

    private val veyn = CardDefinition(
        id = "VEYN",
        name = "Veyn",
        description = "O melhor atirador de Markus. Silencioso, paciente e extremamente preciso — Veyn não precisa de muitos disparos quando conhece exatamente onde acertar.",
        type = CardType.CHARACTER,
        rarity = Rarity.LEGENDARY,
        faction = "markus_gang",
        baseStats = mapOf(
            Stat.HP to 420.0,
            Stat.ATK to 48.0,
            Stat.DEF to 14.0,
            Stat.SPEED to 120.0,
            Stat.CRIT_CHANCE to 0.15,
            Stat.CRIT_DAMAGE to 1.50
        ),
        statsPerLevel = mapOf(
            Stat.HP to 4.0,
            Stat.ATK to 7.0,
            Stat.SPEED to 1.5,
            Stat.CRIT_CHANCE to 0.02
        ),
        tags = setOf(
            "speed",
            "archer",
            "marksman",
            "precision",
            "execution"
        ),
        abilities = listOf(

            // =====================================================================
            // PASSIVE — OLHO DE ÁGUIA
            // =====================================================================

            Ability(
                name = "Olho de Águia",
                description = "A cada ataque contra um inimigo, Veyn acumula uma Marca de Mira nesse alvo. Cada marca aumenta em 5% o dano de Veyn contra ele. Máximo de 3 marcas.",
                type = AbilityType.PASSIVE,
                trigger = AbilityTrigger.OnHit,
                effects = listOf(
                    Effect.Custom("VEYN_AIM_MARK") { self, target, state, team ->
                        if (target == null || target.hp <= 0) return@Custom

                        val markKey = "veyn:aim:${self.id}:${target.id}"
                        val currentMarks = (state.globalFlags[markKey] as? Int) ?: 0

                        if (currentMarks >= 3) return@Custom

                        val newMarks = currentMarks + 1
                        state.globalFlags[markKey] = newMarks

                        state.combatLog +=
                            "🎯 ${self.card.name} marcou ${target.card.name} " +
                                    "(${newMarks}/3)."
                    }
                )
            ),

            // =====================================================================
            // PASSIVE — PONTO FRACO
            // =====================================================================

            Ability(
                name = "Ponto Fraco",
                description = "Inimigos com 3 Marcas de Mira revelam seu ponto fraco. Contra eles, os ataques de Veyn ganham 20% de CRIT_CHANCE e ignoram 25% da DEF.",
                type = AbilityType.PASSIVE,
                trigger = AbilityTrigger.OnHit,
                effects = listOf(
                    Effect.Custom("VEYN_WEAK_POINT") { self, target, state, team ->
                        if (target == null || target.hp <= 0) return@Custom

                        val markKey = "veyn:aim:${self.id}:${target.id}"
                        val marks = (state.globalFlags[markKey] as? Int) ?: 0

                        if (marks < 3) return@Custom

                        /*
                         * O bônus de crítico é aplicado apenas durante este ataque.
                         * Como o sistema atual não possui um Effect explícito para
                         * "crit chance neste ataque", usamos a rolagem manual aqui.
                         *
                         * O dano/crit normal do ataque continua sendo processado
                         * pelo CombatEvent.
                         *
                         * A redução de DEF é aplicada apenas temporariamente para
                         * não destruir permanentemente os atributos do alvo.
                         */

                        val def = target.stats[Stat.DEF] ?: 0.0
                        if (def <= 0.0) return@Custom

                        val reduction = def * 0.25
                        target.stats[Stat.DEF] = def - reduction

                        state.globalFlags["veyn:def_restore:${self.id}:${target.id}"] = reduction

                        state.combatLog +=
                            "🎯 ${self.card.name} encontrou o ponto fraco de " +
                                    "${target.card.name} (-25% DEF)."
                    }
                )
            ),

            // =====================================================================
            // PASSIVE — APOSTA CERTA
            // =====================================================================

            Ability(
                name = "Aposta Certa",
                description = "Veyn é especialista em esperar o momento certo. Ao atingir um inimigo com 3 Marcas de Mira, seu próximo ataque contra ele recebe +30% de CRIT_DAMAGE.",
                type = AbilityType.PASSIVE,
                trigger = AbilityTrigger.OnHit,
                effects = listOf(
                    Effect.Custom("VEYN_PERFECT_BET") { self, target, state, team ->
                        if (target == null || target.hp <= 0) return@Custom

                        val markKey = "veyn:aim:${self.id}:${target.id}"
                        val marks = (state.globalFlags[markKey] as? Int) ?: 0

                        if (marks < 3) return@Custom

                        state.globalFlags["veyn:perfect:${self.id}:${target.id}"] = true

                        state.combatLog +=
                            "🎯 ${self.card.name} preparou um disparo perfeito contra " +
                                    "${target.card.name}."
                    }
                )
            ),

            // =====================================================================
            // ACTIVE — DISPARO PERFEITO
            // =====================================================================

            Ability(
                name = "Disparo Perfeito",
                description = "Veyn concentra toda sua mira em um único disparo. O dano aumenta conforme as Marcas de Mira no alvo. Com 3 marcas, o disparo ignora 50% da DEF e causa dano crítico garantido.",
                type = AbilityType.ACTIVE,
                trigger = AbilityTrigger.Manual,
                effects = listOf(
                    Effect.Custom("VEYN_PERFECT_SHOT") { self, target, state, team ->
                        if (target == null || target.hp <= 0) return@Custom

                        val markKey = "veyn:aim:${self.id}:${target.id}"
                        val marks = (state.globalFlags[markKey] as? Int) ?: 0

                        val atk = self.stats[Stat.ATK] ?: 0.0
                        val originalDef = target.stats[Stat.DEF] ?: 0.0

                        /*
                         * Multiplicador baseado na preparação:
                         *
                         * 0 marcas = 100%
                         * 1 marca  = 130%
                         * 2 marcas = 170%
                         * 3 marcas = 230%
                         */
                        val multiplier = when (marks) {
                            0 -> 1.00
                            1 -> 1.30
                            2 -> 1.70
                            else -> 2.30
                        }

                        val damage = atk * multiplier

                        /*
                         * Com 3 marcas, o disparo ignora 50% da DEF.
                         * Como o CombatEvent provavelmente calcula a mitigação
                         * posteriormente, reduzimos temporariamente a DEF.
                         */
                        if (marks >= 3 && originalDef > 0.0) {
                            target.stats[Stat.DEF] = originalDef * 0.50
                        }

                        val perfectShot = marks >= 3

                        state.combatLog +=
                            "🏹 ${self.card.name} dispara o DISPARO PERFEITO contra " +
                                    "${target.card.name} — $marks marcas, " +
                                    "${"%.0f".format(multiplier * 100)}% ATK" +
                                    if (perfectShot) " — PONTO FRACO!" else ""

                        state.queue.add(
                            CombatEvent.BeforeDamage(
                                source = self,
                                target = target,
                                damage = damage,
                                damageType = DamageType.PHYSICAL,
                                canCrit = perfectShot
                            )
                        )

                        /*
                         * O disparo consome toda a preparação.
                         */
                        state.globalFlags.remove(markKey)
                        state.globalFlags.remove(
                            "veyn:perfect:${self.id}:${target.id}"
                        )

                        /*
                         * Restaura a DEF caso tenhamos alterado para o disparo.
                         *
                         * Como o dano está na queue, a restauração precisa acontecer
                         * após o processamento do evento. O sistema atual não possui
                         * um trigger explícito de "AfterDamage", então registramos
                         * o valor para que o CombatEvent/dispatcher possa restaurá-lo
                         * caso necessário.
                         */
                        if (marks >= 3 && originalDef > 0.0) {
                            state.globalFlags[
                                "veyn:restore_def:${self.id}:${target.id}"
                            ] = originalDef
                        }
                    }
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
        faction = "sol",
        baseStats = mapOf(
            Stat.HP to 660.0,
            Stat.ATK to 58.0,
            Stat.DEF to 36.0,
            Stat.SPEED to 90.0,
            Stat.CRIT_CHANCE to 0.10,
            Stat.CRIT_DAMAGE to 1.30
        ),
        statsPerLevel = mapOf(
            Stat.HP to 14.0,
            Stat.ATK to 12.0,
            Stat.DEF to 3.0
        ),
        abilities = listOf(
            Ability(
                name = "Graça Contínua",
                description = "Lumina cura todos os aliados vivos em 20% de seu ATK.",
                type = AbilityType.ACTIVE,
                trigger = AbilityTrigger.Manual,
                effects = listOf(
                    Effect.Custom("Heal allies 20% ATK") { self, target, state, team ->
                        val healAmount = (self.stats[Stat.ATK] ?: 0.0) * 0.15
                        if (healAmount <= 0) return@Custom
                        val team = team ?: return@Custom
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
                description = "Lumina concede a todos os aliados +30% ATK temporário por 2 rodadas.",
                type = AbilityType.ACTIVE,
                trigger = AbilityTrigger.Manual,
                effects = listOf(
                    Effect.Custom("Temp buff ATK 30%") { self, target, state, team ->
                        val atk = self.stats[Stat.ATK] ?: 0.0
                        val atkBuff = atk * 0.15
                        val team = team ?: return@Custom
                        team.units.filter { it.hp > 0 }.forEach { ally ->
                            ally.stats[Stat.ATK] = (ally.stats[Stat.ATK] ?: 0.0) + atkBuff
                            state.temporaryStatModifiers += TemporaryStatModifier(
                                unitId = ally.id,
                                stat = Stat.ATK,
                                delta = atkBuff,
                                remainingRounds = 2,
                                source = "LUMINA_BUFF"
                            )
                            state.combatLog += "🌅 ${ally.card.name} recebeu +${atkBuff.toInt()} ATK por 2 rodadas."
                        }
                    }
                )
            ),
            Ability(
                name = "Fé Crescente",
                description = "A cada 4 turnos, Lumina fortalece sua própria fé e ganha +8 ATK permanente.",
                type = AbilityType.PASSIVE,
                trigger = AbilityTrigger.OnTurnEvery(4),
                effects = listOf(
                    Effect.Custom("Self-scale ATK") { self, target, state, team ->
                        self.stats[Stat.ATK] = (self.stats[Stat.ATK] ?: 0.0) + 8.0
                        state.combatLog += "🙏 ${self.card.name} fortaleceu sua fé (+8 ATK)."
                    }
                )
            )
        )
    )


    private val markus = CardDefinition(
        id = "MARKUS",
        name = "Markus, Mestre das Apostas",
        description = "O maior apostador do cassino. Para Markus, toda ficha é uma oportunidade — e toda oportunidade merece uma aposta.",
        type = CardType.CHARACTER,
        rarity = Rarity.MYTHIC,
        faction = "markus_gang",
        baseStats = mapOf(
            Stat.HP to 760.0,
            Stat.ATK to 54.0,
            Stat.DEF to 20.0,
            Stat.SPEED to 110.0,
            Stat.CRIT_CHANCE to 0.10,
            Stat.CRIT_DAMAGE to 2.0
        ),
        statsPerLevel = mapOf(
            Stat.HP to 10.0,
            Stat.ATK to 5.5,
            Stat.DEF to 2.5,
            Stat.CRIT_CHANCE to 0.01,
            Stat.CRIT_DAMAGE to 0.02
        ),
        tags = setOf(
            "rng",
            "gambler",
            "risk",
            "chaos",
            "scaling",
            "boss",
            "markus_gang",
            "malignant"
        ),
        abilities = listOf(

            // =====================================================================
            // PASSIVE — MESTRE DA MESA
            // =====================================================================

            Ability(
                name = "Mestre da Mesa",
                description = "A cada turno, Markus gera 3 fichas para sua equipe. Sempre que uma ficha é adicionada ou gasta por Markus, existe uma pequena chance de desencadear um Evento da Mesa.",
                type = AbilityType.PASSIVE,
                trigger = AbilityTrigger.OnTurnStart,
                effects = listOf(
                    Effect.Custom("MARKUS_TABLE_MASTER") { self, target, state, team ->
                        val team = team
                            ?: return@Custom

                        team.addCoins(3)

                        state.combatLog +=
                            "🎰 ${self.card.name} recebeu 3 fichas."

                        /*
                         * Pequena chance de um evento acontecer simplesmente
                         * porque Markus entrou em uma nova rodada.
                         *
                         * Isso evita que o sistema fique completamente previsível.
                         */
                        if (Random.nextDouble() < 0.12) {
                            state.combatLog +=
                                "🎲 A mesa chamou Markus para uma aposta!"

                            state.globalFlags["markus:table_event"] = true
                        }
                    }
                )
            ),

            // =====================================================================
            // PASSIVE — A CASA SEMPRE GANHA
            // =====================================================================

            Ability(
                name = "A Casa Sempre Ganha",
                description = "Sempre que Markus perde fichas, existe uma chance de recuperar parte delas. Quando a sorte decide devolver o dinheiro, um novo Evento da Mesa pode acontecer.",
                type = AbilityType.PASSIVE,
                trigger = AbilityTrigger.OnTurnStart,
                effects = listOf(
                    Effect.Custom("MARKUS_HOUSE_ALWAYS_WINS") { self, target, state, team ->
                        val team = team
                            ?: return@Custom

                        val previousCoins =
                            (state.globalFlags["markus:previous_coins"] as? Int)
                                ?: team.coins()

                        val currentCoins = team.coins()

                        state.globalFlags["markus:previous_coins"] = currentCoins

                        /*
                         * Detecta se Markus perdeu fichas desde o último turno.
                         */
                        if (currentCoins < previousCoins) {
                            val lost = previousCoins - currentCoins

                            if (Random.nextDouble() < 0.25) {
                                val refund = maxOf(1, lost / 2)

                                team.addCoins(refund)

                                state.combatLog +=
                                    "🃏 ${self.card.name} recuperou $refund fichas da mesa!"
                            }
                        }
                    }
                )
            ),

            // =====================================================================
            // ACTIVE — APOSTA BAIXA
            // =====================================================================

            Ability(
                name = "Aposta Baixa",
                description = "Markus aposta 3 fichas para provocar um pequeno Evento da Mesa. O resultado pode beneficiar Markus, sua equipe ou seus inimigos.",
                type = AbilityType.ACTIVE,
                trigger = AbilityTrigger.Manual,
                effects = listOf(
                    Effect.Custom("MARKUS_LOW_BET") { self, target, state, team ->
                        val team = team
                            ?: return@Custom

                        if (team.coins() < 3) {
                            state.combatLog +=
                                "🎰 ${self.card.name} não possui fichas suficientes para Aposta Baixa."
                            return@Custom
                        }

                        team.addCoins(-3)

                        state.combatLog +=
                            "🎲 ${self.card.name} apostou 3 fichas..."

                        when (Random.nextInt(0, 8)) {

                            // -----------------------------------------------------
                            // GANHO
                            // -----------------------------------------------------

                            0 -> {
                                team.addCoins(5)

                                state.combatLog +=
                                    "💰 PEQUENA VITÓRIA! Markus ganhou 5 fichas."
                            }

                            // -----------------------------------------------------
                            // CURA
                            // -----------------------------------------------------

                            1 -> {
                                val maxHp = self.stats[Stat.HP] ?: 0.0
                                val heal = maxHp * 0.15

                                self.hp = minOf(
                                    self.hp + heal,
                                    maxHp
                                )

                                state.combatLog +=
                                    "❤️ A sorte sorriu! Markus recuperou ${"%.1f".format(heal)} HP."
                            }

                            // -----------------------------------------------------
                            // ATAQUE
                            // -----------------------------------------------------

                            2 -> {
                                if (target != null && target.hp > 0) {
                                    val damage =
                                        (self.stats[Stat.ATK] ?: 0.0) * 1.25

                                    state.queue.add(
                                        CombatEvent.BeforeDamage(
                                            source = self,
                                            target = target,
                                            damage = damage
                                        )
                                    )

                                    state.combatLog +=
                                        "💥 APOSTA OFENSIVA! Markus causou ${"%.1f".format(damage)} de dano."
                                }
                            }

                            // -----------------------------------------------------
                            // BUFF ATK
                            // -----------------------------------------------------

                            3 -> {
                                val bonus =
                                    (self.stats[Stat.ATK] ?: 0.0) * 0.15

                                self.stats[Stat.ATK] =
                                    (self.stats[Stat.ATK] ?: 0.0) + bonus

                                state.combatLog +=
                                    "🔥 Markus ganhou +${"%.1f".format(bonus)} ATK permanentemente!"
                            }

                            // -----------------------------------------------------
                            // BUFF SPEED
                            // -----------------------------------------------------

                            4 -> {
                                val bonus =
                                    (self.stats[Stat.SPEED] ?: 0.0) * 0.10

                                self.stats[Stat.SPEED] =
                                    (self.stats[Stat.SPEED] ?: 0.0) + bonus

                                state.combatLog +=
                                    "⚡ Markus ficou mais rápido! +${"%.1f".format(bonus)} SPEED."
                            }

                            // -----------------------------------------------------
                            // DEBUFF
                            // -----------------------------------------------------

                            5 -> {
                                if (target != null && target.hp > 0) {
                                    val def = target.stats[Stat.DEF] ?: 0.0
                                    val reduction = def * 0.15

                                    target.stats[Stat.DEF] =
                                        maxOf(0.0, def - reduction)

                                    state.combatLog +=
                                        "💀 APOSTA SUJA! ${target.card.name} perdeu ${"%.1f".format(reduction)} DEF."
                                }
                            }

                            // -----------------------------------------------------
                            // PERDA
                            // -----------------------------------------------------

                            6 -> {
                                if (team.coins() > 0) {
                                    team.addCoins(-1)

                                    state.combatLog +=
                                        "💸 A CASA GANHOU! Markus perdeu mais uma ficha."
                                } else {
                                    state.combatLog +=
                                        "💸 A CASA GANHOU! Markus ficou sem fichas."
                                }
                            }

                            // -----------------------------------------------------
                            // JACKPOT
                            // -----------------------------------------------------

                            7 -> {
                                val jackpot = 8

                                team.addCoins(jackpot)

                                state.combatLog +=
                                    "🎰 JACKPOT! Markus ganhou $jackpot fichas!"
                            }
                        }
                    }
                )
            ),

            // =====================================================================
            // ACTIVE — DOBRO OU NADA
            // =====================================================================

            Ability(
                name = "Dobro ou Nada",
                description = "Markus aposta todas as fichas da equipe. 40% de chance de dobrá-las, 30% de perder tudo, 20% de conseguir um Jackpot e 10% de sofrer um desastre.",
                type = AbilityType.ACTIVE,
                trigger = AbilityTrigger.Manual,
                effects = listOf(
                    Effect.Custom("MARKUS_DOUBLE_OR_NOTHING") { self, target, state, team ->
                        val team = team
                            ?: return@Custom

                        val coins = team.coins()

                        if (coins <= 0) {
                            state.combatLog +=
                                "🎰 ${self.card.name} não possui fichas para apostar."
                            return@Custom
                        }

                        team.addCoins(-coins)

                        state.combatLog +=
                            "🎲 ${self.card.name} colocou $coins fichas na mesa."

                        when (Random.nextInt(0, 100)) {

                            // =====================================================
                            // VITÓRIA — 40%
                            // =====================================================

                            in 0..39 -> {
                                val winnings = coins * 2

                                team.addCoins(winnings)

                                val atkBonus =
                                    (self.stats[Stat.ATK] ?: 0.0) * 0.15

                                self.stats[Stat.ATK] =
                                    (self.stats[Stat.ATK] ?: 0.0) + atkBonus

                                state.combatLog +=
                                    "💰 DOBRO OU NADA — VITÓRIA! " +
                                            "$coins → $winnings fichas. " +
                                            "Markus ganhou +${"%.1f".format(atkBonus)} ATK."
                            }

                            // =====================================================
                            // DERROTA — 30%
                            // =====================================================

                            in 40..69 -> {
                                state.combatLog +=
                                    "💸 DOBRO OU NADA — DERROTA! " +
                                            "Markus perdeu todas as $coins fichas."
                            }

                            // =====================================================
                            // JACKPOT — 20%
                            // =====================================================

                            in 70..89 -> {
                                val winnings = coins * 3

                                team.addCoins(winnings)

                                state.combatLog +=
                                    "🎰 JACKPOT! " +
                                            "$coins → $winnings fichas!"
                            }

                            // =====================================================
                            // DESASTRE — 10%
                            // =====================================================

                            else -> {
                                val maxHp =
                                    self.stats[Stat.HP] ?: 0.0

                                val damage =
                                    maxHp * 0.10

                                self.hp = maxOf(
                                    1.0,
                                    self.hp - damage
                                )

                                state.combatLog +=
                                    "💀 DESASTRE! A casa roubou tudo e Markus " +
                                            "sofreu ${"%.1f".format(damage)} de dano!"
                            }
                        }
                    }
                )
            ),

            // =====================================================================
            // ACTIVE — ALL-IN
            // =====================================================================

            Ability(
                name = "ALL-IN",
                description = "Markus aposta todas as fichas restantes. Quanto maior a aposta, mais caóticos e poderosos são os possíveis resultados.",
                type = AbilityType.ACTIVE,
                trigger = AbilityTrigger.Manual,
                effects = listOf(
                    Effect.Custom("MARKUS_ALL_IN") { self, target, state, team ->
                        if (target == null || target.hp <= 0) {
                            state.combatLog +=
                                "🎰 ALL-IN cancelado: nenhum alvo válido."
                            return@Custom
                        }

                        val team = team
                            ?: return@Custom

                        val coins = team.coins()

                        if (coins <= 0) {
                            state.combatLog +=
                                "🎰 ALL-IN cancelado: Markus não possui fichas."
                            return@Custom
                        }

                        team.addCoins(-coins)

                        state.combatLog +=
                            "💀 ${self.card.name} declarou ALL-IN com $coins fichas!"

                        /*
                         * Quanto mais fichas, maior o acesso aos resultados
                         * realmente absurdos.
                         */
                        val roll = Random.nextInt(0, 100)

                        val atk =
                            self.stats[Stat.ATK] ?: 0.0

                        when {

                            // =====================================================
                            // 0-4 FICHAS — APOSTA PEQUENA
                            // =====================================================

                            coins < 5 -> {
                                val damage = atk * Random.nextDouble(0.8, 1.8)

                                state.queue.add(
                                    CombatEvent.BeforeDamage(
                                        source = self,
                                        target = target,
                                        damage = damage
                                    )
                                )

                                state.combatLog +=
                                    "🎲 A mesa devolveu um ataque simples: " +
                                            "${"%.1f".format(damage)} de dano."
                            }

                            // =====================================================
                            // 5-9 FICHAS
                            // =====================================================

                            coins < 10 -> {
                                when {

                                    roll < 70 -> {
                                        val damage =
                                            atk * Random.nextDouble(2.0, 3.5)

                                        state.queue.add(
                                            CombatEvent.BeforeDamage(
                                                source = self,
                                                target = target,
                                                damage = damage
                                            )
                                        )

                                        state.combatLog +=
                                            "💥 GRANDE APOSTA! " +
                                                    "${"%.1f".format(damage)} de dano."
                                    }

                                    roll < 90 -> {
                                        val damage =
                                            atk * Random.nextDouble(4.0, 6.0)

                                        state.queue.add(
                                            CombatEvent.BeforeDamage(
                                                source = self,
                                                target = target,
                                                damage = damage
                                            )
                                        )

                                        state.combatLog +=
                                            "🔥 APOSTA VENCEDORA! " +
                                                    "${"%.1f".format(damage)} de dano."
                                    }

                                    else -> {
                                        team.addCoins(coins * 2)

                                        state.combatLog +=
                                            "🎰 JACKPOT! " +
                                                    "Markus recuperou ${coins * 2} fichas!"
                                    }
                                }
                            }

                            // =====================================================
                            // 10-19 FICHAS
                            // =====================================================

                            coins < 20 -> {
                                when {

                                    roll < 50 -> {
                                        val damage =
                                            atk * Random.nextDouble(4.0, 7.0)

                                        state.queue.add(
                                            CombatEvent.BeforeDamage(
                                                source = self,
                                                target = target,
                                                damage = damage
                                            )
                                        )

                                        state.combatLog +=
                                            "💥 ALL-IN! Markus explodiu a mesa " +
                                                    "causando ${"%.1f".format(damage)} de dano."
                                    }

                                    roll < 75 -> {
                                        repeat(3) {
                                            val damage =
                                                atk * Random.nextDouble(1.5, 3.0)

                                            state.queue.add(
                                                CombatEvent.BeforeDamage(
                                                    source = self,
                                                    target = target,
                                                    damage = damage
                                                )
                                            )
                                        }

                                        state.combatLog +=
                                            "🔫 RAJADA DA SORTE! " +
                                                    "Markus disparou 3 ataques!"
                                    }

                                    roll < 95 -> {
                                        team.addCoins(coins * 2)

                                        state.combatLog +=
                                            "🎰 JACKPOT! " +
                                                    "A mesa devolveu ${coins * 2} fichas!"
                                    }

                                    else -> {
                                        val damage =
                                            self.stats[Stat.HP]?.times(0.20) ?: 0.0

                                        self.hp =
                                            maxOf(1.0, self.hp - damage)

                                        state.combatLog +=
                                            "💀 DESASTRE! Markus perdeu o ALL-IN " +
                                                    "e sofreu ${"%.1f".format(damage)} de dano."
                                    }
                                }
                            }

                            // =====================================================
                            // 20+ FICHAS — MODO CAOS
                            // =====================================================

                            else -> {
                                when {

                                    // -------------------------------------------------
                                    // 40% — DANO ABSURDO
                                    // -------------------------------------------------

                                    roll < 40 -> {
                                        val damage =
                                            atk * Random.nextDouble(8.0, 14.0)

                                        state.queue.add(
                                            CombatEvent.BeforeDamage(
                                                source = self,
                                                target = target,
                                                damage = damage
                                            )
                                        )

                                        state.combatLog +=
                                            "🔥🔥 MEGA APOSTA! " +
                                                    "Markus causou ${"%.1f".format(damage)} de dano!"
                                    }

                                    // -------------------------------------------------
                                    // 25% — MULTI ATTACK
                                    // -------------------------------------------------

                                    roll < 65 -> {
                                        repeat(5) {
                                            val damage =
                                                atk * Random.nextDouble(1.5, 3.5)

                                            state.queue.add(
                                                CombatEvent.BeforeDamage(
                                                    source = self,
                                                    target = target,
                                                    damage = damage
                                                )
                                            )
                                        }

                                        state.combatLog +=
                                            "🔫🔫🔫 CAOS TOTAL! " +
                                                    "Markus realizou 5 ataques!"
                                    }

                                    // -------------------------------------------------
                                    // 20% — JACKPOT
                                    // -------------------------------------------------

                                    roll < 85 -> {
                                        val winnings =
                                            coins * 3

                                        team.addCoins(winnings)

                                        state.combatLog +=
                                            "🎰👑 MEGA JACKPOT! " +
                                                    "$coins → $winnings fichas!"
                                    }

                                    // -------------------------------------------------
                                    // 10% — EVENTO ALEATÓRIO
                                    // -------------------------------------------------

                                    roll < 95 -> {
                                        when (Random.nextInt(0, 4)) {

                                            0 -> {
                                                val bonus =
                                                    atk * 0.50

                                                self.stats[Stat.ATK] =
                                                    (self.stats[Stat.ATK] ?: 0.0) + bonus

                                                state.combatLog +=
                                                    "🃏 CARTA CORINGA! " +
                                                            "Markus ganhou +${"%.1f".format(bonus)} ATK."
                                            }

                                            1 -> {
                                                val maxHp =
                                                    self.stats[Stat.HP] ?: 0.0

                                                val heal =
                                                    maxHp * 0.50

                                                self.hp =
                                                    minOf(
                                                        self.hp + heal,
                                                        maxHp
                                                    )

                                                state.combatLog +=
                                                    "❤️ CARTA CORINGA! " +
                                                            "Markus recuperou ${"%.1f".format(heal)} HP."
                                            }

                                            2 -> {
                                                team.addCoins(10)

                                                state.combatLog +=
                                                    "🪙 CARTA CORINGA! " +
                                                            "Markus ganhou 10 fichas extras."
                                            }

                                            else -> {
                                                val damage =
                                                    atk * Random.nextDouble(3.0, 8.0)

                                                state.queue.add(
                                                    CombatEvent.BeforeDamage(
                                                        source = self,
                                                        target = target,
                                                        damage = damage
                                                    )
                                                )

                                                state.combatLog +=
                                                    "🃏 CARTA CORINGA! " +
                                                            "Um disparo aleatório causou " +
                                                            "${"%.1f".format(damage)} de dano."
                                            }
                                        }
                                    }

                                    // -------------------------------------------------
                                    // 5% — DESASTRE
                                    // -------------------------------------------------

                                    else -> {
                                        val damage =
                                            self.stats[Stat.HP]?.times(0.35) ?: 0.0

                                        self.hp =
                                            maxOf(1.0, self.hp - damage)

                                        state.combatLog +=
                                            "💀💀 A CASA QUEBROU MARKUS! " +
                                                    "Ele perdeu o ALL-IN e sofreu " +
                                                    "${"%.1f".format(damage)} de dano!"
                                    }
                                }
                            }
                        }
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
        faction = "sol",
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
                description = "Causa 20% de dano extra em inimigos com a tag 'malignant'.",
                type = AbilityType.PASSIVE,
                trigger = AbilityTrigger.OnAttackAgainstTag("malignant"),
                effects = listOf(Effect.DamageIncreasePercent(value = 0.20))
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
                trigger = AbilityTrigger.OnDamageTaken(),
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

    private val ironTorc = CardDefinition(
        id = "IRON_TORC",
        name = "Torque de Ferro",
        description = "Um anel de pescoço simples que fortalece levemente o portador. Sem magias, sem truques.",
        type = CardType.EQUIPMENT,
        rarity = Rarity.COMMON,
        slot = EquipmentSlot.TRINKET,
        baseStats = mapOf(
            Stat.HP to 55.0,
            Stat.DEF to 10.0
        ),
        statsPerLevel = mapOf(
            Stat.HP to 6.0,
            Stat.DEF to 1.0
        ),
        tags = setOf("starter", "defense"),
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
                trigger = AbilityTrigger.OnHit,
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

    private val magicCrystal = CardDefinition(
        id = "MAGIC_CRYSTAL",
        name = "Cristal Arcano",
        description = "Um fragmento de cristal carregado de energia mágica. Aguça a consciência arcana, aumentando a resistência a feitiços.",
        type = CardType.EQUIPMENT,
        rarity = Rarity.RARE,
        slot = EquipmentSlot.TRINKET,
        baseStats = mapOf(
            Stat.INT to 28.0,
            Stat.HP to -15.0
        ),
        statsPerLevel = mapOf(Stat.INT to 3.5),
        tags = setOf("magic", "int"),
        abilities = emptyList()
    )

    private val reinforcedPauldrons = CardDefinition(
        id = "REINFORCED_PAULDRONS",
        name = "Paramentos Reforçados",
        description = "Ombros reforçados com placas extras. Oferece proteção sólida e endurece gradualmente durante o combate.",
        type = CardType.EQUIPMENT,
        rarity = Rarity.RARE,
        slot = EquipmentSlot.ARMOR,
        baseStats = mapOf(
            Stat.DEF to 34.0,
            Stat.HP to 55.0,
            Stat.ATK to -6.0
        ),
        statsPerLevel = mapOf(
            Stat.DEF to 3.5,
            Stat.HP to 7.0
        ),
        tags = setOf("armor", "defense"),
        abilities = listOf(
            Ability(
                name = "Enrijecer",
                description = "A cada 3 turnos, as placas se ajustam e ganham +4 DEF permanente.",
                type = AbilityType.PASSIVE,
                trigger = AbilityTrigger.OnTurnEvery(3),
                effects = listOf(Effect.BuffStat(stat = Stat.DEF, value = 4.0, target = AbilityTarget.SELF))
            )
        )
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
                trigger = AbilityTrigger.OnHit,
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
                description = "Desfere um golpe colossal que causa 140% do ATK atual como dano.",
                type = AbilityType.ACTIVE,
                trigger = AbilityTrigger.Manual,
                effects = listOf(
                    Effect.DamageBasedOnStat(stat = Stat.ATK, scaling = 1.4, statSource = StatSource.SELF)
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
        abilities = listOf(
            Ability(
                name = "Proteção Sagrada",
                description = "A cada 2 turnos, concede escudos temporários: membros da fé recebem 16% do ATK, os demais 8%.",
                type = AbilityType.PASSIVE,
                trigger = AbilityTrigger.OnTurnEvery(2),
                effects = listOf(
                    Effect.Custom("Solar shield") { self, target, state, team ->
                        val atk = self.stats[Stat.ATK] ?: 0.0
                        val team = team ?: return@Custom
                        team.units.filter { it.hp > 0 }.forEach { ally ->
                            val isFaith = ally.card.faction == "sol"
                            val shieldValue = atk * (if (isFaith) 0.16 else 0.08)
                            state.temporaryStatModifiers += TemporaryStatModifier(
                                unitId = ally.id,
                                stat = Stat.HP,
                                delta = shieldValue,
                                remainingRounds = 1,
                                source = "DEVOTION_SHIELD"
                            )
                            state.combatLog += "🛡️ ${ally.card.name} recebeu ${shieldValue.toInt()} de escudo (${if (isFaith) "solar" else "normal"})."
                        }
                    }
                )
            )
        ),
        faction = "sol",
        baseStats = mapOf(
            Stat.ATK to 44.0,
            Stat.SPEED to -14.0
        ),
        statsPerLevel = mapOf(Stat.ATK to 6.0),
        tags = setOf("sol", "faith", "support", "scaling")
    )

    private val bulwarkShield = CardDefinition(
        id = "BULWARK_SHIELD",
        name = "Escudo Fortaleza",
        description = "Uma muralha portátil. Absorve dano expressivo — e quando o portador está à beira da morte, endurece ainda mais.",
        type = CardType.EQUIPMENT,
        rarity = Rarity.EPIC,
        slot = EquipmentSlot.SECONDARY,
        baseStats = mapOf(
            Stat.DEF to 46.0,
            Stat.HP to 60.0,
            Stat.ATK to -24.0,
            Stat.SPEED to -20.0
        ),
        statsPerLevel = mapOf(
            Stat.DEF to 4.5,
            Stat.HP to 14.0
        ),
        tags = setOf("shield", "defense", "tank"),
        abilities = listOf(
            Ability(
                name = "Última Barreira",
                description = "Ao cair abaixo de 50% de vida, o escudo se fortalece: +30% DEF permanente. Ocorre uma única vez.",
                type = AbilityType.PASSIVE,
                trigger = AbilityTrigger.OnBellowHealth(0.50),
                once = true,
                effects = listOf(Effect.StatIncreasePercent(stat = Stat.DEF, percent = 0.30))
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
                    Effect.Custom("CRITFISH_LOCK") { self, target, state, team ->
                        val currentCritDmg = self.stats[Stat.CRIT_DAMAGE] ?: 1.5
                        self.stats[Stat.CRIT_CHANCE] = 0.08
                        self.stats[Stat.CRIT_DAMAGE] = currentCritDmg * 3.0
                        state.combatLog += "🐟 Critfish travou o crítico em 8% e triplicou o multiplicador (${currentCritDmg}x → ${
                            "%.2f".format(
                                currentCritDmg * 3.0
                            )
                        }x)."
                    }
                )
            )
        )
    )

    private val demonHunterCrossbow = CardDefinition(
        id = "DEMON_HUNTER_CROSSBOW",
        name = "Besta da Caçadora de Demônios",
        description = "A cada 3 ataques, dispara uma flecha de prata que causa dano real com base no HP máximo do alvo. Excelente contra alvos com muita vida.",
        type = CardType.EQUIPMENT,
        rarity = Rarity.LEGENDARY,
        slot = EquipmentSlot.WEAPON,
        baseStats = mapOf(
            Stat.SPEED to 18.0,
            Stat.ATK to 18.0,
            Stat.CRIT_CHANCE to 10.0
        ),
        statsPerLevel = mapOf(
            Stat.SPEED to 3.0,
            Stat.ATK to 2.0
        ),
        tags = setOf("speed"),
        abilities = listOf(
            Ability(
                name = "Flecha de Prata",
                description = "A cada 3 ataques, dispara uma flecha que causa 7% do HP máximo do alvo como dano real.",
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
                trigger = AbilityTrigger.OnHit,
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

    private val stormBoots = CardDefinition(
        id = "STORM_BOOTS",
        name = "Botas da Tempestade",
        description = "Botas imbuídas de eletricidade. Cada passo acumula voltagem — e a cada ataque, parte dessa energia é descarregada no inimigo.",
        type = CardType.EQUIPMENT,
        rarity = Rarity.LEGENDARY,
        slot = EquipmentSlot.BOOTS,
        baseStats = mapOf(
            Stat.SPEED to 44.0,
            Stat.CRIT_CHANCE to 0.10,
            Stat.ATK to 14.0
        ),
        statsPerLevel = mapOf(
            Stat.SPEED to 4.5,
            Stat.CRIT_CHANCE to 0.01
        ),
        tags = setOf("speed", "lightning", "crit"),
        abilities = listOf(
            Ability(
                name = "Carga Relâmpago",
                description = "A cada ataque, libera uma descarga elétrica que causa 18% da SPEED atual como dano mágico.",
                type = AbilityType.PASSIVE,
                trigger = AbilityTrigger.OnHit,
                effects = listOf(
                    Effect.DamageBasedOnStat(
                        stat = Stat.SPEED,
                        scaling = 0.18,
                        statSource = StatSource.SELF,
                        target = AbilityTarget.ENEMY,
                        damageType = DamageType.MAGIC
                    )
                )
            )
        )
    )

    private val soulPendant = CardDefinition(
        id = "SOUL_PENDANT",
        name = "Pingente da Alma",
        description = "Um amuleto que contém um fragmento de alma. Cada crítico absorve vitalidade do inimigo, devolvendo força ao portador.",
        type = CardType.EQUIPMENT,
        rarity = Rarity.LEGENDARY,
        slot = EquipmentSlot.TRINKET,
        baseStats = mapOf(
            Stat.ATK to 28.0,
            Stat.LIFESTEAL to 0.20,
            Stat.CRIT_CHANCE to 0.12,
            Stat.CRIT_DAMAGE to 0.40
        ),
        statsPerLevel = mapOf(
            Stat.ATK to 4.0,
            Stat.CRIT_CHANCE to 0.01,
            Stat.LIFESTEAL to 0.01
        ),
        tags = setOf("lifesteal", "crit", "sustain"),
        abilities = listOf(
            Ability(
                name = "Absorção de Alma",
                description = "Ao acertar um crítico, a alma absorvida recupera 80 HP.",
                type = AbilityType.PASSIVE,
                trigger = AbilityTrigger.OnCrit,
                effects = listOf(Effect.Heal(value = 80.0, target = AbilityTarget.SELF))
            )
        )
    )

    // =========================================================================
    // MYTHIC EQUIPMENT
    // =========================================================================

    private val undefined = CardDefinition(
        id = "UNDEFINED",
        name = "undefined",
        description = "`error: undefined is not an item`",
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
        faction = "sol",
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
                trigger = AbilityTrigger.OnHit,
                effects = listOf(
                    Effect.BuffStat(stat = Stat.ATK, value = 3.5)
                )
            ),
            Ability(
                name = "Chama Divina",
                description = "Cada ataque aplica a chama divina ao alvo, causando 20% do ATK atual como dano real adicional.",
                type = AbilityType.PASSIVE,
                trigger = AbilityTrigger.OnHit,
                effects = listOf(
                    Effect.Custom("Real damage 20% ATK") { self, unit, state, _ ->
                        val damage = (self.stats[Stat.ATK] ?: 0.0) * 0.20
                        if (damage <= 0 || unit == null) return@Custom
                        state.combatLog += "🔥 A chama divina penetra em ${unit.card.name} causando ${
                            "%.1f".format(
                                damage
                            )
                        } de dano real!"
                        unit.hp -= damage
                    }
                )
            ),
            Ability(
                name = "Provação Divina",
                description = "Ao receber dano, a fé é testada e o ATK cresce em +5 permanentemente.",
                type = AbilityType.PASSIVE,
                trigger = AbilityTrigger.OnDamageTaken(),
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


    private val goblin = CardDefinition(
        id = "GOBLIN",
        name = "Goblin",
        description = "Rápido, fraco e agressivo. Ataca antes da maioria, mas fica mais perigoso a cada golpe desferido.",
        type = CardType.CHARACTER,
        rarity = Rarity.COMMON,
        baseStats = mapOf(
            Stat.HP to 380.0,
            Stat.ATK to 38.0,
            Stat.DEF to 4.0,
            Stat.SPEED to 98.0,
            Stat.CRIT_CHANCE to 0.06,
            Stat.CRIT_DAMAGE to 1.15
        ),
        statsPerLevel = mapOf(
            Stat.HP to 8.0,
            Stat.ATK to 4.5
        ),
        tags = setOf("starter", "fast"),
        abilities = listOf(
            Ability(
                name = "Garra Desesperada",
                description = "A cada golpe desferido, o Goblin fica mais agressivo e ganha +3 de ATK permanente.",
                type = AbilityType.PASSIVE,
                trigger = AbilityTrigger.OnHit,
                effects = listOf(Effect.BuffStat(stat = Stat.ATK, value = 3.0, target = AbilityTarget.SELF))
            )
        )
    )

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
                description = "A cada 2 turnos, o Guardião endurece sua armadura, ganhando +5 DEF permanente.",
                type = AbilityType.PASSIVE,
                trigger = AbilityTrigger.OnTurnEvery(2),
                effects = listOf(Effect.BuffStat(stat = Stat.DEF, value = 5.0, target = AbilityTarget.SELF))
            )
        )
    )

    private val ironGargoyle = CardDefinition(
        id = "IRON_GARGOYLE",
        name = "Gárgula de Ferro",
        description = "Um guardião de pedra. Praticamente indestrutível — e cada golpe recebido devolve energia sombria a todos os inimigos.",
        type = CardType.CHARACTER,
        rarity = Rarity.LEGENDARY,
        baseStats = mapOf(
            Stat.HP to 840.0,
            Stat.ATK to 58.0,
            Stat.DEF to 85.0,
            Stat.SPEED to 62.0,
            Stat.CRIT_CHANCE to 0.08,
            Stat.CRIT_DAMAGE to 1.25
        ),
        statsPerLevel = mapOf(
            Stat.HP to 22.0,
            Stat.DEF to 7.0,
            Stat.ATK to 3.5
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
                description = "A cada 2 turnos, a pedra se reconstrói sozinha, curando 28 HP.",
                type = AbilityType.PASSIVE,
                trigger = AbilityTrigger.OnTurnEvery(2),
                effects = listOf(Effect.Heal(value = 28.0, target = AbilityTarget.SELF))
            ),
            Ability(
                name = "Contra-Ataque de Pedra",
                description = "Ao receber dano físico, emite uma onda de pedra que causa 22% da sua DEF atual como dano mágico a todos os inimigos.",
                type = AbilityType.PASSIVE,
                trigger = AbilityTrigger.OnDamageTaken(DamageType.PHYSICAL),
                effects = listOf(
                    Effect.DamageBasedOnStat(
                        stat = Stat.DEF,
                        scaling = 0.22,
                        statSource = StatSource.SELF,
                        target = AbilityTarget.ALL_ENEMIES,
                        damageType = DamageType.MAGIC
                    )
                )
            )
        )
    )


    private val quickBoots = CardDefinition(
        id = "QUICK_BOOTS",
        name = "Botas Velozes",
        description = "Botas leves como vento. Concede um bônus expressivo de velocidade ao custo de um pouco de resistência.",
        type = CardType.EQUIPMENT,
        rarity = Rarity.RARE,
        slot = EquipmentSlot.BOOTS,
        baseStats = mapOf(
            Stat.SPEED to 30.0,
            Stat.HP to -10.0
        ),
        statsPerLevel = mapOf(Stat.SPEED to 4.0),
        tags = setOf("speed", "light"),
        abilities = emptyList()
    )


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
                description = "Ao receber dano físico, emite espinhos mágicos causando 25% da DEF atual como dano mágico a todos os inimigos.",
                type = AbilityType.PASSIVE,
                trigger = AbilityTrigger.OnDamageTaken(DamageType.PHYSICAL),
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
                description = "A cada 3 turnos, cura 80 HP.",
                type = AbilityType.PASSIVE,
                trigger = AbilityTrigger.OnTurnEvery(3),
                effects = listOf(
                    Effect.Heal(value = 80.0, target = AbilityTarget.SELF)
                )
            )
        )
    )


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
                    Effect.Custom("SIEGEBREAKER_RUPTURA") { self, target, state, team ->
                        val ownerTeam =
                            state.teams.firstOrNull { t -> t.units.any { u -> u.id == self.id } } ?: return@Custom
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
                trigger = AbilityTrigger.OnHit,
                effects = listOf(
                    Effect.Custom("SIEGEBREAKER_CORROSIVE") { _, target, state, _ ->
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
                description = "Ao acertar um crítico, desfere imediatamente um segundo golpe que causa 80% do ATK como dano físico.",
                type = AbilityType.PASSIVE,
                trigger = AbilityTrigger.OnCrit,
                effects = listOf(
                    Effect.Custom("Double strike 80% ATK") { self, target, state, team ->
                        if (target == null || target.hp <= 0) return@Custom
                        val damage = (self.stats[Stat.ATK] ?: 0.0) * 0.80
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

    private val swiftHunterCloth = CardDefinition(
        id = "SWIFT_HUNTER_CLOTH",
        name = "Manto do Vento Cortante",
        description = "Tecido leve imbuído de ventos afiados. Privilegia quem atinge rápido e com precisão — e cada crítico acertado alimenta ainda mais velocidade.",
        type = CardType.EQUIPMENT,
        rarity = Rarity.EPIC,
        slot = EquipmentSlot.ARMOR,
        baseStats = mapOf(
            Stat.SPEED to 32.0,
            Stat.CRIT_CHANCE to 0.15,
            Stat.CRIT_DAMAGE to 0.30,
            Stat.DEF to -8.0
        ),
        statsPerLevel = mapOf(
            Stat.SPEED to 3.0,
            Stat.CRIT_CHANCE to 0.015
        ),
        tags = setOf("speed", "crit", "light"),
        abilities = listOf(
            Ability(
                name = "Conversão Aerodinâmica",
                description = "No início da batalha, converte 8% da SPEED atual em bônus de dano crítico permanente.",
                type = AbilityType.PASSIVE,
                trigger = AbilityTrigger.OnBattleStart,
                effects = listOf(
                    Effect.Custom("Speed to crit damage") { self, target, state, team ->
                        val speed = self.stats[Stat.SPEED] ?: 0.0
                        val bonus = speed * 0.08
                        self.stats[Stat.CRIT_DAMAGE] = (self.stats[Stat.CRIT_DAMAGE] ?: 0.0) + (bonus / 100)
                        state.combatLog += "💨 ${self.card.name} converteu velocidade em poder crítico (+${
                            "%.2f".format(
                                bonus
                            )
                        }x CRIT DMG)."
                    }
                )
            ),
            Ability(
                name = "Aceleração Instintiva",
                description = "Cada crítico acertado acelera o portador permanentemente: +2 SPEED.",
                type = AbilityType.PASSIVE,
                trigger = AbilityTrigger.OnCrit,
                effects = listOf(
                    Effect.BuffStat(stat = Stat.SPEED, value = 2.0, target = AbilityTarget.SELF)
                )
            )
        )
    )

    private val heavySteelBoots = CardDefinition(
        id = "HAEVY_STEEL_BOOTS",
        name = "Botas de Aço Pesadas",
        description = "Botas maciças forjadas em aço puro. Reduzem drasticamente a mobilidade, mas transformam cada passo em uma ameaça.",
        type = CardType.EQUIPMENT,
        slot = EquipmentSlot.BOOTS,
        rarity = Rarity.EPIC,
        baseStats = mapOf(
            Stat.DEF to 38.0,
            Stat.SPEED to -15.0
        ),
        statsPerLevel = mapOf(Stat.DEF to 5.0),
        tags = setOf("heavy", "boots", "defense"),
        abilities = listOf(
            Ability(
                name = "Pisão de Aço",
                description = "A cada 3 turnos, dá um pisão no chão que causa 30% da DEF atual como dano físico a todos os inimigos.",
                type = AbilityType.PASSIVE,
                trigger = AbilityTrigger.OnTurnEvery(3),
                effects = listOf(
                    Effect.DamageBasedOnStat(
                        stat = Stat.DEF,
                        scaling = 0.30,
                        statSource = StatSource.SELF,
                        target = AbilityTarget.ALL_ENEMIES,
                        damageType = DamageType.PHYSICAL
                    )
                )
            )
        )
    )

    val renouncedSwordCloth = CardDefinition(
        id = "RENOUNCED_SWORD_CLOTH",
        name = "Vestes da Espada Renunciada",
        description = "A vestimenta daquela que abandonou seu próprio nome para tornar-se apenas uma extensão da lâmina. Dizem que o tecido ainda pulsa ao ouvir o som de espadas gêmeas",
        type = CardType.EQUIPMENT,
        slot = EquipmentSlot.ARMOR,
        rarity = Rarity.LEGENDARY,
        faction = "kono",
        tags = setOf("kono", "celestial", "physical", "magic"),
        baseStats = mapOf(
            Stat.ATK to 75.0,
            Stat.INT to 75.0,
            Stat.SPEED to 15.0
        ),
        statsPerLevel = mapOf(
            Stat.ATK to 8.0,
            Stat.INT to 8.0,
        ),
        abilities = listOf(
            Ability(
                name = "Corte Gêmeo Espectral",
                description = "Um espectro replica os ataques da lâmina esquerda causando dano mágico adicional.",
                type = AbilityType.PASSIVE,
                trigger = AbilityTrigger.OnHit,
                effects = listOf(
                    Effect.DamageBasedOnStat(Stat.INT, 0.8, damageType = DamageType.MAGIC)
                )
            )
        )
    )

    val konoSister = CardDefinition(
        id = "KONO_SISTER",
        name = "A Gemea de Kono",
        rarity = Rarity.KONO,
        description = "A Espada Gêmea de Kono.\n" +
                "\n" +
                "Seu verdadeiro nome foi perdido há muito tempo, levado pelos ventos da guerra antiga.\n" +
                "\n" +
                "Ela própria o abandonou.\n" +
                "\n" +
                "Tudo o que restou foi seu propósito.\n" +
                "\n" +
                "Tornar-se a espada definitiva de Kono.\n" +
                "\n" +
                "Desde então, dedicou sua existência apenas a isso.\n" +
                "\n" +
                "Não possui reino.\n" +
                "Não possui ambições.\n" +
                "Não possui desejos além de proteger sua irmã.\n" +
                "\n" +
                "Dizem que seus passos são mais rápidos que o som do aço.\n" +
                "\n" +
                "Que suas lâminas dançam antes mesmo que o inimigo perceba a própria morte.\n" +
                "\n" +
                "E em todas as eras…\n" +
                "\n" +
                "Nunca perdeu uma batalha.",
        type = CardType.CHARACTER,
        baseStats = mapOf(),
        statsPerLevel = mapOf(),
        abilities = listOf()
    )

    val konoTwinbladeR = CardDefinition(
        id = "KONO_TWINBLADE_R",
        name = "Lamina Gemea do Alvorecer",
        rarity = Rarity.MYTHIC,
        type = CardType.EQUIPMENT,
        slot = EquipmentSlot.WEAPON,
        description =
            "A lâmina direita da Espada Gêmea de Kono. " +
                    "Leve como luz e rápida como pensamento, ela foi criada para eliminar ameaças antes mesmo que pudessem reagir. " +
                    "Sozinha, já é considerada uma arma divina.\n\n" +
                    "Mas sua verdadeira força apenas desperta ao lado de sua irmã.",
        baseStats = mapOf(
            Stat.ATK to 125.0,
            Stat.CRIT_CHANCE to 0.15,
            Stat.CRIT_DAMAGE to 0.8,
        ),
        statsPerLevel = mapOf(),
        abilities = listOf(
            Ability(
                name = "Benção das laminas irmãs",
                type = AbilityType.PASSIVE,
                once = true,
                trigger = AbilityTrigger.OnBattleStart,
                effects = listOf(
                    Effect.Custom("Double twin stats") { self, target, state, team ->
                        val twin = self.equipments.find { it.id == "KONO_TWINBLADE_L" }

                        if (twin != null) {
                            state.combatLog += "⚔️ As laminas gemêas estão juntas novamente... seu poder real foi despertado"

                            val int = self.stats[Stat.INT] ?: 0.0
                            self.stats[Stat.INT] = int * 2.0

                            val speed = self.stats[Stat.SPEED] ?: 0.0
                            self.stats[Stat.SPEED] = speed * 2.0

                            val hp = self.stats[Stat.HP] ?: 0.0
                            self.stats[Stat.HP] = hp * 2.0
                        }
                    }
                )
            ),
            Ability(
                name = "Benção das laminas irmãs",
                type = AbilityType.PASSIVE,
                once = true,
                trigger = AbilityTrigger.OnBattleStart,
                effects = listOf(
                    Effect.Custom("Double twin stats") { self, target, state, team ->
                        val twin = self.equipments.find { it.id == "KONO_TWINBLADE_L" }

                        if (twin != null) {
                            state.combatLog += "⚔️ As laminas gemêas estão juntas novamente... seu poder real foi despertado"

                            val int = self.stats[Stat.INT] ?: 0.0
                            self.stats[Stat.INT] = int * 2.0

                            val speed = self.stats[Stat.SPEED] ?: 0.0
                            self.stats[Stat.SPEED] = speed * 2.0

                            val hp = self.stats[Stat.HP] ?: 0.0
                            self.stats[Stat.HP] = hp * 2.0
                        }
                    }
                )
            ),
            Ability(
                name = "Redemoinho das Lâminas Gêmeas",
                description = "Quando ambas as lâminas estão presentes, a cada 3 ataques desencadeia um giro devastador — 4 golpes alternados: físico (INT), mágico (ATK), físico (INT), mágico (ATK) — causando 45% por golpe.",
                type = AbilityType.PASSIVE,
                trigger = AbilityTrigger.OnAttackEvery(3),
                effects = listOf(
                    Effect.Custom("Twin spin attack") { self, target, state, team ->
                        val twin = self.equipments.find { it.id == "KONO_TWINBLADE_L" }
                        if (twin == null || target == null || target.hp <= 0) return@Custom

                        val intStat = self.stats[Stat.INT] ?: 0.0
                        val atkStat = self.stats[Stat.ATK] ?: 0.0
                        val physDamage = intStat * 0.45
                        val magicDamage = atkStat * 0.45

                        state.combatLog += "🌀 ${self.card.name} desencadeia o Redemoinho das Lâminas Gêmeas!"

                        // Physical (INT) → Magic (ATK) → Physical (INT) → Magic (ATK)
                        state.queue.add(
                            CombatEvent.BeforeDamage(
                                source = self,
                                target = target,
                                damage = physDamage,
                                damageType = DamageType.PHYSICAL,
                                canCrit = false
                            )
                        )
                        state.queue.add(
                            CombatEvent.BeforeDamage(
                                source = self,
                                target = target,
                                damage = magicDamage,
                                damageType = DamageType.MAGIC,
                                canCrit = false
                            )
                        )
                        state.queue.add(
                            CombatEvent.BeforeDamage(
                                source = self,
                                target = target,
                                damage = physDamage,
                                damageType = DamageType.PHYSICAL,
                                canCrit = false
                            )
                        )
                        state.queue.add(
                            CombatEvent.BeforeDamage(
                                source = self,
                                target = target,
                                damage = magicDamage,
                                damageType = DamageType.MAGIC,
                                canCrit = false
                            )
                        )
                    }
                )
            )
        ),
    )

    val konoTwinbladeL = CardDefinition(
        id = "KONO_TWINBLADE_L",
        name = "Lamina Gemea do Crepusculo",
        rarity = Rarity.MYTHIC,
        type = CardType.EQUIPMENT,
        slot = EquipmentSlot.SECONDARY,
        description = "A lâmina esquerda da Espada Gêmea de Kono. " +
                "Silenciosa e mortal, seus golpes jamais desperdiçam movimento. " +
                "Dizem que ela corta não apenas carne, mas intenção.\n\n" +
                "Mesmo separada de sua contraparte, ainda carrega poder suficiente para destruir exércitos.\n\n" +
                "Quando ambas as lâminas lutam juntas… batalhas terminam antes mesmo de começarem.",
        baseStats = mapOf(
            Stat.INT to 125.0,
            Stat.SPEED to 25.0,
            Stat.HP to 125.0
        ),
        statsPerLevel = mapOf(),
        abilities = listOf(
            Ability(
                name = "Benção das laminas irmãs",
                type = AbilityType.PASSIVE,
                once = true,
                trigger = AbilityTrigger.OnBattleStart,
                effects = listOf(
                    Effect.Custom("Double twin stats") { self, _, _, _ ->
                        val twin = self.equipments.find { it.id == "KONO_TWINBLADE_R" }

                        if (twin != null) {
                            val atk = self.stats[Stat.ATK] ?: 0.0
                            self.stats[Stat.ATK] = atk * 2.0

                            val critRate = self.stats[Stat.CRIT_CHANCE] ?: 0.0
                            self.stats[Stat.CRIT_CHANCE] = critRate * 2.0

                            val critDamage = self.stats[Stat.CRIT_DAMAGE] ?: 0.0
                            self.stats[Stat.CRIT_DAMAGE] = critDamage * 2.0
                        }
                    }
                )
            )
        ),
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
        aurum,
        lumina,
        // Characters — Legendary
        veyn,
        ironGargoyle,
        // Characters — Mythic
        markus,
        unleashedJuniorKnight,
        // sami,

        // Characters — Kono
        kono,
        dummy,
        konoSister,

        // Equipment — Common
        woodenSword, dagger, ironArmor, ironTorc,
        // Equipment — Rare
        ironSword, ironShield, heavyIronArmor,
        katana, vampireRing, quickBoots,
        magicCrystal, reinforcedPauldrons,
        // Equipment — Epic
        polishedKatana, vampireCore, greatsword,
        gamblerCharm, devotionStaff, thornmail,
        elixirVial, heavySteelBoots, swiftHunterCloth,
        bulwarkShield,
        // Equipment — Legendary
        critfish, demonHunterCrossbow, allInEmblem,
        siegebreaker, twinFangKatana,
        stormBoots, soulPendant, renouncedSwordCloth,
        // Equipment — Mythic
        undefined, sunGodGreatsword,
        // glacialOrb, samiStaff, samiCloth, samiBoots, frozenRose, // sami related

        konoTwinbladeL, konoTwinbladeR // kono
    )

    private val byId = all.associateBy { it.id }

    fun getById(id: String): CardDefinition? = byId[id.uppercase()]
}
