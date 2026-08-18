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

        // =========================================================================
        // CHAPTER 1 — A FLORESTA
        // =========================================================================

        EnemyTeamDefinition(
            id = "SLIME_SWARM",
            name = "Enxame de Slimes",
            description = "Três criaturas viscosas que abusam de volume.",
            essenceReward = 70,
            members = listOf(
                EnemyTeamMember("SLIME", 3),
                EnemyTeamMember("SLIME", 3),
                EnemyTeamMember("SLIME", 3)
            ),
            storyOrder = 1,
            storyChapterTitle = "A Floresta",
            preText =
                "Você deixa sua aldeia ao amanhecer.\n\n" +
                        "A estrada principal corta uma floresta densa e silenciosa. " +
                        "O cheiro de terra molhada domina o ar.\n\n" +
                        "Então algo se move.\n\n" +
                        "Pequenas massas gelatinosas emergem entre raízes e pedras. " +
                        "Slimes.\n\n" +
                        "Criaturas fracas… mas famintas.",
            postText =
                "Os slimes explodem em gosma pelo chão da floresta.\n\n" +
                        "Você limpa a lâmina nas folhas e continua seguindo estrada adentro.\n\n" +
                        "A cidade ainda está longe."
        ),

        // =========================================================================
        // CHAPTER 2 — A EMBOSCADA
        // =========================================================================

        EnemyTeamDefinition(
            id = "GOBLIN_AMBUSH",
            name = "Emboscada Goblin",
            description = "Goblinóides rápidos e oportunistas.",
            essenceReward = 120,
            members = listOf(
                EnemyTeamMember("GOBLIN", 6, equipmentIds = listOf("DAGGER")),
                EnemyTeamMember("GOBLIN", 5, equipmentIds = listOf("IRON_ARMOR")),
                EnemyTeamMember("GOBLIN", 6, equipmentIds = listOf("DAGGER"))
            ),
            storyOrder = 2,
            storyChapterTitle = "A Emboscada",
            preText =
                "As árvores começam a se abrir.\n\n" +
                        "Ao longe, você já consegue enxergar fumaça, luzes e sinais de civilização.\n\n" +
                        "Então um grito ecoa dos arbustos.\n\n" +
                        "\"PEGUEM ELE!\"\n\n" +
                        "Três goblins saltam na estrada com armas improvisadas e olhos famintos.",
            postText =
                "Os goblins fogem desesperados floresta adentro.\n\n" +
                        "A estrada principal finalmente aparece diante de você.\n\n" +
                        "Ao longe, uma placa torta balança com o vento:\n\n" +
                        "\"CIDADE DAS APOSTAS — 500m\""
        ),

        // =========================================================================
        // CHAPTER 3 — A ENTRADA DA CIDADE
        // =========================================================================

        EnemyTeamDefinition(
            id = "BLADE_TRIO",
            name = "Trio das Lâminas",
            description = "Criminosos locais que controlam a entrada da cidade.",
            essenceReward = 165,
            members = listOf(
                EnemyTeamMember("THIEF", 8, equipmentIds = listOf("IRON_SWORD", "IRON_ARMOR")),
                EnemyTeamMember("THIEF", 6, equipmentIds = listOf("DAGGER", "IRON_ARMOR")),
                EnemyTeamMember("THIEF", 7, equipmentIds = listOf("KATANA"))
            ),
            storyOrder = 3,
            storyChapterTitle = "A Entrada da Cidade",
            preText =
                "A cidade pulsa.\n\n" +
                        "Músicas altas.\n" +
                        "Moedas tilintando.\n" +
                        "Gente bêbada.\n\n" +
                        "Mas antes mesmo de cruzar os portões, três homens bloqueiam seu caminho.\n\n" +
                        "\"Pedágio.\"\n\n" +
                        "Um deles gira a faca entre os dedos enquanto sorri.\n\n" +
                        "\"Todo mundo paga pra entrar.\"",
            postText =
                "Os três caem derrotados diante dos portões.\n\n" +
                        "A multidão observa em silêncio por alguns segundos… antes de voltar à rotina.\n\n" +
                        "Um velho encostado na parede cospe no chão.\n\n" +
                        "\"Capangas do Markus.\"\n\n" +
                        "\"Você acabou de chamar atenção.\""
        ),

        // =========================================================================
        // CHAPTER 4 — O CASSINO DOURADO
        // =========================================================================

        EnemyTeamDefinition(
            id = "MARKUS_HENCHMEN",
            name = "Capangas de Markus",
            description = "Os homens de confiança do grande apostador.",
            essenceReward = 270,
            members = listOf(
                EnemyTeamMember("VEYN", 9, equipmentIds = listOf("POLISHED_KATANA", "QUICK_BOOTS")),
                EnemyTeamMember("JORGE", 9, equipmentIds = listOf("HEAVY_IRON_ARMOR", "IRON_SHIELD")),
                EnemyTeamMember("AURUM", 9, equipmentIds = listOf("BONE_STAFF", "IRON_ARMOR"))
            ),
            storyOrder = 4,
            storyChapterTitle = "O Cassino Dourado",
            preText =
                "No centro da cidade existe um prédio impossível de ignorar.\n\n" +
                        "O Cassino Dourado.\n\n" +
                        "Luzes douradas iluminam a rua inteira.\n" +
                        "Guardas observam cada movimento.\n\n" +
                        "Quando você sobe os degraus da entrada… três figuras surgem bloqueando o caminho.\n\n" +
                        "\"Negócio fechado pra estranhos.\"",
            postText =
                "Os três recuam em silêncio.\n\n" +
                        "As portas do cassino se abrem lentamente.\n\n" +
                        "Lá dentro… alguém bate palmas.\n\n" +
                        "Lentas.\n\n" +
                        "Como se estivesse esperando por você."
        ),

        // =========================================================================
        // CHAPTER 5 — O GRANDE APOSTADOR
        // =========================================================================

        EnemyTeamDefinition(
            id = "MARKUS_GANG",
            name = "Esquadrão das Apostas",
            description = "Markus e seu grupo apostam tudo em combate.",
            essenceReward = 420,
            members = listOf(
                EnemyTeamMember("MARKUS", 12, equipmentIds = listOf("ALL_IN_EMBLEM", "GAMBLER_CHARM")),
                EnemyTeamMember("JORGE", 14, equipmentIds = listOf("HEAVY_IRON_ARMOR", "IRON_SHIELD", "ELIXIR_VIAL")),
                EnemyTeamMember("VEYN", 14, equipmentIds = listOf("POLISHED_KATANA", "GAMBLER_CHARM", "VAMPIRE_RING"))
            ),
            storyOrder = 5,
            storyChapterTitle = "O Grande Apostador",
            preText =
                "O interior do cassino está vazio.\n\n" +
                        "Mesas abandonadas.\n" +
                        "Roletas paradas.\n" +
                        "Fichas espalhadas pelo chão.\n\n" +
                        "No centro da sala, sentado como um rei em seu trono, Markus observa você.\n\n" +
                        "\"Impressionante.\"\n\n" +
                        "Ele sorri.\n\n" +
                        "\"Mas toda aposta termina da mesma forma.\"\n\n" +
                        "\"A casa sempre ganha.\"",
            postText =
                "Markus cai de joelhos ainda sorrindo.\n\n" +
                        "\"Boa jogada, viajante...\"\n\n" +
                        "\"Hoje... você foi a casa.\" \n\n" +
                        "Ele joga uma bolsa pesada de essence aos seus pés.\n\n" +
                        "Pela primeira vez desde que chegou na cidade… ninguém ousa ficar no seu caminho."
        ),

        // =========================================================================
        // CHAPTER 6 — LADRÕES DE ESTRADA
        // =========================================================================

        EnemyTeamDefinition(
            id = "ROAD_BANDITS",
            name = "Ladrões de Estrada",
            description = "Bandidos fracos tentando lucrar com sua fama.",
            essenceReward = 200,
            members = listOf(
                EnemyTeamMember("THIEF", 7, equipmentIds = listOf("DAGGER")),
                EnemyTeamMember("GOBLIN", 11),
                EnemyTeamMember("THIEF", 6)
            ),
            storyOrder = 6,
            storyChapterTitle = "Estrada Silenciosa",
            preText =
                "A cidade das apostas fica para trás.\n\n" +
                        "A estrada volta a ficar silenciosa.\n\n" +
                        "\"Ei. Mochila no chão.\" \n\n" +
                        "Três figuras surgem ao lado de uma carroça quebrada.\n\n" +
                        "\"Você saiu daquele cassino vivo.\"\n\n" +
                        "\"Então deve estar carregando muita essence.\"",
            postText =
                "Os homens fogem desesperados estrada abaixo.\n\n" +
                        "Um deles tropeça antes de desaparecer no escuro.\n\n" +
                        "\"Seu monstro...\"\n\n" +
                        "\"Vai pro Norte então!\"\n\n" +
                        "\"Vai morrer igual todos os outros!\""
        ),
    )

    private val byId = teams.associateBy { it.id }
    private val storyTeams = teams.sortedBy { it.storyOrder }

    fun getById(id: String): EnemyTeamDefinition? = byId[id.uppercase()]

    fun storyTeamsInOrder(): List<EnemyTeamDefinition> = storyTeams

    fun getStoryChapter(chapterIndex: Int): EnemyTeamDefinition? =
        storyTeams.getOrNull(chapterIndex)
}
