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

        // =========================================================================
        // CHAPTER 7 — O CEMITÉRIO DE LÂMINAS
        // =========================================================================

        EnemyTeamDefinition(
            id = "BLADE_GRAVEYARD",
            name = "Ecos da Guerra Antiga",
            description = "Algo ainda habita o antigo campo de batalha.",
            essenceReward = 460,
            members = listOf(
                EnemyTeamMember("IRON_GUARDIAN", 14, equipmentIds = listOf("HEAVY_IRON_ARMOR", "IRON_SHIELD")),
                EnemyTeamMember("BERSERKER", 12, equipmentIds = listOf("IRON_SWORD", "VAMPIRE_RING")),
                EnemyTeamMember("SHADOW", 12, equipmentIds = listOf("POLISHED_KATANA", "QUICK_BOOTS"))
            ),
            storyOrder = 7,
            storyChapterTitle = "O Cemitério de Lâminas",
            preText =
                "A estrada muda.\n\n" +
                        "A vegetação desaparece aos poucos, substituída por terra escura e seca.\n\n" +
                        "Não existem pássaros.\n\n" +
                        "Não existe vento.\n\n" +
                        "Então você vê.\n\n" +
                        "Lâminas.\n\n" +
                        "Centenas.\n\n" +
                        "Não… milhares.\n\n" +
                        "Espadas, lanças e armaduras cobrem o vale inteiro como lápides de uma guerra esquecida.\n\n" +
                        "No centro do campo…\n\n" +
                        "Uma espada colossal atravessa a terra.\n\n" +
                        "Grande demais para qualquer humano empunhar.\n\n" +
                        "E mesmo séculos depois…\n\n" +
                        "Ela não possui um único sinal de ferrugem.",
            postText =
                "O silêncio retorna.\n\n" +
                        "Mas algo parece errado.\n\n" +
                        "Você sente.\n\n" +
                        "Como se alguma coisa naquele lugar ainda estivesse viva.\n\n" +
                        "Ao tocar uma das lâminas enterradas…\n\n" +
                        "Imagens invadem sua mente.\n\n" +
                        "Um campo em chamas.\n" +
                        "O céu partido.\n" +
                        "Exércitos inteiros caindo.\n\n" +
                        "E ao longe…\n\n" +
                        "Uma figura colossal caminhando sozinha através da guerra."
        ),


        // =========================================================================
        // CHAPTER 8 — A CIDADE DOURADA
        // =========================================================================

        EnemyTeamDefinition(
            id = "AUREA_GATE",
            name = "Guardas dos Portões de Aurea",
            description = "Soldados de elite treinados para impedir que ameaças entrem pela porta principal.",
            essenceReward = 620,
            members = listOf(
                EnemyTeamMember(
                    "GOLDEN_KNIGHT", 16,
                    equipmentIds = listOf("HEAVY_IRON_ARMOR", "IRON_SHIELD", "ELIXIR_VIAL")
                ),
                EnemyTeamMember("AUREA_SOLDIER", 13, equipmentIds = listOf("IRON_SWORD", "IRON_ARMOR")),
                EnemyTeamMember("AUREA_SOLDIER", 12, equipmentIds = listOf("BONE_STAFF", "IRON_ARMOR"))
            ),
            storyOrder = 8,
            storyChapterTitle = "A Cidade Dourada",
            preText =
                "Você a vê antes de chegar.\n\n" +
                        "Não porque ela é alta.\n" +
                        "Não porque ela é grande.\n\n" +
                        "Porque ela brilha.\n\n" +
                        "Aurea.\n\n" +
                        "Uma cidade construída inteira de pedra clara\n" +
                        "com detalhes dourados que capturam o sol\n" +
                        "e o devolvem em ângulos que parecem calculados por alguém\n" +
                        "que passou a vida inteira pensando em como a luz se move.\n\n" +
                        "Torres que não existem para impor.\n" +
                        "Existem para iluminar.\n\n" +
                        "O cheiro de incenso chega antes do barulho.\n" +
                        "E o barulho de Aurea é diferente de qualquer cidade que você já visitou.\n\n" +
                        "Não é o barulho do comércio.\n" +
                        "Não é o barulho da guerra.\n\n" +
                        "É o barulho de pessoas que acreditam em algo\n" +
                        "e constroem esse algo toda manhã com as próprias mãos.\n\n" +
                        "Sinos.\n" +
                        "Passos ritmados em formação.\n" +
                        "O som metálico de armas em treinamento que nunca para.\n\n" +
                        "Você chega aos portões.\n\n" +
                        "E percebe que os guardas não estavam esperando.\n" +
                        "Estavam treinando.\n\n" +
                        "Mesmo em postura de guarda.\n" +
                        "Mesmo parados.\n" +
                        "Há algo nos ombros deles, na forma como seguram as armas,\n" +
                        "que diz que o treino nunca acabou.\n" +
                        "Apenas pausou.\n\n" +
                        "\"Viajante.\"\n\n" +
                        "O Cavaleiro Dourado no centro fala sem hesitação.\n\n" +
                        "\"Você vem do Sul.\"\n" +
                        "\"Pelo caminho que atravessa o vale.\"\n\n" +
                        "Ele te olha de cima a baixo.\n" +
                        "Não com desdém.\n" +
                        "Com avaliação.\n\n" +
                        "\"Esse caminho passa pelo Cemitério de Lâminas.\"\n\n" +
                        "Uma pausa.\n\n" +
                        "\"Ninguém que passa pelo Cemitério\n" +
                        "entra em Aurea sem demonstrar que veio em paz.\"\n\n" +
                        "Ele dá um passo à frente.\n\n" +
                        "\"Aqui em Aurea, paz se demonstra de uma forma só.\"\n\n" +
                        "A mão vai para a empunhadura.\n\n" +
                        "\"No campo.\"\n\n" +
                        "Os dois soldados atrás dele assumem posição.\n\n" +
                        "\"Se você vencer, as portas abrem.\"\n" +
                        "\"Se você perder…\"\n\n" +
                        "Ele desembainha.\n\n" +
                        "\"Vai saber que não estava pronto para entrar.\"",
            postText =
                "O Cavaleiro Dourado cai sobre um joelho.\n\n" +
                        "Respira fundo.\n\n" +
                        "Então ergue a cabeça e te olha com algo que não estava lá antes.\n\n" +
                        "Respeito.\n\n" +
                        "Não o respeito de quem tem medo.\n" +
                        "O respeito de um guerreiro que reconheceu outro.\n\n" +
                        "\"Bem-vindo a Aurea.\"\n\n" +
                        "Os portões se abrem.\n\n" +
                        "Por dentro a cidade é diferente do que você esperava.\n\n" +
                        "Não é um lugar de silêncio e veneração.\n" +
                        "É um lugar de movimento.\n\n" +
                        "Soldados treinando em cada praça aberta.\n" +
                        "Jovens correndo com pesos nas costas pelas ruas de pedra.\n" +
                        "Ferreiros que trabalham com uma precisão que parece ritual.\n" +
                        "Crianças que imitam posturas de combate\n" +
                        "com gravidade que não deveria ser possível nessa idade.\n\n" +
                        "Em Aurea, força não é uma conquista.\n" +
                        "É uma obrigação.\n\n" +
                        "No topo da escadaria da catedral central:\n\n" +
                        "Uma mulher de vestes brancas e douradas.\n" +
                        "Parada.\n" +
                        "Observando você desde quando entrou.\n\n" +
                        "Ela desce dois degraus.\n\n" +
                        "\"Viajante.\"\n\n" +
                        "A voz é calma.\n" +
                        "Não performática.\n" +
                        "Calma de quem não precisa elevar o tom para ser ouvida.\n\n" +
                        "\"Meu nome é Lumina.\"\n\n" +
                        "Ela olha para os guardas ainda se recuperando.\n\n" +
                        "\"Poucos chegam até a porta e vencem a primeira prova.\"\n\n" +
                        "Um leve inclinar de cabeça.\n\n" +
                        "\"Você deve ter muitas perguntas.\"\n" +
                        "\"E eu tenho algumas também.\"\n\n" +
                        "\"Venha.\"\n" +
                        "\"O Rei vai querer saber que você chegou.\""
        ),

        // =========================================================================
        // CHAPTER 9 — A CHAMA DO SOL
        // =========================================================================

        EnemyTeamDefinition(
            id = "SOLAR_TOURNAMENT",
            name = "Campeões do Torneio da Chama",
            description = "Os três guerreiros mais fortes de Aurea. Vencê-los é a maior honra que um forasteiro pode receber.",
            essenceReward = 820,
            members = listOf(
                EnemyTeamMember(
                    "GOLDEN_KNIGHT", 17,
                    equipmentIds = listOf("HEAVY_IRON_ARMOR", "IRON_SHIELD", "SOLARBRAND")
                ),
                EnemyTeamMember(
                    "AUREA_SOLDIER", 16,
                    equipmentIds = listOf("AURE_GOLDEN_ARMOR", "GREATSWORD")
                ),
                EnemyTeamMember(
                    "SUN_PRIESTESS", 15,
                    equipmentIds = listOf("DEVOTION_STAFF", "AURE_GOLDEN_ARMOR")
                )
            ),
            storyOrder = 9,
            storyChapterTitle = "A Chama do Sol",
            preText =
                "Lumina explicou por horas.\n\n" +
                        "Sobre Aurea.\n" +
                        "Sobre o Sol.\n" +
                        "Sobre a crença de que o poder solar que existiu na era antiga\n" +
                        "ainda está no mundo na forma de fé praticada.\n\n" +
                        "\"Em Aurea, fé não é o que você sente.\"\n" +
                        "\"É o que você faz.\"\n\n" +
                        "Ela disse isso com a convicção de quem nunca duvidou.\n" +
                        "E você percebeu:\n" +
                        "ela está certa sobre pelo menos isso.\n\n" +
                        "Cada pessoa que você viu desde que entrou na cidade\n" +
                        "não está esperando o Sol aparecer.\n" +
                        "Está construindo algo.\n" +
                        "Treinando algo.\n" +
                        "Forjando algo.\n\n" +
                        "\"O Torneio da Chama acontece toda lua nova.\"\n" +
                        "Lumina disse enquanto guiava você pela cidade.\n" +
                        "\"É a forma que temos de medir onde estamos.\"\n" +
                        "\"E de descobrir quem ainda tem o que crescer.\"\n\n" +
                        "\"O Rei assiste a todos os torneios.\"\n" +
                        "\"Pessoalmente.\"\n" +
                        "\"Sempre.\"\n\n" +
                        "\"E quando vê algo que lhe interessa…\"\n" +
                        "\"ele convida para uma conversa.\"\n\n" +
                        "Ela parou na entrada de uma arena aberta ao céu.\n\n" +
                        "Centenas de pessoas nas arquibancadas.\n" +
                        "O calor de uma tarde de sol pleno pressionando de cima.\n" +
                        "E no centro do campo:\n" +
                        "terra batida marcada pelos passos de incontáveis torneios anteriores.\n\n" +
                        "\"Você poderia observar.\"\n" +
                        "Lumina falou sem olhar para você.\n" +
                        "\"Mas imagino que não é esse o seu estilo.\"\n\n" +
                        "As arquibancadas começaram a perceber sua presença.\n" +
                        "Murmúrios.\n" +
                        "Um forasteiro.\n\n" +
                        "No camarote mais alto:\n" +
                        "uma figura de armadura dourada observa tudo com braços cruzados.\n\n" +
                        "O Rei de Aurea.\n\n" +
                        "Ele não demonstra reação quando te vê.\n" +
                        "Mas também não desvia o olhar.\n\n" +
                        "Os três campeões do torneio deste ciclo entram no campo.\n\n" +
                        "E um arauto anuncia:\n\n" +
                        "\"O forasteiro aceita o desafio da Chama.\"\n\n" +
                        "Você não disse isso ainda.\n" +
                        "Mas todo mundo nas arquibancadas já sabe que vai.\n\n" +
                        "E honestamente…\n\n" +
                        "eles não estão errados.",
            postText =
                "O último campeão cai.\n\n" +
                        "A arena fica em silêncio por exatamente três segundos.\n\n" +
                        "Então explode.\n\n" +
                        "Não o barulho de uma multidão empolgada.\n" +
                        "O barulho específico de pessoas que estão vendo algo\n" +
                        "que não achavam que veriam em vida.\n\n" +
                        "Os três campeões estão no chão.\n" +
                        "Os três melhores guerreiros de Aurea.\n" +
                        "Ao mesmo tempo.\n" +
                        "Por um único forasteiro.\n\n" +
                        "Lumina, ao lado do campo, não demonstra surpresa.\n" +
                        "Ela sorri.\n" +
                        "Levemente.\n" +
                        "Como alguém que apostou em algo e ganhou\n" +
                        "mas que considerava educado não vibrar demais.\n\n" +
                        "Do camarote:\n\n" +
                        "O Rei de Aurea se levanta.\n\n" +
                        "Devagar.\n" +
                        "Com o peso de alguém que não faz movimentos sem intenção.\n\n" +
                        "Ele te olha do alto.\n\n" +
                        "A distância é grande o suficiente para que você não veja\n" +
                        "a expressão no rosto dele com clareza.\n\n" +
                        "Mas você vê o gesto.\n\n" +
                        "Ele aponta para você.\n" +
                        "Depois aponta para cima.\n" +
                        "Para o camarote.\n\n" +
                        "Um convite.\n\n" +
                        "Lumina se aproxima.\n\n" +
                        "\"Isso não acontece normalmente.\"\n\n" +
                        "Uma pausa.\n\n" +
                        "\"Na verdade, isso nunca aconteceu.\"\n\n" +
                        "\"Vá.\"\n" +
                        "\"Mas saiba:\"\n\n" +
                        "\"O Rei de Aurea não convida para conversa.\"\n" +
                        "\"Ele convida para mais um teste.\"\n\n" +
                        "\"E o próximo não vai ser com espadas.\""
        ),

        // =========================================================================
        // CHAPTER 10 — O REI DE AUREA
        // =========================================================================

        EnemyTeamDefinition(
            id = "AURE_KING_DUEL",
            name = "O Rei de Aurea",
            description = "O monarca não confia em palavras. Nunca confiou. Cada aliança que Aurea já fez começou com um duelo.",
            essenceReward = 1100,
            members = listOf(
                EnemyTeamMember(
                    "AURE_KING", 19,
                    equipmentIds = listOf("SOLARBRAND", "AURE_GOLDEN_ARMOR")
                ),
                EnemyTeamMember(
                    "GOLDEN_KNIGHT", 17,
                    equipmentIds = listOf("HEAVY_IRON_ARMOR", "IRON_SHIELD", "ELIXIR_VIAL")
                ),
                EnemyTeamMember(
                    "GOLDEN_KNIGHT", 16,
                    equipmentIds = listOf("AURE_GOLDEN_ARMOR", "SIEGEBREAKER")
                )
            ),
            storyOrder = 10,
            storyChapterTitle = "O Rei de Aurea",
            preText =
                "O camarote do Rei é simples.\n\n" +
                        "Isso é a primeira coisa que você nota.\n\n" +
                        "Sem ornamentos desnecessários.\n" +
                        "Sem tapetes caros.\n" +
                        "Sem serventes esperando com comida e vinho.\n\n" +
                        "Uma cadeira.\n" +
                        "Uma mesa com mapas.\n" +
                        "E o Rei de Aurea, que já estava de pé\n" +
                        "quando você entrou.\n\n" +
                        "De perto, ele é exatamente o que a armadura sugeria à distância.\n\n" +
                        "Grande.\n" +
                        "Não de forma intimidatória.\n" +
                        "De forma funcional.\n" +
                        "Como alguém cujo corpo foi construído por décadas de exigência real\n" +
                        "e não por vaidade.\n\n" +
                        "Os cabelos grisalhos cortados curtos.\n" +
                        "Uma cicatriz que vai do queixo até a orelha esquerda.\n" +
                        "Olhos escuros que te avaliam com a mesma precisão\n" +
                        "com que os guardas da porta te avaliaram.\n" +
                        "Mas com mais camadas.\n\n" +
                        "Mais história.\n\n" +
                        "\"Você derrotou três campeões.\"\n\n" +
                        "Não é elogio.\n" +
                        "É dado.\n\n" +
                        "\"Dois deles eu não conseguiria derrotar ao mesmo tempo\n" +
                        "quando tinha sua idade.\"\n\n" +
                        "Ele dobra os braços.\n\n" +
                        "\"Isso me interessa.\"\n" +
                        "\"E me preocupa.\"\n\n" +
                        "\"Me interessa porque poder assim é raro.\"\n" +
                        "\"Me preocupa porque poder assim indo para o Norte…\"\n\n" +
                        "Uma pausa.\n\n" +
                        "\"Não costuma terminar bem para ninguém.\"\n\n" +
                        "Ele pega os mapas da mesa e os dobra com cuidado.\n" +
                        "Metódico.\n" +
                        "Como se precisasse das mãos ocupadas para pensar direito.\n\n" +
                        "\"Lumina me disse que você passou pelo Cemitério.\"\n" +
                        "\"E que seus olhos tinham o peso de quem tocou as lâminas\n" +
                        "e viu o que não devia ver.\"\n\n" +
                        "Ele te olha direto.\n\n" +
                        "\"Então vou ser direto também.\"\n\n" +
                        "\"Minha família guarda algo há seis gerações.\"\n" +
                        "\"Um registro.\"\n" +
                        "\"Sobre o que existia no Norte antes de Kono reorganizar o mundo.\"\n\n" +
                        "\"Nunca mostrei a ninguém.\"\n" +
                        "\"Nunca pretendi mostrar.\"\n\n" +
                        "\"Mas preciso saber primeiro se você é capaz de carregar\n" +
                        "o peso do que vai ler.\"\n\n" +
                        "Os dois Cavaleiros Dourados atrás dele assumem posição.\n\n" +
                        "O Rei desembainha a lâmina.\n\n" +
                        "Não com ameaça.\n" +
                        "Com a naturalidade de quem fez isso dez mil vezes\n" +
                        "e vai fazer dez mil mais.\n\n" +
                        "\"Em Aurea a gente não usa palavras para medir pessoas.\"\n\n" +
                        "\"Você sabe o que fazemos.\"\n\n" +
                        "Ele assume postura.\n\n" +
                        "Perfeita.\n" +
                        "Sem esforço aparente.\n\n" +
                        "\"Mostre-me que você merece saber.\"",
            postText =
                "O Rei cai sobre um joelho.\n\n" +
                        "A lâmina crava no chão à sua frente.\n\n" +
                        "Os dois Cavaleiros Dourados trocam um olhar.\n" +
                        "Nenhum dos dois diz nada.\n" +
                        "Não existe palavra para o que acabaram de ver.\n\n" +
                        "O Rei respira fundo.\n\n" +
                        "Uma vez.\n" +
                        "Duas.\n\n" +
                        "Então ri.\n\n" +
                        "Baixo.\n" +
                        "Genuíno.\n" +
                        "O riso de alguém que não ria assim fazia tempo.\n\n" +
                        "\"Bom.\"\n\n" +
                        "Ele se levanta sem ajuda.\n" +
                        "Com dificuldade, mas sem aceitar ajuda.\n" +
                        "Você aprende algo sobre ele nisso.\n\n" +
                        "\"Venha.\"\n\n" +
                        "---\n\n" +
                        "O registro fica numa câmara abaixo do camarote.\n\n" +
                        "Menor do que o espaço acima.\n" +
                        "Mais velha.\n" +
                        "Com paredes de pedra que pertencem a uma construção anterior\n" +
                        "à cidade que existe hoje.\n\n" +
                        "No centro:\n" +
                        "uma caixa de ferro.\n" +
                        "Simples.\n" +
                        "Sem ornamentos.\n\n" +
                        "O Rei a abre sem cerimônia.\n\n" +
                        "Dentro:\n" +
                        "um único documento.\n" +
                        "Dobrado.\n" +
                        "Com uma caligrafia que você não reconhece,\n" +
                        "mas numa língua que ainda é legível.\n\n" +
                        "O Rei não o entrega.\n" +
                        "Apenas lê em voz alta.\n\n" +
                        "\"'O portador das lâminas solares não era deus.\"\n" +
                        "\"Não era rei.\"\n" +
                        "\"Não era homem.\"\n" +
                        "\"Era algo que existia antes de qualquer nome\"\n" +
                        "\"que pudéssemos dar a poder.\"\n" +
                        "\"E quando ele empunhou o fogo solar,\"\n" +
                        "\"os deuses da era antiga não recuaram por fraqueza.\"\n" +
                        "\"Recuaram por reconhecimento.\"\n" +
                        "\"Como se afasta de um incêndio\"\n" +
                        "\"não por medo, mas por respeito ao que o fogo é.\"'\n\n" +
                        "O Rei fecha o documento.\n" +
                        "Dobra de volta.\n" +
                        "Coloca na caixa.\n\n" +
                        "\"Seis gerações da minha família acreditaram\n" +
                        "que esse portador era Aurea.\"\n" +
                        "\"Que nossa cidade foi fundada por seus descendentes.\"\n" +
                        "\"Que o poder solar que celebramos é herança direta dele.\"\n\n" +
                        "Ele fecha a caixa.\n\n" +
                        "\"Eu acreditei nisso a vida inteira.\"\n\n" +
                        "Uma pausa que pesa mais do que deveria.\n\n" +
                        "\"Mas há algo que nunca coube na história.\"\n\n" +
                        "Ele te olha.\n\n" +
                        "\"Se o portador era de Aurea…\"\n" +
                        "\"por que o único registro que temos dele\"\n" +
                        "\"nunca menciona a cidade?\"\n\n" +
                        "\"Por que as lâminas que carregava\"\n" +
                        "\"nunca foram encontradas nos nossos registros de armas?\"\n\n" +
                        "\"Por que quando eu olho para o Norte…\"\n\n" +
                        "Ele não termina a frase.\n\n" +
                        "\"Vá.\"\n" +
                        "\"Mas saiba:\"\n\n" +
                        "\"O que você vai encontrar lá em cima\"\n" +
                        "\"não pertence a Aurea.\"\n\n" +
                        "\"E suspeito que também não pertencia\"\n" +
                        "\"a quem quer que tenha tentado reivindicá-lo antes.\"\n\n" +
                        "Ele apaga a tocha da câmara.\n\n" +
                        "\"Lumina vai te dar provisões para o Norte.\"\n" +
                        "\"E se você sobreviver ao que está lá…\"\n\n" +
                        "Uma última pausa na escuridão.\n\n" +
                        "\"Volte.\"\n" +
                        "\"Eu quero saber como essa história termina.\""
        ),
    )

    private val byId = teams.associateBy { it.id }
    private val storyTeams = teams.sortedBy { it.storyOrder }

    fun getById(id: String): EnemyTeamDefinition? = byId[id.uppercase()]

    fun storyTeamsInOrder(): List<EnemyTeamDefinition> = storyTeams

    fun getStoryChapter(chapterIndex: Int): EnemyTeamDefinition? =
        storyTeams.getOrNull(chapterIndex)
}
