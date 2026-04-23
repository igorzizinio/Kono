package me.igorunderplayer.kono.domain.battle

object EnemyTeamCatalog {

    data class EnemyTeamMember(
        val cardId: String,
        val level: Int = 1
    )

    data class EnemyTeamDefinition(
        val id: String,
        val name: String,
        val description: String,
        val essenceReward: Int,
        val members: List<EnemyTeamMember>
    )

    private val teams = listOf(
        EnemyTeamDefinition(
            id = "SLIME_SWARM",
            name = "Enxame de Slimes",
            description = "Três criaturas viscosas que abusam de volume.",
            essenceReward = 60,
            members = listOf(
                EnemyTeamMember("SLIME", 3),
                EnemyTeamMember("SLIME", 3),
                EnemyTeamMember("SLIME", 3)
            )
        ),
        EnemyTeamDefinition(
            id = "MARKUS_HENCHMEN",
            name = "Capangas de Markus",
            description = "Os capangas de Markus, o grande Apostador",
            essenceReward = 160,
            members = listOf(
                EnemyTeamMember("VEYN", 4),
                EnemyTeamMember("JORGE", 3),
                EnemyTeamMember("AURUM", 3)
            )
        ),
        EnemyTeamDefinition(
            id = "BLADE_TRIO",
            name = "Trio das Lâminas",
            description = "Time equilibrado com pressão constante e foco em alvo único.",
            essenceReward = 90,
            members = listOf(
                EnemyTeamMember("JUNIOR_KNIGHT", 1),
                EnemyTeamMember("THIEF", 2),
                EnemyTeamMember("THIEF", 1)
            )
        ),
        EnemyTeamDefinition(
            id = "MARKUS_GANG",
            name = "Esquadrão das Apostas",
            description = "Time agressivo de Markus, com risco e burst.",
            essenceReward = 260,
            members = listOf(
                EnemyTeamMember("MARKUS", 5),
                EnemyTeamMember("JORGE", 8),
                EnemyTeamMember("VEYN", 8)
            )
        ),
        EnemyTeamDefinition(
            id = "KONO_SISTERS",
            name = "Irmãs Kono",
            description = "As 3 irmãs unidas, divindade pura",
            essenceReward = 2000,
            members = listOf(
                EnemyTeamMember("KONO", 20),
                EnemyTeamMember("KONO", 15),
                EnemyTeamMember("KONO", 15)
            )
        ),
        EnemyTeamDefinition(
            id = "DUMMIES",
            name = "Bonecos de treino",
            description = "Apenas 3 bonecos de treino",
            essenceReward = 0,
            members = listOf(
                EnemyTeamMember("DUMMY", 1),
                EnemyTeamMember("DUMMY", 1),
                EnemyTeamMember("DUMMY", 1)
            )
        )
    )

    private val byId = teams.associateBy { it.id }

    fun all(): List<EnemyTeamDefinition> = teams

    fun getById(id: String): EnemyTeamDefinition? = byId[id.uppercase()]
}


