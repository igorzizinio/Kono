package me.igorunderplayer.kono.domain.battle

object EnemyTeamCatalog {

    data class EnemyTeamMember(
        val cardId: String,
        val level: Int = 1,
        val equipmentIds: List<String> = emptyList()
    )

    data class EnemyTeamDefinition(
        val id: String,
        val name: String,
        val description: String,
        val essenceReward: Int,
        val members: List<EnemyTeamMember>,
        val storyOrder: Int,
        val storyChapterTitle: String,
        val preText: String,
        val postText: String
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
            ),
            storyOrder = 1,
            storyChapterTitle = "A Floresta",
            preText = "Você deixa sua aldeia e parte para a cidade grande. A estrada corta uma floresta densa e quieta demais. De repente, o chão ao redor brilha e estremece. Três massas gelatinosas emergem das sombras — Slimes, famintos e agressivos.",
            postText = "Os slimes explodem em gosma pelo chão da floresta. Você limpa a bota com folhas e continua caminhando. A estrada principal ainda está distante, mas ao menos a floresta ficou para trás."
        ),
        EnemyTeamDefinition(
            id = "GOBLIN_AMBUSH",
            name = "Emboscada Goblin",
            description = "Três goblins ágeis que confiam na velocidade para surpreender e abater antes de serem respondidos.",
            essenceReward = 110,
            members = listOf(
                EnemyTeamMember("GOBLIN", 5, equipmentIds = listOf("DAGGER")),
                EnemyTeamMember("GOBLIN", 3),
                EnemyTeamMember("GOBLIN", 4)
            ),
            storyOrder = 2,
            storyChapterTitle = "A Emboscada",
            preText = "A estrada principal leva direto à cidade. Você já consegue ver as luzes ao longe quando três goblins saltam dos arbustos, brandindo facas enferrujadas e gritando algo incompreensível. Simples e irritantes — mas ainda assim, no seu caminho.",
            postText = "Os goblins fogem berrando floresta adentro. Ao longe, o som de sinos, músicas e moedas preenche o ar. Uma placa torta na beira da estrada lê: \"CIDADE DAS APOSTAS — 500m\". Você está chegando."
        ),
        EnemyTeamDefinition(
            id = "BLADE_TRIO",
            name = "Trio das Lâminas",
            description = "Time equilibrado com pressão constante e foco em alvo único.",
            essenceReward = 90,
            members = listOf(
                EnemyTeamMember("THIEF", 6, equipmentIds = listOf("WOODEN_SWORD", "IRON_ARMOR")),
                EnemyTeamMember("THIEF", 4, equipmentIds = listOf("DAGGER")),
                EnemyTeamMember("THIEF", 5)
            ),
            storyOrder = 3,
            storyChapterTitle = "A Entrada da Cidade",
            preText = "Na entrada da cidade, três homens bloqueiam o caminho. Lâminas nas mãos, olhares tortos e a postura de quem cobra pedágio há anos. \"Quer entrar? Aqui a gente cobra pelo prazer.\" Um deles passa a faca entre os dedos com um sorriso.",
            postText = "Os três caem no chão gemendo. A multidão ao redor assiste em silêncio por um momento — depois volta a se mover, indiferente. Um velho encostado na parede cospe no chão e murmura: \"São capangas de Markus. Você vai se arrepender disso, viajante.\""
        ),
        EnemyTeamDefinition(
            id = "MARKUS_HENCHMEN",
            name = "Capangas de Markus",
            description = "Os capangas de Markus, o grande Apostador",
            essenceReward = 160,
            members = listOf(
                EnemyTeamMember("VEYN", 9, equipmentIds = listOf("QUICK_BOOTS", "VAMPIRE_RING")),
                EnemyTeamMember("JORGE", 6, equipmentIds = listOf("IRON_SHIELD")),
                EnemyTeamMember("AURUM", 6, equipmentIds = listOf("BONE_STAFF"))
            ),
            storyOrder = 4,
            storyChapterTitle = "O Cassino Dourado",
            preText = "A cidade pulsa. Luzes em toda esquina, dados rolando, fichas trocando de mão. No centro, o Cassino Dourado domina tudo — um prédio imenso com letras douradas acesas. Ao se aproximar da entrada, três figuras surgem bloqueando o caminho: Veyn, Jorge e Aurum. \"Negócio fechado para estranhos.\"",
            postText = "Os três recuam sem dizer palavra. A porta de madeira pesada do cassino se abre sozinha — lenta, como se te convidasse para entrar. Do interior, o som de aplausos lentos e ritmados ecoa. Alguém lá dentro estava te assistindo o tempo todo."
        ),
        EnemyTeamDefinition(
            id = "MARKUS_GANG",
            name = "Esquadrão das Apostas",
            description = "Time agressivo de Markus, com risco e burst.",
            essenceReward = 260,
            members = listOf(
                EnemyTeamMember("MARKUS", 6, equipmentIds = listOf("ALL_IN_EMBLEM", "GAMBLER_CHARM")),
                EnemyTeamMember("JORGE", 11, equipmentIds = listOf("HEAVY_IRON_ARMOR", "IRON_SHIELD", "GAMBLER_CHARM")),
                EnemyTeamMember("VEYN", 11, equipmentIds = listOf("POLISHED_KATANA", "GAMBLER_CHARM"))
            ),
            storyOrder = 5,
            storyChapterTitle = "O Grande Apostador",
            preText = "O interior do cassino é dourado do chão ao teto. Mesas de cartas, roletas, fichas — mas tudo parado. Todos os olhares te seguem. No centro da sala, numa cadeira que parece mais um trono, Markus se levanta devagar. \"Você chegou até aqui.\" Ele sorri, ajeitando o colete. \"Impressionante. Mas essa aposta... você com certeza vai perder.\"",
            postText = "Markus cai de joelhos, ainda sorrindo enquanto respira fundo. \"Boa jogada, viajante. A casa sempre ganha — mas hoje, você foi a casa.\" Ele lança uma bolsa pesada de essence aos seus pés. As moedas tilintam no chão de mármore. A cidade das apostas, ao menos por hoje, é sua."
        )
    )

    private val byId = teams.associateBy { it.id }
    private val storyTeams = teams.sortedBy { it.storyOrder }

    fun getById(id: String): EnemyTeamDefinition? = byId[id.uppercase()]

    fun storyTeamsInOrder(): List<EnemyTeamDefinition> = storyTeams

    fun getStoryChapter(chapterIndex: Int): EnemyTeamDefinition? = storyTeams.getOrNull(chapterIndex)
}
