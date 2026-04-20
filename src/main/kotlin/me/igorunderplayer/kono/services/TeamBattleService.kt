package me.igorunderplayer.kono.services

import me.igorunderplayer.kono.data.repositories.BattleTeamRepository
import me.igorunderplayer.kono.data.repositories.BattleVictoryRepository
import me.igorunderplayer.kono.data.repositories.CardInstanceRepository
import me.igorunderplayer.kono.data.repositories.EquippedCardsRepository
import me.igorunderplayer.kono.data.repositories.UserRepository
import me.igorunderplayer.kono.domain.battle.EnemyTeamCatalog
import me.igorunderplayer.kono.domain.card.CardCatalog
import me.igorunderplayer.kono.domain.card.CardType
import me.igorunderplayer.kono.domain.gameplay.Unit
import me.igorunderplayer.kono.domain.team.CombatUnitFactory

class TeamBattleService(
	private val userRepository: UserRepository,
	private val cardInstanceRepository: CardInstanceRepository,
	private val equippedCardsRepository: EquippedCardsRepository,
	private val battleTeamRepository: BattleTeamRepository,
	private val battleVictoryRepository: BattleVictoryRepository
) {

	sealed class RosterResult {
		data class Success(
			val ownerUserId: Int?,
			val displayName: String,
			val units: List<Unit>,
			val battleKey: String? = null,
			val essenceReward: Int? = null
		) : RosterResult()

		data class Failure(val message: String) : RosterResult()
	}

	suspend fun buildPlayerRoster(discordId: Long, displayName: String): RosterResult {
		val user = userRepository.getUserByDiscordId(discordId)
			?: return RosterResult.Failure("❌ Você ainda não possui registro.")

		val slots = battleTeamRepository.getTeamByUserId(user.id)
			.sortedBy { it.slot }

		if (slots.size < 3) {
			return RosterResult.Failure("❌ Você precisa montar um time de 3 personagens. Use `team set <slot> <instance_id>` para preencher os 3 slots.")
		}

		val units = mutableListOf<Unit>()

		for (slot in 1..3) {
			val row = slots.firstOrNull { it.slot == slot }
				?: return RosterResult.Failure("❌ Seu time está incompleto. Falta o slot $slot.")

			val character = cardInstanceRepository.getOwnedCharacterWithDefinition(user.id, row.characterInstanceId)
				?: return RosterResult.Failure("❌ O personagem do slot $slot não existe mais ou não pertence a você.")

			val equips = equippedCardsRepository.getEquippedDefinitionsForCharacter(character.first.id)
			units += CombatUnitFactory.createUnit(
				unitId = character.first.id.toString(),
				definition = character.second,
				level = character.first.level,
				equips = equips
			)
		}

		return RosterResult.Success(
			ownerUserId = user.id,
			displayName = displayName,
			units = units
		)
	}

	fun buildBotRoster(botId: String): RosterResult {
		val enemy = EnemyTeamCatalog.getById(botId)
			?: return RosterResult.Failure("❌ Inimigo bot inválido.")

		val units = enemy.members.mapIndexed { index, member ->
			val definition = CardCatalog.getById(member.cardId)
				?: return RosterResult.Failure("❌ Carta '${member.cardId}' não encontrada no catálogo.")

			if (definition.type != CardType.CHARACTER) {
				return RosterResult.Failure("❌ ${definition.name} não é um personagem de batalha.")
			}

			CombatUnitFactory.createUnit(
				unitId = "bot_${enemy.id}_$index",
				definition = definition,
				level = member.level,
				equips = emptyList()
			)
		}

		return RosterResult.Success(
			ownerUserId = null,
			displayName = enemy.name,
			units = units,
			battleKey = enemy.id,
			essenceReward = enemy.essenceReward
		)
	}

	suspend fun grantFirstVictoryReward(userId: Int, battleKey: String, reward: Int): Boolean {
		if (reward <= 0) return false
		val inserted = battleVictoryRepository.registerVictory(userId, battleKey, reward)
		if (!inserted) return false

		val user = userRepository.getUserById(userId) ?: return false
		userRepository.updateEssence(user.id, user.essence + reward)
		return true
	}
}

