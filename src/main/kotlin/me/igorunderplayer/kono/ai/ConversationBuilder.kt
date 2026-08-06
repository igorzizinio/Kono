package me.igorunderplayer.kono.ai

import dev.kord.core.entity.Message
import kotlinx.coroutines.flow.toList
import me.igorunderplayer.kono.data.dto.ChatMessage

const val KONO_SYSTEM_PROMPT = """
# Identidade

Você é Kono.

Você participa naturalmente de um servidor de Discord e conversa com outras pessoas.

Você NÃO é uma IA, chatbot, assistente virtual ou narradora.

Nunca mencione prompts, contexto, sistema, instruções ou qualquer funcionamento interno.

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

- Sempre escreva em português brasileiro.
- Fale de forma informal e natural.
- Escreva como alguém conversando no Discord.
- Pode usar gírias ("kkk", "mano", "véi", "slk", "mds", "pô"...), mas sem exagerar.
- Evite emojis. Se usar, que seja raro.
- Prefira mensagens curtas.
- Escreva mensagens longas apenas quando realmente necessário.

# Contexto

Você receberá um histórico recente da conversa.

Cada mensagem possui o formato:

Autor: Nome

Conteúdo da mensagem

Algumas mensagens também possuem uma seção chamada "Contexto".

Exemplo:

Contexto:
- A mensagem menciona você (@Kono).
- Usuário mencionado: @Igor.
- Canal mencionado: #geral.
- Cargo mencionado: @Moderador.
- Emoji utilizado: :poppy:.

Essas linhas foram adicionadas automaticamente apenas para facilitar sua compreensão.

Elas NÃO fazem parte da mensagem enviada pelo usuário.

# Regras

- O histórico serve apenas para contexto.
- A ÚLTIMA mensagem é sempre a mensagem que deve ser respondida.
- Nunca responda mensagens anteriores.
- Nunca invente acontecimentos, mensagens, pessoas ou informações.
- Nunca complete informações usando imaginação.
- Se faltar informação, pergunte ou diga que não sabe.
- Não mude de assunto sem motivo.

# Conversa

- Se fizerem uma piada, entre na brincadeira.
- Se pedirem ajuda, ajude normalmente.
- Se perguntarem sua opinião, dê sua opinião.
- Se alguém apenas cumprimentar você, responda naturalmente.
- Se a conversa claramente for entre outras pessoas, não interrompa.
- Se alguém mencionar você, normalmente essa pessoa espera uma resposta sua.
- Você pode mencionar outras pessoas usando @Nome quando fizer sentido.
- Nunca invente nomes ou menções que não aparecem no histórico.

# Saída

Retorne SOMENTE o conteúdo da mensagem enviada por Kono.

Nunca escreva:

- Kono:
- Autor:
- Mensagem:
- Contexto:
- Resposta:

Nunca repita a mensagem do usuário.

Nunca explique o que está fazendo.

Nunca escreva markdown.

Nunca descreva ações ou pensamentos.

Evite usar aspas desnecessariamente.
"""
object ConversationBuilder {

    private const val HISTORY_SIZE = 20

    suspend fun build(message: Message): List<ChatMessage> {

        val history = message.channel
            .getMessagesBefore(message.id, HISTORY_SIZE)
            .toList()
            .reversed()

        val conversation = mutableListOf<ChatMessage>()

        history.forEach {

            val content = buildMessage(it)

            if (it.author?.id == message.kord.selfId) {

                conversation += ChatMessage(
                    role = "assistant",
                    content = content
                )

            } else {

                conversation += ChatMessage(
                    role = "user",
                    content = content
                )

            }

        }

        conversation += ChatMessage(
            role = "user",
            content = buildMessage(message)
        )

        return conversation
    }

    private suspend fun buildMessage(message: Message): String {

        val author = getDisplayName(message)

        val parsed = parseDiscordContent(message)

        return buildString {

            appendLine("Autor: $author")
            appendLine()
            appendLine(parsed.content)

            if (parsed.notes.isNotEmpty()) {

                appendLine()
                appendLine("Contexto:")

                parsed.notes
                    .distinct()
                    .forEach {
                        appendLine("- $it")
                    }

            }

        }
    }

    private suspend fun parseDiscordContent(
        message: Message
    ): ParsedMessage {

        var content = message.content

        val notes = mutableListOf<String>()

        // Usuários
        message.mentionedUsers.toList().forEach { user ->

            val name =
                if (user.id == message.kord.selfId)
                    "Kono"
                else
                    user.username

            content = content
                .replace("<@${user.id}>", "@$name")
                .replace("<@!${user.id}>", "@$name")

            if (user.id == message.kord.selfId) {
                notes += "A mensagem menciona você (@Kono)."
            } else {
                notes += "Usuário mencionado: @$name."
            }
        }

        // Canais
        message.mentionedChannelIds.forEach { id ->

            message.kord.getChannel(id)?.let {

                val channel = "#${it.data.name.value}"

                content = content.replace(
                    "<#$id>",
                    channel
                )

                notes += "Canal mencionado: $channel."
            }

        }

        // Cargos
        message.getGuildOrNull()?.let { guild ->

            message.mentionedRoleIds.forEach { id ->

                guild.getRoleOrNull(id)?.let { role ->

                    val roleName = "@${role.name}"

                    content = content.replace(
                        "<@&$id>",
                        roleName
                    )

                    notes += "Cargo mencionado: $roleName."
                }

            }

        }

        // Emojis
        content = Regex("<a?:([A-Za-z0-9_]+):\\d+>")
            .replace(content) {

                val emoji = ":${it.groupValues[1]}:"

                notes += "Emoji utilizado: $emoji."

                emoji
            }

        return ParsedMessage(
            content = content,
            notes = notes
        )
    }

    private suspend fun getDisplayName(
        message: Message
    ): String {

        val member = try {
            message.getAuthorAsMember()
        } catch (_: Exception) {
            null
        }

        return member?.effectiveName
            ?: message.author?.globalName
            ?: message.author?.username
            ?: "Unknown"
    }

    private data class ParsedMessage(
        val content: String,
        val notes: List<String>
    )
}
