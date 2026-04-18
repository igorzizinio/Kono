package me.igorunderplayer.kono.engine.combat

import me.igorunderplayer.kono.domain.card.Stat
import me.igorunderplayer.kono.domain.card.StatSource
import me.igorunderplayer.kono.domain.card.ability.AbilityTarget
import me.igorunderplayer.kono.domain.card.ability.AbilityTrigger
import me.igorunderplayer.kono.domain.card.ability.AbilityType
import me.igorunderplayer.kono.domain.card.ability.Effect
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

                if (unit.hp <= 0) continue

                syncConditionalEffects(unit)

                for (ability in unit.abilities) {

                    if (ability.type != AbilityType.PASSIVE) continue
                    if (!isTriggeredForOwner(ability.trigger, unit, ability.name, event)) continue

                    val onceKey = onceAbilityKey(unit, ability)
                    if (ability.once && !state.onceTriggeredAbilityKeys.add(onceKey)) continue

                    state.combatLog += "✨ ${unitLabel(unit, state)} ativou [${ability.name}] (${ability.trigger})"

                    ability.effects.forEach { effect ->
                        applyEffect(effect, unit, event)
                    }
                }
            }
        }
    }

    private fun applyEffect(
        effect: Effect,
        owner: Unit,
        event: CombatEvent?
    ) {
        when (effect) {

            is Effect.Damage -> {
                val targets = resolveTargets(owner, effect.target, event)
                for (target in targets) {
                    enqueue(
                        CombatEvent.BeforeDamage(
                            source = owner,
                            target = target,
                            damage = effect.value
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
                            damage = damage
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
                        damage = increased
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

            is Effect.Random -> {
                processRandom(effect.profile, owner)
            }
        }
    }

    private fun resolveCoreEvent(event: CombatEvent) {

        when (event) {

            is CombatEvent.BattleStart -> {}

            is CombatEvent.Attack -> {
                state.combatLog += "⚔️ ${unitLabel(event.attacker, state)} atacando ${unitLabel(event.target, state)}"

                val damage = calculateDamage(event.attacker)

                enqueue(
                    CombatEvent.BeforeDamage(
                        source = event.attacker,
                        target = event.target,
                        damage = damage
                    )
                )
            }

            is CombatEvent.BeforeDamage -> {

                val finalDamage = event.damage // aqui depois entram modifiers

                event.target.hp -= finalDamage

                state.combatLog += "💥 ${unitLabel(event.source, state)} causou ${"%.1f".format(finalDamage)} de dano em ${unitLabel(event.target, state)} (${"%.1f".format(event.target.hp.coerceAtLeast(0.0))} HP restante)"

                enqueue(
                    CombatEvent.AfterDamage(
                        source = event.source,
                        target = event.target,
                        damage = finalDamage
                    )
                )

                if (event.target.hp <= 0.0) {
                    enqueue(CombatEvent.Death(event.target))
                }
            }

            is CombatEvent.Death -> {
                state.combatLog += "☠️ ${unitLabel(event.unit, state)} foi derrotado!"
            }

            else -> {}
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

    private fun selectTargetBySlotPriority(attacker: Unit, enemyTeam: TeamState): Unit? {
        val aliveEnemies = enemyTeam.aliveUnits()
        if (aliveEnemies.isEmpty()) return null

        // Base 3x3 behavior: try mirrored slot first, then nearest slot, then fallback to first alive.
        enemyTeam.aliveUnitBySlot(attacker.slot)?.let { return it }

        val nearestBySlot = aliveEnemies
            .minByOrNull { kotlin.math.abs(it.slot - attacker.slot) }
        if (nearestBySlot != null) return nearestBySlot

        return aliveEnemies.firstOrNull()
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
            else -> processDefaultRandom(owner)
        }
    }

    private fun processDefaultRandom(owner: Unit) {
        if (state.rng.nextDouble() < 0.5) {
            heal(owner, 10.0)
            state.combatLog += "🎲 ${owner.card.name} rolou sorte e curou 10 HP."
        } else {
            owner.hp -= 5.0
            state.combatLog += "🎲 ${owner.card.name} rolou azar e perdeu 5 HP."
        }
    }

    private fun processUndefinedRandom(owner: Unit) {
        val enemy = findTarget(owner)
        when (state.rng.nextInt(4)) {
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
            else -> {
                if (enemy != null) {
                    heal(enemy, 60.0)
                    state.combatLog += "🌀 ${owner.card.name} curou o inimigo em 60 HP."
                }
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
                state.combatLog += "🎰 ${owner.card.name} converteu moedas em ${damage.toInt()} dano."
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
}


