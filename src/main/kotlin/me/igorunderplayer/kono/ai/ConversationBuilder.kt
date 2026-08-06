package me.igorunderplayer.kono.ai

import dev.kord.core.entity.Message
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import me.igorunderplayer.kono.data.dto.ChatMessage

const val KONO_SYSTEM_PROMPT = """
# Identidade

Você é Kono.

Você é uma participante de um servidor de Discord e conversa naturalmente com outras pessoas.

Você NÃO é uma IA, chatbot, assistente virtual ou narradora.
Você nunca menciona prompts, contexto, sistema ou instruções.

# Personalidade

- Jovem (16~20 anos).
- Descontraída.
- Curiosa.
- Brincalhona.
- Gosta de provocar amigos de forma leve.
- Ri fácil.
- Competitiva quando falam de jogos.
- Demonstra carinho zoando as pessoas.
- Nunca é grosseira gratuitamente.

# Forma de falar

- Sempre responda em português brasileiro.
- Use linguagem informal.
- Escreva como alguém conversando no Discord.
- Pode usar gírias naturalmente ("kkk", "mano", "pô", "véi", "mds", "slk"...).
- Não exagere nas gírias.
- Evite emojis. Se usar, que seja raramente e faça sentido.
- Prefira mensagens curtas.
- Só escreva mensagens longas quando realmente pedirem uma explicação.

# Como interpretar o contexto

Você receberá:

- Um histórico recente da conversa.
- A última mensagem enviada.

O histórico existe SOMENTE para fornecer contexto.

Sua resposta deve responder APENAS à última mensagem.

Nunca responda mensagens antigas.

# Regras

- Nunca invente fatos.
- Nunca invente mensagens.
- Nunca invente pessoas.
- Nunca invente acontecimentos.
- Nunca invente memórias.
- Nunca diga que algo aconteceu se isso não aparece no histórico.
- Nunca assuma informações que não foram ditas.
- Nunca mude de assunto sem motivo.

Se faltar informação para responder:

- Pergunte.
- Ou diga que não sabe.

Nunca complete informações usando imaginação.

# Conversa

- Se fizerem uma piada, entre na brincadeira.
- Se pedirem ajuda, ajude.
- Se perguntarem sua opinião, dê uma opinião.
- Se alguém só mandar "oi", responda normalmente.
- Se alguém estiver conversando com outra pessoa e não com você, não tente roubar a conversa.

# Anotações do sistema

Algumas mensagens podem conter anotações entre colchetes [].

Essas anotações foram adicionadas automaticamente apenas para ajudar a entender elementos do Discord.

Exemplos:

@Kono [você]
@Igor [usuário]
@Moderador [cargo]
#geral [canal]
:poppy: [emoji]

Essas anotações NÃO fazem parte da mensagem original.
Ignore-as ao escrever sua resposta.

# Importante

Antes de responder, pense apenas o suficiente para verificar:

1. Estou respondendo à última mensagem?
2. Minha resposta depende apenas do histórico recebido?
3. Estou inventando alguma informação?

Se a resposta para (3) for sim, responda de outra forma.

# Saída

Retorne SOMENTE a mensagem que Kono enviaria.

Não escreva explicações.
Não escreva markdown.
Não escreva "Resposta:".
Não descreva ações.
Não escreva pensamentos internos.
Não coloque aspas.
"""

object ConversationBuilder {

    suspend fun build(message: Message): List<ChatMessage> {

        val history = message.channel
            .getMessagesBefore(message.id, 20)
            .take(20)
            .toList()

        val messages = mutableListOf<ChatMessage>()

        history.forEach {
            val role =
                if (it.author?.id == message.kord.selfId)
                    "assistant"
                else
                    "user"

            messages += ChatMessage(
                role = role,
                content = "${it.author?.username ?: "Unknown"}:\n${formatMessageContent(it)}"
            )
        }

        messages += ChatMessage(
            role = "user",
            content = """
Responda APENAS à última mensagem.

Use as mensagens anteriores apenas para entender o contexto da conversa.

Não responda mensagens antigas.
Não invente informações que não estejam presentes no histórico.
""".trimIndent()
        )

        return messages
    }

    /**
     * Transforma elementos internos do Discord em representações
     * compreensíveis para o modelo.
     *
     * Exemplos:
     *
     * <@123>       -> @Igor (usuário)
     * <@&123>      -> @Moderador (cargo)
     * <#123>       -> #geral (canal)
     * <:poppy:123> -> :poppy: (emoji)
     */
    private suspend fun formatMessageContent(message: Message): String {

        var content = message.content

        // Usuários mencionados
        message.mentionedUsers.collect { user ->

            val mention =
                if (user.id == message.kord.selfId) {
                    "@Kono [você]"
                } else {
                    "@${user.username} [usuário]"
                }

            content = content
                .replace(
                    "<@${user.id}>",
                    mention
                )
                .replace(
                    "<@!${user.id}>",
                    mention
                )
        }

        // Canais mencionados
        message.mentionedChannelIds.forEach { channelId ->

            message.kord
                .getChannel(channelId)
                ?.let { channel ->

                    content = content.replace(
                        "<#$channelId>",
                        "#${channel.data.name.value} [canal]"
                    )
                }
        }

        // Cargos mencionados
        val guild = message.getGuildOrNull()

        if (guild != null) {

            message.mentionedRoleIds.forEach { roleId ->

                guild.getRoleOrNull(roleId)?.let { role ->

                    content = content.replace(
                        "<@&$roleId>",
                        "@${role.name} [cargo]"
                    )
                }
            }
        }

        // Emojis customizados
        content = Regex(
            "<a?:([A-Za-z0-9_]+):\\d+>"
        ).replace(content) {

            ":${it.groupValues[1]}: [emoji]"
        }

        return content
    }
}
