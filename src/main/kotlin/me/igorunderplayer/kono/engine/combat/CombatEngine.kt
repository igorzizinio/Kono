package me.igorunderplayer.kono.engine.combat

import me.igorunderplayer.kono.domain.card.Stat
import me.igorunderplayer.kono.domain.card.StatSource
import me.igorunderplayer.kono.domain.card.ability.AbilityTarget
import me.igorunderplayer.kono.domain.card.ability.AbilityTrigger
import me.igorunderplayer.kono.domain.card.ability.AbilityType
import me.igorunderplayer.kono.domain.card.ability.DamageType
import me.igorunderplayer.kono.domain.card.ability.Effect
import me.igorunderplayer.kono.domain.card.ability.ScalingMode
import me.igorunderplayer.kono.domain.gameplay.CombatEvent
import me.igorunderplayer.kono.domain.gameplay.CombatState
import me.igorunderplayer.kono.domain.gameplay.Unit
import me.igorunderplayer.kono.domain.team.TeamState


class CombatEngine(
    private val state: CombatState
) {

    private var battleStartProcessed = false

    fun run() {
        processBattleStart()
        while (!state.isFinished()) {
            processTurn()
        }
    }

    fun processNextTurn() {
        if (state.isFinished()) return
        processBattleStart()
        processTurn()
    }

    private fun processTurn() {

        state.combatLog += "\n🔄 ===== TURNO ${state.turn} ====="

        val units = state.teams
            .flatMap { it.units }
            .filter { it.hp > 0 }
            .sortedByDescending { it.stats[Stat.SPEED] ?: 0.0 }

        for (unit in units) {

            if (unit.hp <= 0) continue

            enqueue(CombatEvent.TurnStart(unit))
            drainQueue()

            val attackCount = resolveAttackCount(unit)

            repeat(attackCount) {
                val target = findTarget(unit) ?: return@repeat
                enqueue(CombatEvent.Attack(unit, target))
                drainQueue()
            }

            if (state.isFinished()) return
        }

        processTemporaryModifiersEndOfRound()

        state.turn++
    }

    private fun enqueue(event: CombatEvent) {
        state.queue.add(event)
    }

    private fun processBattleStart() {
        if (battleStartProcessed) return

        battleStartProcessed = true

        // Foundation for upcoming 3x3: every team enters combat with deterministic slots.
        state.teams.forEach { it.normalizeSlots() }

        enqueue(CombatEvent.BattleStart)
        drainQueue()
    }

    private fun drainQueue() {

        while (state.queue.isNotEmpty()) {
            val event = state.queue.removeFirst()
            state.eventHistory += event

            processAbilities(event)
            resolveCoreEvent(event)
        }
    }

    private fun processAbilities(event: CombatEvent) {

        for (team in state.teams) {
            for (unit in team.units) {

                if (unit.hp <= 0 && event !is CombatEvent.Death) continue

                syncConditionalEffects(unit)

                for (ability in unit.abilities) {

                    if (ability.type != AbilityType.PASSIVE) continue
                    if (!isTriggeredForOwner(ability.trigger, unit, ability.name, event)) continue

                    val onceKey = onceAbilityKey(unit, ability)
                    if (ability.once && !state.onceTriggeredAbilityKeys.add(onceKey)) continue

                    state.combatLog += "✨ ${unitLabel(unit, state)} ativou [${ability.name}] (${ability.trigger})"

                    ability.effects.forEach { effect ->
                        applyEffect(effect, unit, event, ability.name)
                    }
                }
            }
        }
    }

    private fun applyEffect(
        effect: Effect,
        owner: Unit,
        event: CombatEvent?,
        abilityName: String? = null
    ) {
        when (effect) {

            is Effect.Damage -> {
                val targets = resolveTargets(owner, effect.target, event)
                for (target in targets) {
                    val damage = when (effect.damageType) {
                        DamageType.PHYSICAL,
                        DamageType.MAGIC,
                        DamageType.TRUE -> effect.value
                    }

                    enqueue(
                        CombatEvent.BeforeDamage(
                            source = owner,
                            target = target,
                            damage = damage,
                            damageType = effect.damageType,
                            canCrit = effect.damageType == DamageType.PHYSICAL || effect.damageType == DamageType.MAGIC,
                            canBeDodged = effect.damageType == DamageType.PHYSICAL || effect.damageType == DamageType.MAGIC
                        )
                    )
                }
            }

            is Effect.DamageBasedOnStat -> {
                val targets = resolveTargets(owner, effect.target, event)

                for (target in targets) {
                    val statValue = when (effect.statSource) {
                        StatSource.SELF -> owner.stats[effect.stat]
                        StatSource.TARGET -> target.stats[effect.stat]
                    } ?: 0.0

                    val damage = statValue * effect.scaling
                    if (damage <= 0) continue

                    enqueue(
                        CombatEvent.BeforeDamage(
                            source = owner,
                            target = target,
                            damage = damage,
                            damageType = effect.damageType,
                            canCrit = effect.damageType == DamageType.PHYSICAL || effect.damageType == DamageType.MAGIC,
                            canBeDodged = effect.damageType == DamageType.PHYSICAL || effect.damageType == DamageType.MAGIC
                        )
                    )
                }
            }
            is Effect.DamageIncreasePercent -> {
                if (event !is CombatEvent.BeforeDamage) return

                val increased = event.damage * (1 + effect.value)

                enqueue(
                    CombatEvent.BeforeDamage(
                        source = event.source,
                        target = event.target,
                        damage = increased,
                        damageType = event.damageType,
                        canCrit = event.canCrit,
                        canBeDodged = event.canBeDodged,
                        sourceAbilityType = event.sourceAbilityType
                    )
                )
            }

            is Effect.Heal -> {
                val targets = resolveTargets(owner, effect.target, event)
                for (target in targets) {
                    heal(target, effect.value)
                }
            }

            is Effect.BuffStat -> {
                val targets = resolveTargets(owner, effect.target, event)
                for (target in targets) {
                    val before = target.stats[effect.stat] ?: 0.0
                    val after = before + effect.value

                    target.stats[effect.stat] = after

                    state.combatLog += "📈 ${unitLabel(target, state)} ganhou +${effect.value} ${effect.stat} (${"%.1f".format(after)})"
                }
            }

            is Effect.StatIncreasePercent -> {
                val targets = resolveTargets(owner, effect.target, event)
                for (target in targets) {
                    val before = target.stats[effect.stat] ?: 0.0
                    val bonus = before * effect.percent
                    val after = before + bonus

                    target.stats[effect.stat] = after

                    state.combatLog += "📈 ${unitLabel(target, state)} ganhou +${effect.percent * 100}% ${effect.stat} (${"%.1f".format(after)})"
                }
            }

            is Effect.StatIncreaseWhileBelowHealth -> {
                syncConditionalStatIncrease(owner, effect)
            }

            is Effect.AddCoins -> {
                val team = findTeam(owner) ?: return
                val granted = resolveCoinsWithGangSynergy(owner, team, effect)
                if (granted <= 0) return

                team.addCoins(granted)
                state.combatLog += "💰 ${unitLabel(owner, state)} gerou $granted moeda(s) (Time ${team.id}: ${team.coins()})"
            }

            is Effect.AddCoinsScaling -> {
                val team = findTeam(owner) ?: return
                val granted = resolveScaledCoins(owner, team, effect)
                if (granted <= 0) return

                team.addCoins(granted)
                state.combatLog += "💰 ${owner.card.name} gerou $granted moeda(s) escaladas para o time ${team.id}. Total: ${team.coins()}"
            }

            is Effect.BuffStatByTeamCoins -> {
                val team = findTeam(owner) ?: return
                val targets = resolveTargets(owner, effect.target, event)
                if (targets.isEmpty()) return

                val stacks = resolveCoinStacks(team.coins(), effect.coinsPerStack, effect.maxStacks)
                val desiredValue = when (effect.mode) {
                    ScalingMode.STACK -> stacks * effect.valuePerStack
                    ScalingMode.HIGHEST_ONLY -> if (stacks > 0) effect.valuePerStack else 0.0
                }

                for (target in targets) {
                    val key = "scale:${owner.id}:${abilityName ?: "unknown"}:${target.id}:${effect.stat.name}"
                    val currentApplied = state.dynamicScaleAppliedValueByKey[key] ?: 0.0
                    val delta = desiredValue - currentApplied
                    if (delta == 0.0) continue

                    target.stats[effect.stat] = (target.stats[effect.stat] ?: 0.0) + delta
                    state.dynamicScaleAppliedValueByKey[key] = desiredValue
                }
            }

            is Effect.ProtectAlliesDamageShare -> {
                val share = effect.sharePercent.coerceIn(0.0, 0.95)
                state.protectorShareByUnitId[owner.id] = share
                state.combatLog += "🛡️ ${owner.card.name} agora protege aliados e absorve ${(share * 100).toInt()}% do dano recebido por eles."
            }

            Effect.Taunt -> {
                state.tauntByUnitId += owner.id
                state.combatLog += "🎯 ${owner.card.name} provocou os inimigos e virou alvo prioritário."
            }

            is Effect.ExecuteBellowHealth -> {
                val target = resolveTargets(owner, effect.target, event).firstOrNull() ?: return

                val maxHp = target.stats[Stat.HP] ?: 0.0
                val currentHp = target.hp

                if (currentHp <= maxHp * effect.threshold) {
                    target.hp = 0.0

                    enqueue(
                        CombatEvent.Death(
                            target
                        )
                    )
                }
            }

            is Effect.Random -> {
                processRandom(effect.profile, owner)
            }
        }
    }

    private fun resolveCoreEvent(event: CombatEvent) {

        when (event) {

            is CombatEvent.BattleStart -> {}

            is CombatEvent.TurnStart -> {}

            is CombatEvent.Attack -> {
                state.attackCountByUnitId[event.attacker.id] = (state.attackCountByUnitId[event.attacker.id] ?: 0) + 1

                state.combatLog += "⚔️ ${unitLabel(event.attacker, state)} atacando ${unitLabel(event.target, state)}"

                val damage = calculateDamage(event.attacker)

                enqueue(
                    CombatEvent.BeforeDamage(
                        source = event.attacker,
                        target = event.target,
                        damage = damage,
                        damageType = DamageType.PHYSICAL
                    )
                )
            }

            is CombatEvent.BeforeDamage -> {
                val incomingDamage = resolveMitigatedDamage(event.damage, event.target, event.damageType)
                if (incomingDamage <= 0.0) return

                val guardian = resolveGuardianFor(event.target)
                val share = guardian?.let { state.protectorShareByUnitId[it.id] ?: 0.0 } ?: 0.0
                val redirectedDamage = (incomingDamage * share).coerceAtMost(incomingDamage)
                val targetDamage = (incomingDamage - redirectedDamage).coerceAtLeast(0.0)

                applyDamageInstance(
                    source = event.source,
                    target = event.target,
                    damage = targetDamage,
                    damageType = event.damageType,
                    sourceAbilityType = event.sourceAbilityType
                )

                if (guardian != null && redirectedDamage > 0.0) {
                    state.combatLog += "🛡️ ${guardian.card.name} interceptou ${redirectedDamage.toInt()} de dano para proteger ${event.target.card.name}."
                    applyDamageInstance(
                        source = event.source,
                        target = guardian,
                        damage = redirectedDamage,
                        damageType = event.damageType,
                        sourceAbilityType = event.sourceAbilityType
                    )
                }
            }

            is CombatEvent.AfterDamage -> {
                val damage = event.damage.coerceAtLeast(0.0)

                event.source.stats[Stat.LIFESTEAL]?.let {
                    if (it > 0.0) {
                        val healAmount = damage * it
                        heal(event.source, healAmount)
                    }
                }
            }

            is CombatEvent.Death -> {
                if (event.unit.hp > 0) return

                state.protectorShareByUnitId.remove(event.unit.id)
                state.tauntByUnitId.remove(event.unit.id)
                state.combatLog += "☠️ ${unitLabel(event.unit, state)} foi derrotado!"
            }
        }
    }

    private fun applyDamageInstance(
        source: Unit,
        target: Unit,
        damage: Double,
        damageType: DamageType = DamageType.PHYSICAL,
        sourceAbilityType: AbilityType? = null
    ) {
        val finalDamage = damage.coerceAtLeast(0.0)
        if (finalDamage <= 0.0 || target.hp <= 0.0) return

        target.hp -= finalDamage

        val damageLabel = when (damageType) {
            DamageType.PHYSICAL -> "dano físico"
            DamageType.MAGIC -> "dano mágico"
            DamageType.TRUE -> "dano verdadeiro"
        }
        state.combatLog += "💥 ${unitLabel(source, state)} causou ${"%.1f".format(finalDamage)} de $damageLabel em ${unitLabel(target, state)} (${"%.1f".format(target.hp.coerceAtLeast(0.0))} HP restante)"

        enqueue(
            CombatEvent.AfterDamage(
                source = source,
                target = target,
                damage = finalDamage,
                damageType = damageType,
                sourceAbilityType = sourceAbilityType
            )
        )

        val sourceTeam = findTeam(source)
        val targetTeam = findTeam(target)
        if (sourceTeam != null && targetTeam != null && sourceTeam != targetTeam) {
            state.lastDamageSourceByTeamId[targetTeam.id] = source.id
            state.lastDamageTurnByUnitId[source.id] = state.turn
        }

        if (target.hp <= 0.0) {
            enqueue(CombatEvent.Death(target))
        }
    }

    private fun isTriggeredForOwner(
        trigger: AbilityTrigger,
        owner: Unit,
        abilityName: String,
        event: CombatEvent
    ): Boolean {
        return when (trigger) {
            AbilityTrigger.OnBattleStart -> event is CombatEvent.BattleStart
            AbilityTrigger.OnTurnStart -> event is CombatEvent.TurnStart && event.unit == owner
            is AbilityTrigger.OnTurnEvery -> {
                event is CombatEvent.TurnStart && event.unit == owner && state.turn % trigger.turns.coerceAtLeast(1) == 0
            }
            AbilityTrigger.OnAttack -> event is CombatEvent.Attack && event.attacker == owner
            is AbilityTrigger.OnAttackEvery -> {
                if (event !is CombatEvent.Attack || event.attacker != owner) {
                    false
                } else {
                    val interval = trigger.attacks.coerceAtLeast(1)
                    val key = "attack:${owner.id}:$abilityName"
                    val currentCount = (state.hitCounterByAbilityKey[key] ?: 0) + 1
                    state.hitCounterByAbilityKey[key] = currentCount
                    currentCount % interval == 0
                }
            }
            is AbilityTrigger.OnAttackAgainstTag -> {
                if (event !is CombatEvent.Attack || event.attacker != owner) {
                    false
                } else {
                    event.target.tags.any { it.equals(trigger.tag, ignoreCase = true) }
                }
            }
            AbilityTrigger.OnDamageTaken -> event is CombatEvent.BeforeDamage && event.target == owner
            AbilityTrigger.OnDamageDealt -> event is CombatEvent.AfterDamage && event.source == owner
            is AbilityTrigger.OnBellowHealth -> {
                if (event !is CombatEvent.AfterDamage || event.target != owner) {
                    false
                } else {
                    val maxHp = owner.stats[Stat.HP] ?: return false
                    if (maxHp <= 0.0) return false

                    val alreadyTriggeredKey = "triggered:${owner.id}:$abilityName"
                    if (state.globalFlags[alreadyTriggeredKey] == true) {
                        false
                    } else {
                        val ratio = owner.hp / maxHp
                        val shouldTrigger = ratio <= trigger.threshold
                        if (shouldTrigger) {
                            state.globalFlags[alreadyTriggeredKey] = true
                        }
                        shouldTrigger
                    }
                }
            }
            AbilityTrigger.OnDeath -> event is CombatEvent.Death && event.unit == owner
            AbilityTrigger.Manual -> false
        }
    }

    private fun syncConditionalEffects(unit: Unit) {
        for (ability in unit.abilities) {
            for (effect in ability.effects) {
                if (effect is Effect.StatIncreaseWhileBelowHealth) {
                    syncConditionalStatIncrease(unit, effect, ability.name)
                }
            }
        }
    }

    private fun syncConditionalStatIncrease(
        owner: Unit,
        effect: Effect.StatIncreaseWhileBelowHealth,
        abilityName: String? = null
    ) {
        val maxHp = owner.stats[Stat.HP] ?: return
        if (maxHp <= 0.0) return

        val shouldBeActive = (owner.hp / maxHp) <= effect.threshold
        val key = "cond:${owner.id}:${abilityName ?: effect.stat.name}:${effect.stat.name}"
        val isActive = state.conditionalEffectStatesByKey[key] == true

        if (shouldBeActive == isActive) return

        val current = owner.stats[effect.stat] ?: 0.0
        owner.stats[effect.stat] = if (shouldBeActive) current + effect.value else current - effect.value
        state.conditionalEffectStatesByKey[key] = shouldBeActive

        val status = if (shouldBeActive) "ativou" else "desativou"
        state.combatLog += "✨ ${unitLabel(owner, state)} $status ${abilityName ?: effect.stat.name}."
    }

    private fun onceAbilityKey(unit: Unit, ability: me.igorunderplayer.kono.domain.card.ability.Ability): String {
        return "once:${unit.id}:${unit.card.id}:${ability.name}"
    }

    private fun heal(unit: Unit, amount: Double) {
        val maxHp = unit.stats[Stat.HP] ?: return
        val before = unit.hp
        unit.hp = (unit.hp + amount).coerceAtMost(maxHp)
        val healed = unit.hp - before

        if (healed > 0) {
            state.combatLog += "💚 ${unitLabel(unit, state)} curou ${"%.1f".format(healed)} HP (${"%.1f".format(unit.hp)} HP)"
        }
    }

    private fun findTeam(unit: Unit) =
        state.teams.firstOrNull { it.units.contains(unit) }

    private fun findTarget(unit: Unit): Unit? {
        val ownerTeam = findTeam(unit) ?: return null
        val enemy = state.teams.firstOrNull { it != ownerTeam } ?: return null

        return selectTargetBySlotPriority(attacker = unit, enemyTeam = enemy)
    }

    private fun resolveGuardianFor(target: Unit): Unit? {
        val team = findTeam(target) ?: return null
        val protectors = team.units
            .filter { it.hp > 0 && it.id != target.id }
            .mapNotNull { protector ->
                val share = state.protectorShareByUnitId[protector.id] ?: return@mapNotNull null
                if (share <= 0.0) return@mapNotNull null
                protector to share
            }

        if (protectors.isEmpty()) return null

        return protectors
            .maxWithOrNull(compareBy<Pair<Unit, Double>> { it.second }.thenBy { it.first.hp })
            ?.first
    }

    private fun selectTargetBySlotPriority(attacker: Unit, enemyTeam: TeamState): Unit? {
        val aliveEnemies = enemyTeam.aliveUnits()
        if (aliveEnemies.isEmpty()) return null

        val tauntingEnemies = aliveEnemies.filter { it.id in state.tauntByUnitId }
        val candidates = if (tauntingEnemies.isNotEmpty()) tauntingEnemies else aliveEnemies

        val ownerTeam = findTeam(attacker)
        val revengeTargetId = ownerTeam?.let { state.lastDamageSourceByTeamId[it.id] }
        if (revengeTargetId != null) {
            candidates.firstOrNull { it.id == revengeTargetId }?.let { return it }
        }

        val threatenedTarget = candidates.maxWithOrNull(
            compareBy<Unit> { state.attackCountByUnitId[it.id] ?: 0 }
                .thenByDescending { state.lastDamageTurnByUnitId[it.id] ?: 0 }
                .thenBy { kotlin.math.abs(it.slot - attacker.slot) }
        )
        if (threatenedTarget != null) return threatenedTarget

        // Base 3x3 behavior: try mirrored slot first, then nearest slot, then fallback to first alive.
        enemyTeam.aliveUnitBySlot(attacker.slot)?.takeIf { it in candidates }?.let { return it }

        val nearestBySlot = candidates
            .minByOrNull { kotlin.math.abs(it.slot - attacker.slot) }
        if (nearestBySlot != null) return nearestBySlot

        return candidates.firstOrNull()
    }

    private fun resolveTargets(owner: Unit, target: AbilityTarget, event: CombatEvent?): List<Unit> {
        val ownerTeam = findTeam(owner) ?: return emptyList()
        val enemyTeam = state.teams.firstOrNull { it != ownerTeam }

        return when (target) {
            AbilityTarget.SELF -> listOf(owner)
            AbilityTarget.ENEMY -> {
                val eventTarget = when (event) {
                    is CombatEvent.Attack -> event.target
                    is CombatEvent.BeforeDamage -> event.target
                    is CombatEvent.AfterDamage -> event.target
                    else -> null
                }

                listOfNotNull(eventTarget?.takeIf { it.hp > 0 } ?: findTarget(owner))
            }
            AbilityTarget.ALLY -> {
                ownerTeam.units.firstOrNull { it != owner && it.hp > 0 }?.let { listOf(it) } ?: listOf(owner)
            }
            AbilityTarget.ALL_ENEMIES -> enemyTeam?.units?.filter { it.hp > 0 } ?: emptyList()
            AbilityTarget.ALL_ALLIES -> ownerTeam.units.filter { it.hp > 0 }
        }
    }

    private fun processRandom(profile: String, owner: Unit) {
        when (profile.uppercase()) {
            "MARKUS_GAMBLER" -> processMarkusRandom(owner)
            "UNDEFINED_BUG" -> processUndefinedRandom(owner)
            "GAMBLER_CHARM" -> processGamblerCharmRandom(owner)
            else -> processDefaultRandom(owner)
        }
    }

    private fun processDefaultRandom(owner: Unit) {
        val hpAmount = 20.0

        if (state.rng.nextDouble() < 0.5) {
            heal(owner, hpAmount)
            state.combatLog += "🎲 ${owner.card.name} rolou sorte e curou $hpAmount HP."
        } else {
            owner.hp -= hpAmount
            state.combatLog += "🎲 ${owner.card.name} rolou azar e perdeu $hpAmount HP."
        }
    }

    private fun processGamblerCharmRandom(owner: Unit) {
        val enemy = findTarget(owner)

        // escolhe alvo aleatório (owner ou enemy)
        val target = if (enemy != null && state.rng.nextBoolean()) enemy else owner

        val roll = state.rng.nextDouble()

        when {
            // 🟢 Cura
            roll < 0.25 -> {
                val healAmount = 60.0
                heal(target, healAmount)
                state.combatLog += "🍀 ${owner.card.name} girou a roleta e ${if (target == owner) "se curou" else "curou o inimigo"} em $healAmount HP."
            }

            // 🔴 Dano
            roll < 0.50 -> {
                if (enemy != null) {
                    val damage = 55.0
                    enqueue(CombatEvent.BeforeDamage(source = owner, target = target, damage = damage))
                    state.combatLog += "🍀 ${owner.card.name} causou $damage de dano em ${if (target == owner) "si mesmo" else "o inimigo"}."
                }
            }

            // 💪 Buff ATK
            roll < 0.70 -> {
                val percent = 0.30
                val current = target.stats[Stat.ATK] ?: 0.0
                val bonus = current * percent

                applyTemporaryStatModifier(
                    target = target,
                    stat = Stat.ATK,
                    delta = bonus,
                    durationRounds = 1,
                    source = "GAMBLER_CHARM_ATK"
                )

                state.combatLog += "🍀 ${owner.card.name} aumentou ATK de ${unitLabel(target, state)} em +${(percent * 100).toInt()}% por 1 rodada."
            }

            // 🛡️ Buff DEF
            roll < 0.85 -> {
                val percent = 0.30
                val current = target.stats[Stat.DEF] ?: 0.0
                val bonus = current * percent

                applyTemporaryStatModifier(
                    target = target,
                    stat = Stat.DEF,
                    delta = bonus,
                    durationRounds = 1,
                    source = "GAMBLER_CHARM_DEF"
                )

                state.combatLog += "🍀 ${owner.card.name} aumentou DEF de ${unitLabel(target, state)} em +${(percent * 100).toInt()}% por 1 rodada."
            }

            // ☠️ Debuff DEF
            else -> {
                val percent = 0.35
                val current = target.stats[Stat.DEF] ?: 0.0
                val reduction = current * percent
                val appliedReduction = reduction.coerceAtMost(current)

                applyTemporaryStatModifier(
                    target = target,
                    stat = Stat.DEF,
                    delta = -appliedReduction,
                    durationRounds = 1,
                    source = "GAMBLER_CHARM_DEF_DEBUFF"
                )

                state.combatLog += "🍀 ${owner.card.name} reduziu DEF de ${unitLabel(target, state)} em -${(percent * 100).toInt()}% por 1 rodada!"
            }
        }
    }

    private fun applyTemporaryStatModifier(
        target: Unit,
        stat: Stat,
        delta: Double,
        durationRounds: Int,
        source: String
    ) {
        if (delta == 0.0) return

        val safeDuration = durationRounds.coerceAtLeast(1)
        val current = target.stats[stat] ?: 0.0
        target.stats[stat] = current + delta

        state.temporaryStatModifiers += me.igorunderplayer.kono.domain.gameplay.TemporaryStatModifier(
            unitId = target.id,
            stat = stat,
            delta = delta,
            remainingRounds = safeDuration,
            source = source
        )
    }

    private fun processTemporaryModifiersEndOfRound() {
        if (state.temporaryStatModifiers.isEmpty()) return

        val iterator = state.temporaryStatModifiers.iterator()
        while (iterator.hasNext()) {
            val modifier = iterator.next()
            modifier.remainingRounds -= 1

            if (modifier.remainingRounds > 0) continue

            val unit = findUnitById(modifier.unitId)
            if (unit != null) {
                val current = unit.stats[modifier.stat] ?: 0.0
                unit.stats[modifier.stat] = current - modifier.delta

                state.combatLog += "⌛ ${unitLabel(unit, state)} perdeu o efeito temporario de ${modifier.stat}."
            }

            iterator.remove()
        }
    }

    private fun findUnitById(unitId: String): Unit? {
        return state.teams
            .flatMap { it.units }
            .firstOrNull { it.id == unitId }
    }

    private fun processUndefinedRandom(owner: Unit) {
        val enemy = findTarget(owner)

        if (state.rng.nextDouble() < 0.01) {
            val enemy = findTarget(owner)

            if (enemy != null) {
                enemy.hp = 0.0
                enqueue(CombatEvent.Death(enemy))

                state.combatLog += "🌌 ${owner.card.name} invocou o fim absoluto e apagou ${enemy.card.name} da existência."
            }

            return
        }

        when (state.rng.nextInt(8)) {
            0 -> {
                owner.hp -= 70.0
                state.combatLog += "🌀 ${owner.card.name} sofreu 70 de dano caotico."
            }
            1 -> {
                heal(owner, 80.0)
                state.combatLog += "🌀 ${owner.card.name} recuperou 80 HP do caos."
            }
            2 -> {
                if (enemy != null) {
                    enqueue(CombatEvent.BeforeDamage(source = owner, target = enemy, damage = 90.0))
                    state.combatLog += "🌀 ${owner.card.name} disparou 90 de dano caotico."
                }
            }
            3 -> {
                if (enemy != null) {
                    heal(enemy, 60.0)
                    state.combatLog += "🌀 ${owner.card.name} curou o inimigo em 60 HP."
                }
            }
            4 -> {
                if (enemy != null) {
                    val temp = owner.hp
                    owner.hp = enemy.hp
                    enemy.hp = temp

                    state.combatLog += "🌀 ${owner.card.name} trocou sua vida com o inimigo."
                }
            }
            5 -> {
                val atk = owner.stats[Stat.ATK] ?: 0.0
                val bonus = atk * 0.5

                applyTemporaryStatModifier(
                    target = owner,
                    stat = Stat.ATK,
                    delta = bonus,
                    durationRounds = 1,
                    source = "UNDEFINED_CHAOS"
                )

                state.combatLog += "🌀 ${owner.card.name} recebeu poder caótico (+50% ATK por 1 turno)."
            }
            6 -> {
                val def = owner.stats[Stat.DEF] ?: 0.0
                val reduction = def * 0.5

                applyTemporaryStatModifier(
                    target = owner,
                    stat = Stat.DEF,
                    delta = -reduction,
                    durationRounds = 1,
                    source = "UNDEFINED_CHAOS"
                )

                state.combatLog += "🌀 ${owner.card.name} teve sua defesa corrompida (-50% DEF)."
            }
            7 -> {
                val allUnits = state.teams.flatMap { it.units }.filter { it.hp > 0 }

                allUnits.forEach {
                    enqueue(
                        CombatEvent.BeforeDamage(
                            source = owner,
                            target = it,
                            damage = 60.0
                        )
                    )
                }

                state.combatLog += "🌀 O caos se espalhou e atingiu todos no campo."
            }
            else -> {
                state.combatLog += "Nada de caótico ocorreu nessa rodada.."
            }
        }
    }

    private fun processMarkusRandom(owner: Unit) {
        val team = findTeam(owner) ?: return
        val coins = team.coins()
        val enemy = findTarget(owner)

        when {
            enemy != null && state.rng.nextDouble() < 0.5 -> {
                val damage = 18.0 + (coins * 0.8)
                enqueue(CombatEvent.BeforeDamage(source = owner, target = enemy, damage = damage))
                state.combatLog += "🎰 ${owner.card.name} converteu moedas em ${damage.toInt()} dano mágico."
            }
            state.rng.nextDouble() < 0.8 -> {
                val speedBuff = 4.0 + (coins * 0.1)
                owner.stats[Stat.SPEED] = (owner.stats[Stat.SPEED] ?: 0.0) + speedBuff
                state.combatLog += "🎰 ${owner.card.name} ganhou +${speedBuff.toInt()} SPEED com a banca do time."
            }
            else -> {
                owner.hp -= 10.0
                state.combatLog += "🎰 ${owner.card.name} perdeu 10 HP numa aposta ruim."
            }
        }
    }

    private fun resolveCoinsWithGangSynergy(owner: Unit, team: TeamState, effect: Effect.AddCoins): Int {
        val base = effect.value.coerceAtLeast(0)
        if (!effect.scaleWithGangSynergy) return base
        if (!isMarkusGangMember(owner)) return base

        val gangMembersAlive = team.units.count { it.hp > 0 && isMarkusGangMember(it) }
        val bonus = (gangMembersAlive - 1).coerceAtLeast(0)
        return base + bonus
    }

    private fun resolveScaledCoins(owner: Unit, team: TeamState, effect: Effect.AddCoinsScaling): Int {
        val base = effect.base.coerceAtLeast(0)
        val scaledStacks = resolveCoinStacks(team.coins(), effect.coinsPerStack)
        val scaleBonus = (scaledStacks * effect.bonusPerStack).coerceAtLeast(0)

        val factionBonus = if (
            effect.allyFactionForBaseBonus.isNullOrBlank() ||
            effect.requiredAlliesForBaseBonus <= 0 ||
            effect.baseBonus <= 0
        ) {
            0
        } else {
            val allies = team.units.count {
                it != owner &&
                    it.hp > 0 &&
                    it.card.faction?.equals(effect.allyFactionForBaseBonus, ignoreCase = true) == true
            }
            if (allies >= effect.requiredAlliesForBaseBonus) effect.baseBonus else 0
        }

        return base + scaleBonus + factionBonus
    }

    private fun resolveCoinStacks(coins: Int, coinsPerStack: Int, maxStacks: Int? = null): Int {
        val step = coinsPerStack.coerceAtLeast(1)
        val stacks = (coins.coerceAtLeast(0) / step)
        return maxStacks?.coerceAtLeast(0)?.let { stacks.coerceAtMost(it) } ?: stacks
    }

    private fun isMarkusGangMember(unit: Unit): Boolean {
        val faction = unit.card.faction?.lowercase()
        if (faction == "gambler") return true

        return unit.tags.any {
            it.equals("gambler", ignoreCase = true) ||
                it.equals("markus", ignoreCase = true)
        }
    }

    private fun unitLabel(unit: Unit, state: CombatState): String {
        return state.unitDisplayNamesById[unit.id] ?: unit.card.name
    }

    private fun resolveAttackCount(unit: Unit): Int {

        val speed = unit.stats[Stat.SPEED] ?: 0.0
        val extra = ((speed - 120.0) / 100.0).coerceAtLeast(0.0)

        val guaranteed = extra.toInt()
        val chance = extra - guaranteed

        var result = 1 + guaranteed

        if (chance > 0 && state.rng.nextDouble() < chance) {
            result++
        }

        return result.coerceIn(1, 3)
    }

    private fun calculateDamage(unit: Unit): Double {
        return (unit.stats[Stat.ATK] ?: 0.0).coerceAtLeast(0.0)
    }

    private fun resolveMitigatedDamage(
        rawDamage: Double,
        target: Unit,
        damageType: DamageType
    ): Double {
        val damage = rawDamage.coerceAtLeast(0.0)
        if (damage <= 0.0) return 0.0
        if (damageType != DamageType.PHYSICAL) return damage

        val defense = (target.stats[Stat.DEF] ?: 0.0).coerceAtLeast(-90.0)
        val mitigationMultiplier = 100.0 / (100.0 + defense)
        return damage * mitigationMultiplier
    }
}


