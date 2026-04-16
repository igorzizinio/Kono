package me.igorunderplayer.kono.engine.combat

import me.igorunderplayer.kono.domain.card.ability.Ability
import me.igorunderplayer.kono.domain.card.ability.AbilityTarget
import me.igorunderplayer.kono.domain.card.ability.AbilityTrigger
import me.igorunderplayer.kono.domain.card.ability.AbilityType
import me.igorunderplayer.kono.domain.card.Stat
import me.igorunderplayer.kono.domain.gameplay.CombatEvent
import me.igorunderplayer.kono.domain.gameplay.CombatState
import me.igorunderplayer.kono.domain.gameplay.Unit

object AbilityProcessor {

    fun process(
        ability: Ability,
        abilityIndex: Int,
        owner: Unit,
        event: CombatEvent,
        state: CombatState,
        modifiers: MutableList<DamageModifier>
    ) {
        if (!matches(ability.trigger, event)) return

        when (ability.type) {

            AbilityType.LIFESTEAL -> {
                if (event is CombatEvent.AfterDamage && event.source == owner) {
                    if (event.damage <= 0.0) return
                    if (!shouldActivateByHitCycle(ability, owner, abilityIndex, state)) return

                    val value = ability.value ?: return
                    val heal = event.damage * (value / 100.0)
                    if (heal <= 0.0) return

                    val before = owner.hp
                    healUnit(owner, heal)
                    val healedAmount = (owner.hp - before).coerceAtLeast(0.0)
                    if (healedAmount > 0.0) {
                        state.combatLog += "🩸 ${unitLabel(owner, state)} drenou ${formatValue(healedAmount)} de vida."
                    }
                }
            }

            AbilityType.DAMAGE -> {
                queueDamageAbility(ability, abilityIndex, owner, event, state)
            }

            AbilityType.RNG_EFFECT -> {
                if (event is CombatEvent.TurnStart && event.unit == owner) {
                    processRandomEffect(ability, owner, state)
                }
            }

            AbilityType.INCOMING_DAMAGE_REDUCTION -> {
                if (event is CombatEvent.BeforeDamage && event.target == owner) {
                    addIncomingDamageReductionModifier(ability, modifiers)
                }
            }

            AbilityType.OUTGOING_DAMAGE_AMPLIFICATION -> {
                if (event is CombatEvent.BeforeDamage && event.source == owner) {
                    addOutgoingDamageAmplificationModifier(ability, modifiers)
                }
            }

            else -> {}
        }
    }

    private fun queueDamageAbility(
        ability: Ability,
        abilityIndex: Int,
        owner: Unit,
        event: CombatEvent,
        state: CombatState
    ) {
        val value = ability.value ?: return

        when (event) {
            is CombatEvent.Attack -> {
                if (event.attacker != owner) return
                val target = resolveTarget(ability.target, owner, event.target)
                queueAbilityDamageEvent(
                    owner = owner,
                    target = target,
                    baseDamage = value,
                    ability = ability,
                    state = state,
                    defaultTrueDamage = false
                )
            }

            is CombatEvent.AfterDamage -> {
                if (event.source != owner || event.damage <= 0.0) return
                if (event.sourceAbilityType == AbilityType.DAMAGE) return
                if (!shouldActivateByHitCycle(ability, owner, abilityIndex, state)) return

                val target = resolveTarget(ability.target, owner, event.target)
                queueAbilityDamageEvent(
                    owner = owner,
                    target = target,
                    baseDamage = value,
                    ability = ability,
                    state = state,
                    defaultTrueDamage = true
                )
            }

            else -> return
        }
    }

    private fun queueAbilityDamageEvent(
        owner: Unit,
        target: Unit,
        baseDamage: Double,
        ability: Ability,
        state: CombatState,
        defaultTrueDamage: Boolean
    ) {
        val isTrueDamage = ability.params?.trueDamage ?: defaultTrueDamage
        val canCrit = ability.params?.canCrit ?: !isTrueDamage
        val canBeDodged = ability.params?.canBeDodged ?: !isTrueDamage

        state.queue.add(
            CombatEvent.BeforeDamage(
                source = owner,
                target = target,
                damage = baseDamage,
                isTrueDamage = isTrueDamage,
                canCrit = canCrit,
                canBeDodged = canBeDodged,
                sourceAbilityType = ability.type
            )
        )
    }

    private fun resolveTarget(target: AbilityTarget?, owner: Unit, defaultTarget: Unit): Unit {
        return when (target) {
            AbilityTarget.SELF -> owner
            else -> defaultTarget
        }
    }

    private fun addIncomingDamageReductionModifier(
        ability: Ability,
        modifiers: MutableList<DamageModifier>
    ) {
        val value = ability.value ?: return
        modifiers.add { dmg -> dmg * (1 - value / 100.0) }
    }

    private fun addOutgoingDamageAmplificationModifier(
        ability: Ability,
        modifiers: MutableList<DamageModifier>
    ) {
        val value = ability.value ?: return
        modifiers.add { dmg -> dmg * (1 + value / 100.0) }
    }

    private fun matches(trigger: AbilityTrigger?, event: CombatEvent): Boolean {
        return when (trigger) {
            AbilityTrigger.ON_TURN_START -> event is CombatEvent.TurnStart
            AbilityTrigger.ON_ATTACK -> event is CombatEvent.Attack
            AbilityTrigger.ON_DAMAGE_TAKEN -> event is CombatEvent.BeforeDamage
            AbilityTrigger.ON_HIT -> event is CombatEvent.AfterDamage
            AbilityTrigger.PASSIVE -> true
            else -> false
        }
    }

    private fun processRandomEffect(
        ability: Ability,
        owner: Unit,
        state: CombatState
    ) {
        val profile = ability.params?.profile

        when (profile) {
            "UNDEFINED_BUG" -> {
                applyUndefinedEffect(owner, state, ability)
                return
            }

            "MARKUS_GAMBLER" -> {
                applyMarkusGamblerEffect(owner, state, ability)
                return
            }

            else -> {
                applyDefaultRandomEffect(owner, state)
            }
        }
    }

    private fun applyDefaultRandomEffect(owner: Unit, state: CombatState) {
        // Legacy RNG behavior (used by older items like Gambler Charm).
        val roll = state.rng.nextDouble()
        if (roll < 0.5) {
            healUnit(owner, 10.0)
        } else {
            owner.hp -= 5
        }
    }

    private fun applyMarkusGamblerEffect(owner: Unit, state: CombatState, ability: Ability) {
        val enemy = findPrimaryEnemy(owner, state)
        val coins = (state.luckCoinsByUnitId[owner.id] ?: 0) + 1
        state.luckCoinsByUnitId[owner.id] = coins

        state.combatLog += "🪙 ${unitLabel(owner, state)} acumulou 1 Moeda da Sorte (total: $coins)."

        val strongEveryTurns = ability.params?.strongEveryTurns?.coerceAtLeast(2) ?: 4
        val isStrongTurn = state.turn % strongEveryTurns == 0

        if (isStrongTurn) {
            state.combatLog += "🎰 ${unitLabel(owner, state)} entrou em ALL-IN neste turno!"
            applyMarkusStrongEffect(owner, enemy, state, ability, coins)
        } else {
            applyMarkusBasicEffect(owner, enemy, state, ability, coins)
        }

        // Quanto mais SPEED, maior a chance de rolar um efeito basico adicional.
        val extraRollChance = resolveExtraRollChance(owner, ability)
        if (state.rng.nextDouble() < extraRollChance) {
            state.combatLog += "⚡ ${unitLabel(owner, state)} ganhou uma rolagem extra pela velocidade!"
            applyMarkusBasicEffect(owner, enemy, state, ability, coins)
        }
    }

    private fun applyMarkusBasicEffect(
        owner: Unit,
        enemy: Unit?,
        state: CombatState,
        ability: Ability,
        coins: Int
    ) {
        val hasDefBuffParams = ability.params?.defBuffMin != null || ability.params?.defBuffMax != null

        val outcome = pickWeighted(
            state = state,
            options = listOf(
                BasicOutcome.ATK_UP to 24.0,
                BasicOutcome.DEF_UP to if (hasDefBuffParams) 18.0 else 0.0,
                BasicOutcome.SPEED_UP to 23.0,
                BasicOutcome.DAMAGE_ENEMY to 22.0,
                BasicOutcome.HEAL_SELF to 16.0,
                BasicOutcome.SELF_DAMAGE to (15.0 - coins * (ability.params?.coinBiasPerCoin ?: 0.9)).coerceAtLeast(2.0),
                BasicOutcome.SHIELD_SELF to 14.0,
                BasicOutcome.NOTHING to 7.0
            )
        )

        when (outcome) {
            BasicOutcome.ATK_UP -> {
                val amount = randomRange(state, ability, "atkBuffMin", "atkBuffMax", 4.0, 9.0)
                buffStat(owner, Stat.ATK, amount)
                state.combatLog += "🃏 ${unitLabel(owner, state)} puxou carta de ataque: +${formatValue(amount)} ATK."
            }

            BasicOutcome.SPEED_UP -> {
                val amount = randomRange(state, ability, "speedBuffMin", "speedBuffMax", 5.0, 12.0)
                buffStat(owner, Stat.SPEED, amount)
                state.combatLog += "🎲 ${unitLabel(owner, state)} ganhou impulso: +${formatValue(amount)} SPEED."
            }

            BasicOutcome.DAMAGE_ENEMY -> {
                if (enemy == null) return
                val amount = randomRange(state, ability, "enemyDamageMin", "enemyDamageMax", 22.0, 42.0)
                queueRngDamage(owner, enemy, amount, state)
                state.combatLog += "💥 ${unitLabel(owner, state)} acertou a Carta da Sorte e causou ${formatValue(amount)} de dano."
            }

            BasicOutcome.HEAL_SELF -> {
                val amount = randomRange(state, ability, "selfHealMin", "selfHealMax", 16.0, 34.0)
                val healed = healAndMeasure(owner, amount)
                if (healed > 0.0) {
                    state.combatLog += "💉 ${unitLabel(owner, state)} recuperou ${formatValue(healed)} de HP."
                }
            }

            BasicOutcome.SELF_DAMAGE -> {
                val amount = randomRange(state, ability, "selfDamageMin", "selfDamageMax", 14.0, 30.0)
                owner.hp -= amount
                state.combatLog += "💀 ${unitLabel(owner, state)} perdeu ${formatValue(amount)} de HP em uma aposta ruim."
            }

            BasicOutcome.SHIELD_SELF -> {
                val stacks = randomIntRange(state, ability, "shieldMin", "shieldMax", 1, 1)
                grantShieldStacks(owner, stacks, state)
                state.combatLog += "🛡️ ${unitLabel(owner, state)} ganhou $stacks camada(s) de escudo."
            }

            BasicOutcome.DEF_UP -> {
                val amount = randomRange(state, ability, "defBuffMin", "defBuffMax", 4.0, 9.0)
                buffStat(owner, Stat.DEF, amount)
                state.combatLog += "🧱 ${unitLabel(owner, state)} reforcou a guarda: +${formatValue(amount)} DEF."
            }

            BasicOutcome.NOTHING -> {
                state.combatLog += "🃏 ${unitLabel(owner, state)} puxou uma carta vazia. Nada aconteceu."
            }
        }
    }

    private fun applyMarkusStrongEffect(
        owner: Unit,
        enemy: Unit?,
        state: CombatState,
        ability: Ability,
        coins: Int
    ) {
        val hpMultiplier = 1.0 + (1.0 - hpRatio(owner)).coerceIn(0.0, 1.0) * 0.6
        val strongMultiplier = (ability.params?.strongMultiplier ?: 2.0) * hpMultiplier

        val outcome = pickWeighted(
            state = state,
            options = listOf(
                StrongOutcome.MASSIVE_DAMAGE to 34.0,
                StrongOutcome.BIG_SHIELD to 24.0,
                StrongOutcome.BIG_SPEED to 24.0,
                StrongOutcome.BIG_SELF_DAMAGE to (18.0 - coins * (ability.params?.coinBiasPerCoin ?: 0.9)).coerceAtLeast(3.0)
            )
        )

        when (outcome) {
            StrongOutcome.MASSIVE_DAMAGE -> {
                if (enemy == null) return
                val base = randomRange(state, ability, "enemyDamageMin", "enemyDamageMax", 22.0, 42.0)
                val amount = base * strongMultiplier
                queueRngDamage(owner, enemy, amount, state)
                state.combatLog += "🎯 ${unitLabel(owner, state)} venceu a Aposta Alta e causou ${formatValue(amount)} de dano!"
            }

            StrongOutcome.BIG_SHIELD -> {
                val base = randomIntRange(state, ability, "shieldMin", "shieldMax", 1, 1)
                val stacks = (base * strongMultiplier).toInt().coerceAtLeast(2)
                grantShieldStacks(owner, stacks, state)
                state.combatLog += "🛡️ ${unitLabel(owner, state)} levantou um escudo enorme ($stacks camadas)."
            }

            StrongOutcome.BIG_SPEED -> {
                val base = randomRange(state, ability, "speedBuffMin", "speedBuffMax", 5.0, 12.0)
                val amount = base * strongMultiplier
                buffStat(owner, Stat.SPEED, amount)
                state.combatLog += "⚡ ${unitLabel(owner, state)} recebeu +${formatValue(amount)} SPEED no all-in."
            }

            StrongOutcome.BIG_SELF_DAMAGE -> {
                val base = randomRange(state, ability, "selfDamageMin", "selfDamageMax", 14.0, 30.0)
                val amount = base * (1.4 + (strongMultiplier - 1.0) * 0.35)
                owner.hp -= amount
                state.combatLog += "💀 ${unitLabel(owner, state)} perdeu ${formatValue(amount)} de HP no all-in."
            }
        }
    }

    private enum class BasicOutcome {
        ATK_UP,
        SPEED_UP,
        DAMAGE_ENEMY,
        HEAL_SELF,
        SELF_DAMAGE,
        SHIELD_SELF,
        DEF_UP,
        NOTHING
    }

    private enum class StrongOutcome {
        MASSIVE_DAMAGE,
        BIG_SHIELD,
        BIG_SPEED,
        BIG_SELF_DAMAGE
    }

    private fun applyUndefinedEffect(
        owner: Unit,
        state: CombatState,
        ability: Ability
    ) {
        val enemy = findPrimaryEnemy(owner, state)

        val selfDamage = randomRange(state, ability, "selfDamageMin", "selfDamageMax", 60.0, 140.0)
        val enemyDamage = randomRange(state, ability, "enemyDamageMin", "enemyDamageMax", 70.0, 160.0)
        val selfHeal = randomRange(state, ability, "selfHealMin", "selfHealMax", 50.0, 120.0)
        val enemyHeal = randomRange(state, ability, "enemyHealMin", "enemyHealMax", 50.0, 120.0)

        val effects = mutableListOf<() -> kotlin.Unit>()
        effects += { owner.hp -= selfDamage }
        effects += { healUnit(owner, selfHeal) }
        effects += {
            state.pendingIncomingDamageNegationByUnitId[owner.id] =
                (state.pendingIncomingDamageNegationByUnitId[owner.id] ?: 0) + 1
        }
        effects += {
            state.pendingOutgoingDamageNegationByUnitId[owner.id] =
                (state.pendingOutgoingDamageNegationByUnitId[owner.id] ?: 0) + 1
        }

        if (enemy != null) {
            effects += {
                state.queue.add(
                    CombatEvent.BeforeDamage(
                        source = owner,
                        target = enemy,
                        damage = enemyDamage,
                        sourceAbilityType = AbilityType.RNG_EFFECT
                    )
                )
            }
            effects += { healUnit(enemy, enemyHeal) }
            effects += {
                state.pendingIncomingDamageNegationByUnitId[enemy.id] =
                    (state.pendingIncomingDamageNegationByUnitId[enemy.id] ?: 0) + 1
            }
            effects += {
                state.pendingOutgoingDamageNegationByUnitId[enemy.id] =
                    (state.pendingOutgoingDamageNegationByUnitId[enemy.id] ?: 0) + 1
            }
        }

        if (effects.isEmpty()) return

        val chosenEffect = effects[state.rng.nextInt(effects.size)]
        chosenEffect.invoke()
    }

    private fun healUnit(unit: Unit, amount: Double) {
        if (amount <= 0.0) return

        val maxHp = unit.stats[me.igorunderplayer.kono.domain.card.Stat.HP]
        unit.hp = if (maxHp != null && maxHp > 0.0) {
            (unit.hp + amount).coerceAtMost(maxHp)
        } else {
            unit.hp + amount
        }
    }

    private fun findPrimaryEnemy(owner: Unit, state: CombatState): Unit? {
        val ownerTeam = state.teams.firstOrNull { team -> team.units.any { it === owner } }
            ?: return null

        return state.teams
            .asSequence()
            .filter { it !== ownerTeam }
            .flatMap { it.units.asSequence() }
            .firstOrNull { it.hp > 0 }
    }

    private fun randomRange(
        state: CombatState,
        ability: Ability,
        minKey: String,
        maxKey: String,
        fallbackMin: Double,
        fallbackMax: Double
    ): Double {
        val minValue = resolveDoubleParam(ability, minKey) ?: fallbackMin
        val maxValue = resolveDoubleParam(ability, maxKey) ?: fallbackMax
        val low = minOf(minValue, maxValue)
        val high = maxOf(minValue, maxValue)

        if (low == high) return low

        return state.rng.nextDouble(low, high)
    }

    private fun shouldActivateByHitCycle(
        ability: Ability,
        owner: Unit,
        abilityIndex: Int,
        state: CombatState
    ): Boolean {
        val everyHits = ability.params?.everyHits?.coerceAtLeast(1) ?: 1

        if (everyHits == 1) return true

        val key = "${owner.id}#$abilityIndex"
        val nextCounter = (state.hitCounterByAbilityKey[key] ?: 0) + 1
        state.hitCounterByAbilityKey[key] = nextCounter

        return nextCounter % everyHits == 0
    }

    private fun resolveDoubleParam(ability: Ability, key: String): Double? {
        return when (key) {
            "selfDamageMin" -> ability.params?.selfDamageMin
            "selfDamageMax" -> ability.params?.selfDamageMax
            "enemyDamageMin" -> ability.params?.enemyDamageMin
            "enemyDamageMax" -> ability.params?.enemyDamageMax
            "selfHealMin" -> ability.params?.selfHealMin
            "selfHealMax" -> ability.params?.selfHealMax
            "enemyHealMin" -> ability.params?.enemyHealMin
            "enemyHealMax" -> ability.params?.enemyHealMax
            "atkBuffMin" -> ability.params?.atkBuffMin
            "atkBuffMax" -> ability.params?.atkBuffMax
            "defBuffMin" -> ability.params?.defBuffMin
            "defBuffMax" -> ability.params?.defBuffMax
            "speedBuffMin" -> ability.params?.speedBuffMin
            "speedBuffMax" -> ability.params?.speedBuffMax
            else -> null
        }
    }

    private fun resolveIntParam(ability: Ability, key: String): Int? {
        return when (key) {
            "shieldMin" -> ability.params?.shieldMin
            "shieldMax" -> ability.params?.shieldMax
            else -> null
        }
    }

    private fun randomIntRange(
        state: CombatState,
        ability: Ability,
        minKey: String,
        maxKey: String,
        fallbackMin: Int,
        fallbackMax: Int
    ): Int {
        val minValue = resolveIntParam(ability, minKey) ?: fallbackMin
        val maxValue = resolveIntParam(ability, maxKey) ?: fallbackMax
        val low = minOf(minValue, maxValue)
        val high = maxOf(minValue, maxValue)

        if (low == high) return low

        return state.rng.nextInt(low, high + 1)
    }

    private fun buffStat(unit: Unit, stat: Stat, amount: Double) {
        if (amount <= 0.0) return
        unit.stats[stat] = (unit.stats[stat] ?: 0.0) + amount
    }

    private fun healAndMeasure(unit: Unit, amount: Double): Double {
        val before = unit.hp
        healUnit(unit, amount)
        return (unit.hp - before).coerceAtLeast(0.0)
    }

    private fun grantShieldStacks(unit: Unit, stacks: Int, state: CombatState) {
        if (stacks <= 0) return
        state.pendingIncomingDamageNegationByUnitId[unit.id] =
            (state.pendingIncomingDamageNegationByUnitId[unit.id] ?: 0) + stacks
    }

    private fun queueRngDamage(owner: Unit, target: Unit, damage: Double, state: CombatState) {
        state.queue.add(
            CombatEvent.BeforeDamage(
                source = owner,
                target = target,
                damage = damage,
                isTrueDamage = false,
                canCrit = false,
                canBeDodged = true,
                sourceAbilityType = AbilityType.RNG_EFFECT
            )
        )
    }

    private fun resolveExtraRollChance(owner: Unit, ability: Ability): Double {
        val speed = owner.stats[Stat.SPEED] ?: 0.0
        val factor = ability.params?.extraRollSpeedFactor ?: 0.003
        return ((speed - 100.0) * factor).coerceIn(0.0, 0.55)
    }

    private fun hpRatio(unit: Unit): Double {
        val maxHp = unit.stats[Stat.HP] ?: return 1.0
        if (maxHp <= 0.0) return 1.0
        return (unit.hp / maxHp).coerceIn(0.0, 1.0)
    }

    private fun <T> pickWeighted(state: CombatState, options: List<Pair<T, Double>>): T {
        val totalWeight = options.sumOf { (_, weight) -> weight.coerceAtLeast(0.0) }
        if (totalWeight <= 0.0) return options.first().first

        var roll = state.rng.nextDouble() * totalWeight
        for ((value, weight) in options) {
            val safeWeight = weight.coerceAtLeast(0.0)
            roll -= safeWeight
            if (roll <= 0.0) return value
        }

        return options.last().first
    }

    private fun unitLabel(unit: Unit, state: CombatState): String {
        return state.unitDisplayNamesById[unit.id] ?: unit.card.name
    }

    private fun formatValue(value: Double): String {
        val rounded = String.format(java.util.Locale.US, "%.2f", value)
        return rounded.trimEnd('0').trimEnd('.')
    }
}
