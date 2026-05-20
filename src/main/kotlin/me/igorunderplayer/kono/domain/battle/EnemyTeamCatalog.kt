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
        val members: List<EnemyTeamMember>,
        val storyOrder: Int? = null,
        val storyChapterTitle: String? = null,
        val preText: String? = null,
        val postText: String? = null
    )

    private val teams = listOf(
        EnemyTeamDefinition(
            id = "SLIME_SWARM",
            name = "Enxame de Slimes",
            description = "Três criaturas viscosas que abusam de volume.",
            essenceReward = 60,
            members = listOf(
                EnemyTeamMember("SLIME", 4),
                EnemyTeamMember("SLIME", 3),
                EnemyTeamMember("SLIME", 3)
            ),
            storyOrder = 1,
            storyChapterTitle = "A Floresta",
            preText = "Você deixa sua aldeia e parte para a cidade grande. A estrada corta uma floresta densa e quieta demais. De repente, o chão ao redor brilha e estremece. Três massas gelatinosas emergem das sombras — Slimes, famintos e agressivos.",
            postText = "Os slimes explodem em gosma pelo chão da floresta. Você limpa a bota com folhas e continua caminhando. A estrada principal ainda está distante, mas ao menos a floresta ficou para trás."
        ),
        EnemyTeamDefinition(
            id = "MARKUS_HENCHMEN",
            name = "Capangas de Markus",
            description = "Os capangas de Markus, o grande Apostador",
            essenceReward = 160,
            members = listOf(
                EnemyTeamMember("VEYN", 6),
                EnemyTeamMember("JORGE", 5),
                EnemyTeamMember("AURUM", 5)
            ),
            storyOrder = 4,
            storyChapterTitle = "O Cassino Dourado",
            preText = "A cidade pulsa. Luzes em toda esquina, dados rolando, fichas trocando de mão. No centro, o Cassino Dourado domina tudo — um prédio imenso com letras douradas acesas. Ao se aproximar da entrada, três figuras surgem bloqueando o caminho: Veyn, Jorge e Aurum. \"Negócio fechado para estranhos.\"",
            postText = "Os três recuam sem dizer palavra. A porta de madeira pesada do cassino se abre sozinha — lenta, como se te convidasse para entrar. Do interior, o som de aplausos lentos e ritmados ecoa. Alguém lá dentro estava te assistindo o tempo todo."
        ),
        EnemyTeamDefinition(
            id = "BLADE_TRIO",
            name = "Trio das Lâminas",
            description = "Time equilibrado com pressão constante e foco em alvo único.",
            essenceReward = 90,
            members = listOf(
                EnemyTeamMember("JUNIOR_KNIGHT", 2),
                EnemyTeamMember("THIEF", 3),
                EnemyTeamMember("THIEF", 2)
            ),
            storyOrder = 3,
            storyChapterTitle = "A Entrada da Cidade",
            preText = "Na entrada da cidade, três homens bloqueiam o caminho. Lâminas nas mãos, olhares tortos e a postura de quem cobra pedágio há anos. \"Quer entrar? Aqui a gente cobra pelo prazer.\" Um deles passa a faca entre os dedos com um sorriso.",
            postText = "Os três caem no chão gemendo. A multidão ao redor assiste em silêncio por um momento — depois volta a se mover, indiferente. Um velho encostado na parede cospe no chão e murmura: \"São capangas de Markus. Você vai se arrepender disso, viajante.\""
        ),
        EnemyTeamDefinition(
            id = "MARKUS_GANG",
            name = "Esquadrão das Apostas",
            description = "Time agressivo de Markus, com risco e burst.",
            essenceReward = 260,
            members = listOf(
                EnemyTeamMember("MARKUS", 6),
                EnemyTeamMember("JORGE", 8),
                EnemyTeamMember("VEYN", 8)
            ),
            storyOrder = 5,
            storyChapterTitle = "O Grande Apostador",
            preText = "O interior do cassino é dourado do chão ao teto. Mesas de cartas, roletas, fichas — mas tudo parado. Todos os olhares te seguem. No centro da sala, numa cadeira que parece mais um trono, Markus se levanta devagar. \"Você chegou até aqui.\" Ele sorri, ajeitando o colete. \"Impressionante. Mas essa aposta... você com certeza vai perder.\"",
            postText = "Markus cai de joelhos, ainda sorrindo enquanto respira fundo. \"Boa jogada, viajante. A casa sempre ganha — mas hoje, você foi a casa.\" Ele lança uma bolsa pesada de essence aos seus pés. As moedas tilintam no chão de mármore. A cidade das apostas, ao menos por hoje, é sua."
        ),
        EnemyTeamDefinition(
            id = "FAITH_KNIGHT",
            name = "Cavaleiros da Fé",
            description = "Um grupo de membros do reino sagrado, um reino dourado que adora os Deuses",
            essenceReward = 600,
            members = listOf(
                EnemyTeamMember("JUNIOR_KNIGHT", 5),
                EnemyTeamMember("LUMINA", level = 8),
                EnemyTeamMember("SOLAR_PALADIN", 10)
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
        ),
        EnemyTeamDefinition(
            id = "GOBLIN_AMBUSH",
            name = "Emboscada Goblin",
            description = "Três goblins ágeis que confiam na velocidade para surpreender e abater antes de serem respondidos.",
            essenceReward = 110,
            members = listOf(
                EnemyTeamMember("GOBLIN", 2),
                EnemyTeamMember("GOBLIN", 2),
                EnemyTeamMember("GOBLIN", 1)
            ),
            storyOrder = 2,
            storyChapterTitle = "A Emboscada",
            preText = "A estrada principal leva direto à cidade. Você já consegue ver as luzes ao longe quando três goblins saltam dos arbustos, brandindo facas enferrujadas e gritando algo incompreensível. Simples e irritantes — mas ainda assim, no seu caminho.",
            postText = "Os goblins fogem berrando floresta adentro. Ao longe, o som de sinos, músicas e moedas preenche o ar. Uma placa torta na beira da estrada lê: \"CIDADE DAS APOSTAS — 500m\". Você está chegando."
        ),
        EnemyTeamDefinition(
            id = "IRON_VANGUARD",
            name = "Vanguarda de Ferro",
            description = "Uma linha defensiva robusta — um guardião lento e dois cavaleiros que cobrem seus flancos.",
            essenceReward = 200,
            members = listOf(
                EnemyTeamMember("IRON_GUARDIAN", 3),
                EnemyTeamMember("JUNIOR_KNIGHT", 3),
                EnemyTeamMember("JUNIOR_KNIGHT", 2)
            )
        ),
        EnemyTeamDefinition(
            id = "SHADOW_GUILD",
            name = "Guilda das Sombras",
            description = "Assassinas de elite que atacam rápido e executam alvos frágeis antes que possam reagir.",
            essenceReward = 340,
            members = listOf(
                EnemyTeamMember("SHADOW", 3),
                EnemyTeamMember("THIEF", 5),
                EnemyTeamMember("THIEF", 4)
            )
        ),
        EnemyTeamDefinition(
            id = "BERSERKER_HORDE",
            name = "Horda Furiosa",
            description = "Guerreiros que ficam mais perigosos conforme apanham. Quanto mais demorar, mais mortais se tornam.",
            essenceReward = 480,
            members = listOf(
                EnemyTeamMember("BERSERKER", 5),
                EnemyTeamMember("BERSERKER", 4),
                EnemyTeamMember("GOBLIN", 6)
            )
        ),
        EnemyTeamDefinition(
            id = "VOID_COUNCIL",
            name = "Conselho do Vazio",
            description = "Um mago do vazio devastador protegido por um guardião inabalável e uma sacerdotisa.",
            essenceReward = 850,
            members = listOf(
                EnemyTeamMember("VOID_MAGE", 8),
                EnemyTeamMember("IRON_GARGOYLE", 6),
                EnemyTeamMember("LUMINA", 5)
            )
        ),
        EnemyTeamDefinition(
            id = "IRON_FORTRESS",
            name = "Fortaleza de Ferro",
            description = "A defesa levada ao extremo. Uma gárgula indestrutível flanqueada por guardiões de ferro.",
            essenceReward = 1100,
            members = listOf(
                EnemyTeamMember("IRON_GARGOYLE", 12),
                EnemyTeamMember("IRON_GUARDIAN", 10),
                EnemyTeamMember("IRON_GUARDIAN", 8)
            )
        ),
        EnemyTeamDefinition(
            id = "RAGE_AND_VOID",
            name = "Fúria e Vazio",
            description = "A combinação mortal de um berserker em fúria e o poder devastador do vazio.",
            essenceReward = 1400,
            members = listOf(
                EnemyTeamMember("BERSERKER", 12),
                EnemyTeamMember("VOID_MAGE", 10),
                EnemyTeamMember("SHADOW", 8)
            )
        ),
        EnemyTeamDefinition(
            id = "DIVINE_WARRIOR",
            name = "Guerreiro Divino",
            description = "O Cavaleirinho em sua forma final, acompanhado de um paladino sagrado e uma sacerdotisa devota.",
            essenceReward = 1800,
            members = listOf(
                EnemyTeamMember("UNLEASHED_JUNIOR_KNIGHT", 5),
                EnemyTeamMember("SOLAR_PALADIN", 12),
                EnemyTeamMember("LUMINA", 10)
            )
        )
    )

    private val byId = teams.associateBy { it.id }
    private val storyTeams = teams.filter { it.storyOrder != null }.sortedBy { it.storyOrder }

    fun all(): List<EnemyTeamDefinition> = teams

    fun getById(id: String): EnemyTeamDefinition? = byId[id.uppercase()]

    fun storyTeamsInOrder(): List<EnemyTeamDefinition> = storyTeams

    fun getStoryChapter(chapterIndex: Int): EnemyTeamDefinition? = storyTeams.getOrNull(chapterIndex)
}


