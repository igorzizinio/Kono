package me.igorunderplayer.kono.commands.text.testing

import dev.kord.common.Color
import dev.kord.common.entity.ButtonStyle
import dev.kord.core.behavior.edit
import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.behavior.interaction.response.createEphemeralFollowup
import dev.kord.core.behavior.reply
import dev.kord.core.entity.Message
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.rest.builder.component.ActionRowBuilder
import dev.kord.rest.builder.message.embed
import kotlinx.coroutines.time.delay
import me.igorunderplayer.kono.commands.BaseCommand
import me.igorunderplayer.kono.commands.CommandCategory
import me.igorunderplayer.kono.data.repositories.UserRepository
import me.igorunderplayer.kono.domain.battle.EnemyTeamCatalog
import me.igorunderplayer.kono.domain.card.CardCatalog
import me.igorunderplayer.kono.domain.card.Stat
import me.igorunderplayer.kono.domain.card.ability.Ability
import me.igorunderplayer.kono.domain.gameplay.CombatState
import me.igorunderplayer.kono.domain.gameplay.Team
import me.igorunderplayer.kono.engine.combat.CombatAction
import me.igorunderplayer.kono.engine.combat.CombatEngine
import me.igorunderplayer.kono.engine.combat.PlayerTurnController
import me.igorunderplayer.kono.engine.combat.RandomAiTurnController
import me.igorunderplayer.kono.engine.combat.TurnController
import me.igorunderplayer.kono.services.TeamBattleService
import me.igorunderplayer.kono.utils.interaction.awaitButtonInteraction
import me.igorunderplayer.kono.utils.interaction.awaitFirstButtonInteraction
import java.time.Duration
import kotlin.random.Random
import me.igorunderplayer.kono.domain.gameplay.Unit as CombatUnit

class StoryCommand(
    private val teamBattleService: TeamBattleService,
    private val userRepository: UserRepository
) : BaseCommand(
    name = "story",
    description = "modo história — siga a jornada do início ao fim",
    category = CommandCategory.Game
) {

    companion object {
        private const val EMBED_DESCRIPTION_LIMIT = 3500
        private val TOTAL_CHAPTERS = EnemyTeamCatalog.storyTeamsInOrder().size

        private val COLOR_PROLOGUE = Color(0x4B0082)
        private val COLOR_CHAPTER = Color(0x8B6914)
        private val COLOR_WIN = Color(0x2ECC71)
        private val COLOR_LOSS = Color(0xE74C3C)
        private val COLOR_DONE = Color(0xF5C518)
    }

    override suspend fun run(event: MessageCreateEvent, args: Array<String>) {
        val author = event.message.author ?: return
        val discordId = author.id.value.toLong()

        val user = userRepository.getUserByDiscordId(discordId)
            ?: run {
                event.message.reply { content = "❌ Você ainda não possui registro. Use `@Kono register` primeiro." }
                return
            }

        if (user.storyProgress >= TOTAL_CHAPTERS) {
            event.message.reply {
                embed {
                    title = "📖 Jornada Concluída"
                    description = buildString {
                        appendLine("✨ Você completou toda a história.")
                        appendLine()
                        appendLine("- A cidade das apostas ainda ecoa seu nome. Markus foi derrotado, e a jornada que começou em uma floresta silenciosa terminou nas mesas douradas do cassino.")
                        appendLine()
                        appendLine("- Novas historias e batalhas incriveis estão por vir!")
                        appendLine("- Continue suas batalhas com `@Kono teamfight` para explorar novos desafios.")
                    }
                    color = COLOR_DONE
                }
            }
            return
        }

        val baseId = "${event.message.channelId}-${event.message.id}"
        val startButtonId = "story-start-$baseId"
        val fightButtonId = "story-fight-$baseId"
        val logButtonId = "story-log-$baseId"

        // ── Prologue ─────────────────────────────────────────────────────────
        var currentProgress = user.storyProgress
        var pendingMessage: Message? = null

        if (currentProgress == -1) {
            pendingMessage = event.message.reply {
                embed {
                    title = "📖 Prólogo — O Primeiro Passo"
                    description = buildString {
                        appendLine("Durante séculos, o mundo de **Kono** foi moldado por cartas.")
                        appendLine("Não cartas comuns — mas fragmentos de almas selados em tinta e papel.")
                        appendLine()
                        appendLine("Heróis lendários. Criaturas ancestrais. Armas amaldiçoadas.")
                        appendLine("Tudo pode se tornar uma carta.")
                        appendLine()
                        appendLine("Coletores atravessam continentes em busca de poder, fama e fortuna.")
                        appendLine("Alguns se tornam lendas.")
                        appendLine("Outros desaparecem sem deixar rastros.")
                        appendLine()
                        appendLine("Você nasceu em uma pequena aldeia afastada do caos do mundo.")
                        appendLine("Enquanto outros sonhavam em se tornar guerreiros ou magos, você observava viajantes chegando com cartas raras presas ao cinto e histórias impossíveis nos olhos.")
                        appendLine()
                        appendLine("Hoje, sua vida muda.")
                        appendLine()
                        appendLine("Ao amanhecer, o velho portão da aldeia se abre pela primeira vez para você.")
                        appendLine("Além dele existe apenas uma estrada.")
                        appendLine()
                        appendLine("Primeiro, uma floresta tomada por criaturas selvagens.")
                        appendLine("Depois, uma cidade iluminada por ouro, apostas e mentiras.")
                        appendLine()
                        appendLine("Ela pertence a um homem conhecido apenas como **Markus, o Grande Apostador**.")
                        appendLine("Dizem que ninguém entra em sua cidade sem apostar algo.")
                        appendLine("E poucos conseguem sair com mais do que tinham.")
                        appendLine()
                        appendLine("Mas antes de enfrentar Markus...")
                        appendLine("Você precisará sobreviver à floresta.")
                        appendLine()
                        appendLine("**Sua jornada começa agora.**")
                        appendLine()
                        appendLine("**Dicas rápidas:**")
                        appendLine("• `@Kono pull` — obter novas cartas")
                        appendLine("• `@Kono team set <slot> <id>` — montar seu time")
                        appendLine("• `@Kono card <nome>` — ver detalhes de uma carta")
                        appendLine("• `@Kono teamfight <bot_id>` — batalhas fora da história")
                        appendLine("_Você também pode usar os comandos de `/` caso queira algo visual!_")
                    }

                    footer { text = "Prólogo · $TOTAL_CHAPTERS capítulos pela frente" }
                    color = COLOR_PROLOGUE
                }
                addComponent(ActionRowBuilder().apply {
                    interactionButton(ButtonStyle.Success, startButtonId) {
                        label = "🗡️ Começar a Jornada"
                    }
                })
            }

            val startClick = event.kord.awaitButtonInteraction(
                customId = startButtonId,
                allowedUserId = discordId
            ) ?: run {
                pendingMessage.edit {
                    components = mutableListOf(ActionRowBuilder().apply {
                        interactionButton(ButtonStyle.Success, startButtonId) {
                            label = "🗡️ Começar a Jornada"
                            disabled = true
                        }
                    })
                }
                return
            }

            startClick.interaction.respondEphemeral { content = "⚔️ A jornada começa!" }
            userRepository.updateStoryProgress(user.id, 0)
            currentProgress = 0
        }

        // ── Build player roster (fail-fast before showing chapter embed) ─────
        val playerRoster = when (val result = teamBattleService.buildPlayerRoster(discordId, author.username)) {
            is TeamBattleService.RosterResult.Success -> result
            is TeamBattleService.RosterResult.Failure -> {
                event.message.reply { content = result.message }
                return
            }
        }

        // ── Chapter embed ─────────────────────────────────────────────────────
        val chapter = EnemyTeamCatalog.getStoryChapter(currentProgress)!!
        val chapterNumber = currentProgress + 1

        val chapterEmbedContent: dev.kord.rest.builder.message.EmbedBuilder.() -> Unit = {
            title = "📖 Capítulo $chapterNumber — ${chapter.storyChapterTitle}"
            description = buildString {
                appendLine(chapter.preText)
                appendLine()
                appendLine("**Inimigos:**")
                chapter.members.forEach { member ->
                    val def = CardCatalog.getById(member.cardId)
                    appendLine("• ${def?.name ?: member.cardId} (nível ${member.level})")
                }
                appendLine()
                appendLine("_Recompensa de primeira vitória: **+${chapter.essenceReward} essence**_")
            }
            footer { text = "Capítulo $chapterNumber de $TOTAL_CHAPTERS" }
            color = COLOR_CHAPTER
        }

        var chapterMessage = if (pendingMessage != null) {
            pendingMessage.edit {
                embed(chapterEmbedContent)
                addComponent(ActionRowBuilder().apply {
                    interactionButton(ButtonStyle.Primary, fightButtonId) {
                        label = "⚔️ Partir para batalha"
                    }
                })
            }
            pendingMessage
        } else {
            event.message.reply {
                embed(chapterEmbedContent)
                addComponent(ActionRowBuilder().apply {
                    interactionButton(ButtonStyle.Primary, fightButtonId) {
                        label = "⚔️ Partir para batalha"
                    }
                })
            }
        }

        // ── Await fight button ────────────────────────────────────────────────
        val fightClick = event.kord.awaitButtonInteraction(
            customId = fightButtonId,
            allowedUserId = discordId
        ) ?: run {
            chapterMessage.edit {
                components = mutableListOf(ActionRowBuilder().apply {
                    interactionButton(ButtonStyle.Primary, fightButtonId) {
                        label = "⚔️ Partir para batalha"
                        disabled = true
                    }
                })
            }
            return
        }

        fightClick.interaction.respondEphemeral { content = "⚔️ Iniciando batalha..." }

        // ── Build combat state ───────────────────────────────────────────────
        val enemyRoster = when (val result = teamBattleService.buildBotRoster(chapter.id)) {
            is TeamBattleService.RosterResult.Success -> result
            is TeamBattleService.RosterResult.Failure -> {
                event.message.reply { content = result.message }
                return
            }
        }

        val state = CombatState(
            teams = listOf(
                Team("player", playerRoster.units.toMutableList()),
                Team("enemy", enemyRoster.units.toMutableList())
            ),
            rng = Random.Default
        )
        playerRoster.units.forEach { state.unitDisplayNamesById[it.id] = it.card.name }
        enemyRoster.units.forEach { state.unitDisplayNamesById[it.id] = it.card.name }

        // ── Run combat round by round: player controla suas unidades via
        // prompt, os bots do capítulo usam RandomAiTurnController (sem prompt).
        val combatId = "story-combat-$baseId"
        val continueButtonId = "$combatId-continue"
        val fullCombatLog = mutableListOf<String>()

        lateinit var engine: CombatEngine

        fun displayNameOf(u: CombatUnit) = state.unitDisplayNamesById[u.id] ?: u.card.name
        fun teamIdOf(u: CombatUnit) = state.teams.first { it.units.contains(u) }.id

        suspend fun promptTargetSelection(unit: CombatUnit, ability: Ability, candidates: List<CombatUnit>): CombatUnit? {
            val targetBaseId = "$combatId-target-${System.currentTimeMillis()}"
            val targetButtonIds = candidates.indices.map { "$targetBaseId:$it" }

            chapterMessage = chapterMessage.edit {
                embed {
                    title = "🎯 ${author.username}, escolha o alvo de [${ability.name}]"
                    description = candidates.joinToString("\n") {
                        "• **${displayNameOf(it)}** (${it.hp.toInt()} HP) — time ${teamIdOf(it)}"
                    }
                    color = COLOR_CHAPTER
                }
                components = mutableListOf(ActionRowBuilder().apply {
                    candidates.forEachIndexed { index, candidate ->
                        interactionButton(ButtonStyle.Secondary, targetButtonIds[index]) {
                            label = displayNameOf(candidate).take(80)
                        }
                    }
                })
            }

            val result = event.kord.awaitFirstButtonInteraction(
                ids = targetButtonIds,
                allowedUserId = discordId
            ) ?: run {
                state.combatLog += "⏱️ ${unit.card.name} não escolheu alvo a tempo, alvo automático será usado."
                return null
            }

            val (chosenId, buttonInteraction) = result
            val chosenTarget = candidates.getOrNull(targetButtonIds.indexOf(chosenId))

            buttonInteraction.interaction.respondEphemeral {
                content = if (chosenTarget != null) {
                    "🎯 Alvo: ${displayNameOf(chosenTarget)}"
                } else {
                    "⚠️ Alvo inválido, usando seleção automática."
                }
            }

            return chosenTarget
        }

        suspend fun promptPlayerAction(unit: CombatUnit, availableAbilities: List<Ability>): CombatAction {
            val actionBaseId = "$combatId-action-${System.currentTimeMillis()}"
            val attackButtonId = "$actionBaseId:attack"
            val abilityButtonIds = availableAbilities.indices.map { "$actionBaseId:ability:$it" }

            val maxHp = unit.stats[Stat.HP]?.toInt()
            val hpText = if (maxHp != null) "${unit.hp.toInt()}/$maxHp HP" else "${unit.hp.toInt()} HP"

            chapterMessage = chapterMessage.edit {
                embed {
                    title = "🎮 Sua vez, ${author.username}"
                    description = buildString {
                        appendLine("Controlando **${unit.card.name}** — $hpText")
                        appendLine()
                        if (availableAbilities.isEmpty()) {
                            appendLine("Nenhuma habilidade ativa disponível — ataque ou aguarde novas cartas.")
                        } else {
                            appendLine("Habilidades disponíveis:")
                            availableAbilities.forEach { appendLine("• **${it.name}**") }
                        }
                    }
                    color = COLOR_CHAPTER
                }
                components = mutableListOf(createActionRow(attackButtonId, availableAbilities, abilityButtonIds))
            }

            val allIds = listOf(attackButtonId) + abilityButtonIds
            val result = event.kord.awaitFirstButtonInteraction(ids = allIds, allowedUserId = discordId)

            if (result == null) {
                state.combatLog += "⏱️ ${unit.card.name} não respondeu a tempo e atacou automaticamente."
                return CombatAction.BasicAttack()
            }

            val (chosenId, buttonInteraction) = result

            if (chosenId == attackButtonId) {
                buttonInteraction.interaction.respondEphemeral { content = "⚔️ Ataque básico escolhido." }
                return CombatAction.BasicAttack()
            }

            val ability = availableAbilities.getOrNull(abilityButtonIds.indexOf(chosenId))
            if (ability == null) {
                buttonInteraction.interaction.respondEphemeral { content = "⚠️ Ação inválida, atacando." }
                return CombatAction.BasicAttack()
            }

            buttonInteraction.interaction.respondEphemeral { content = "🌟 [${ability.name}] escolhida." }

            val manualKind = engine.manualTargetKind(ability)
            val target = if (manualKind != null) {
                val candidates = engine.manualTargetCandidates(unit, manualKind)
                if (candidates.size > 1) promptTargetSelection(unit, ability, candidates) else null
            } else {
                null
            }

            return CombatAction.UseAbility(ability, target)
        }

        val botController = RandomAiTurnController()
        val controllersByUnitId: Map<String, TurnController> = buildMap {
            playerRoster.units.forEach { unit ->
                put(unit.id, PlayerTurnController { u, abilities -> promptPlayerAction(u, abilities) })
            }
            enemyRoster.units.forEach { unit -> put(unit.id, botController) }
        }

        engine = CombatEngine(state = state, controllersByUnitId = controllersByUnitId)

        chapterMessage = chapterMessage.edit {
            embed {
                title = "⚔️ Capítulo $chapterNumber — Batalha"
                description = buildString {
                    appendLine("**Seu time:** ${playerRoster.units.joinToString(", ") { it.card.name }}")
                    appendLine("**${chapter.name}:** ${enemyRoster.units.joinToString(", ") { it.card.name }}")
                    appendLine()
                    appendLine("Clique em **Continuar** para avançar o combate.")
                }
                color = COLOR_CHAPTER
            }
            components = mutableListOf(createContinueRow(continueButtonId))
        }

        while (!state.isFinished()) {
            val interaction = event.kord.awaitButtonInteraction(
                customId = continueButtonId,
                allowedUserId = discordId
            ) ?: run {
                chapterMessage.edit {
                    components = mutableListOf(createContinueRow(continueButtonId, disabled = true))
                }
                return
            }

            interaction.interaction.respondEphemeral { content = "⚔️ Prosseguindo com a batalha" }

            state.combatLog.clear()
            // Dentro dessa chamada, promptPlayerAction() edita chapterMessage e
            // suspende pra cada unidade do jogador — os bots decidem na hora.
            engine.processNextTurnControlled()
            fullCombatLog += state.combatLog

            val isFinished = state.isFinished()
            if (!isFinished) {
                chapterMessage = chapterMessage.edit {
                    embed {
                        title = "⚔️ Turno ${state.turn - 1}"
                        description = if (state.combatLog.isEmpty()) {
                            "Nenhum evento foi registrado neste turno."
                        } else {
                            state.combatLog.joinToString("\n")
                        }
                        color = COLOR_CHAPTER
                    }
                    components = mutableListOf(createContinueRow(continueButtonId))
                }
            }
        }

        val playerWon = state.teams.first { it.id == "player" }.units.any { it.hp > 0 } &&
                !state.teams.first { it.id == "enemy" }.units.any { it.hp > 0 }

        // ── Rewards & progress ────────────────────────────────────────────────
        val essenceText = if (playerWon) {
            val rewarded = teamBattleService.grantFirstVictoryReward(
                userId = user.id,
                battleKey = chapter.id,
                reward = chapter.essenceReward
            )
            if (rewarded) "\n\n💎 **+${chapter.essenceReward} essence** pela vitória!"
            else "\n\nℹ️ Você já recebeu a recompensa de essence por esse capítulo."
        } else ""

        if (playerWon) {
            userRepository.updateStoryProgress(user.id, currentProgress + 1)
        }

        val nextHint = if (playerWon) {
            val next = EnemyTeamCatalog.getStoryChapter(currentProgress + 1)
            if (next != null) "\n\n➡️ Próximo capítulo: **${next.storyChapterTitle}** — use `@Kono story` para continuar."
            else "\n\n✨ Você completou toda a história!"
        } else {
            "\n\nUse `@Kono story` para tentar novamente."
        }

        val outcomeText = buildString {
            appendLine(if (playerWon) chapter.postText else "Você foi derrotado. A jornada continua, mas não hoje.")
            appendLine()
            appendLine("**Resultado:**")
            playerRoster.units.forEachIndexed { i, unit ->
                appendLine("${i + 1}. ${unit.card.name} — ${unit.hp.coerceAtLeast(0.0).toInt()} HP")
            }
            appendLine()
            appendLine("**${chapter.name}:**")
            enemyRoster.units.forEachIndexed { i, unit ->
                appendLine("${i + 1}. ${unit.card.name} — ${unit.hp.coerceAtLeast(0.0).toInt()} HP")
            }
            append(essenceText)
            append(nextHint)
        }

        chapterMessage.edit {
            embed {
                title =
                    "📖 Capítulo $chapterNumber — ${chapter.storyChapterTitle} | ${if (playerWon) "🏆 Vitória!" else "💀 Derrota..."}"
                description = outcomeText.take(EMBED_DESCRIPTION_LIMIT)
                footer { text = "Capítulo $chapterNumber de $TOTAL_CHAPTERS" }
                color = if (playerWon) COLOR_WIN else COLOR_LOSS
            }
            addComponent(ActionRowBuilder().apply {
                interactionButton(ButtonStyle.Secondary, logButtonId) {
                    label = "Ver diário de batalha"
                }
            })
        }

        // ── Combat log ────────────────────────────────────────────────────────
        val logClick = event.kord.awaitButtonInteraction(
            customId = logButtonId,
            allowedUserId = discordId
        ) ?: run {
            chapterMessage.edit {
                components = mutableListOf(ActionRowBuilder().apply {
                    interactionButton(ButtonStyle.Secondary, logButtonId) {
                        label = "Ver diário de batalha"
                        disabled = true
                    }
                })
            }
            return
        }

        val logPages = buildLogPages(fullCombatLog)

        val logResponse = logClick.interaction.respondEphemeral {
            content = "📜 Diário de batalha"
            embed {
                title = logPages.first().title
                description = logPages.first().description
                logPages.first().footer?.let { footer { text = it } }
            }
        }

        logPages.drop(1).forEach { page ->
            logResponse.createEphemeralFollowup {
                embed {
                    title = page.title
                    description = page.description
                    page.footer?.let { footer { text = it } }
                }
            }
            delay(Duration.ofMillis(250))
        }
    }

    private fun createContinueRow(customId: String, disabled: Boolean = false) = ActionRowBuilder().apply {
        interactionButton(ButtonStyle.Primary, customId) {
            label = "Continuar"
            this.disabled = disabled
        }
    }

    private fun createActionRow(
        attackButtonId: String,
        availableAbilities: List<Ability>,
        abilityButtonIds: List<String>
    ) = ActionRowBuilder().apply {
        interactionButton(ButtonStyle.Primary, attackButtonId) {
            label = "⚔️ Atacar"
        }

        // Discord permite no máximo 5 botões por linha (1 já usado pro ataque).
        // TODO: se algum personagem puder ter mais de 4 habilidades ativas
        // disponíveis ao mesmo tempo, quebrar em uma segunda ActionRow.
        availableAbilities.take(4).forEachIndexed { index, ability ->
            interactionButton(ButtonStyle.Secondary, abilityButtonIds[index]) {
                label = ability.name.take(80)
            }
        }
    }

    private fun buildLogPages(eventLog: List<String>): List<LogPage> {
        val lines = eventLog.ifEmpty { listOf("ℹ️ Nenhum evento foi registrado durante a luta.") }
        val pages = mutableListOf<String>()
        val current = StringBuilder()

        for (raw in lines) {
            val line = "• ${raw.take(EMBED_DESCRIPTION_LIMIT - 8)}"
            if (current.isNotEmpty() && current.length + line.length + 1 > EMBED_DESCRIPTION_LIMIT) {
                pages += current.toString()
                current.clear()
            }
            if (current.isNotEmpty()) current.append('\n')
            current.append(line)
        }
        if (current.isNotEmpty()) pages += current.toString()

        return pages.mapIndexed { i, text ->
            LogPage("📜 Diário de Batalha", text, "Página ${i + 1}/${pages.size}")
        }
    }

    private data class LogPage(val title: String, val description: String, val footer: String?)
}
