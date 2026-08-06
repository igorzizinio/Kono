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

# Informações adicionadas pelo sistema

Algumas mensagens podem conter linhas começando com:

[Sistema: ...]

Essas linhas NÃO foram escritas pelos usuários.

Elas existem apenas para ajudá-lo a interpretar elementos do Discord, como:

- quem foi mencionado;
- canais mencionados;
- cargos mencionados;
- emojis personalizados.

Exemplos:

[Sistema: Kono foi mencionada.]
[Sistema: Usuários mencionados: @Igor.]
[Sistema: Canal mencionado: #geral.]
[Sistema: Cargo mencionado: @Moderador.]
[Sistema: Emoji utilizado: :poppy:.]

Essas informações servem apenas como contexto.

Nunca copie linhas começando com "[Sistema:" para sua resposta.
Nunca mencione que recebeu essas informações.

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

            if (it.author?.id == message.kord.selfId) {

                // Mensagens anteriores da própria Kono.
                // O role=assistant já informa ao modelo quem escreveu.
                conversation += ChatMessage(
                    role = "assistant",
                    content = parseDiscordContent(it).content
                )

            } else {

                conversation += ChatMessage(
                    role = "user",
                    content = buildMessage(it)
                )

            }

        }

        conversation += ChatMessage(
            role = "user",
            content = buildMessage(message)
        )
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

            appendLine("$author:")
            appendLine(parsed.content)

            if (parsed.notes.isNotEmpty()) {

                appendLine()

                parsed.notes
                    .distinct()
                    .forEach {
                        appendLine("[Sistema: $it]")
                    }

            }
        }
    }

    private suspend fun parseDiscordContent(
        message: Message
    ): ParsedMessage {

        var content = message.content

        val notes = mutableListOf<String>()

        message.referencedMessage?.let {

            val author = it.author?.username ?: "Unknown"

            val preview = it.content
                .replace("\n", " ")
                .take(80)

            notes += "Esta mensagem responde à mensagem de @$author: $preview"
        }

        val mentionedUsers = mutableListOf<String>()

        message.mentionedUsers.toList().forEach { user ->

            val name =
                if (user.id == message.kord.selfId)
                    "Kono"
                else
                    user.username

            content = content
                .replace("<@${user.id}>", "@$name")
                .replace("<@!${user.id}>", "@$name")

            mentionedUsers += "@$name"

            if (user.id == message.kord.selfId) {
                notes += "Kono foi mencionada."
            }
        }

        if (mentionedUsers.isNotEmpty()) {
            notes += "Usuários mencionados: ${mentionedUsers.joinToString(", ")}."
        }

        val channels = mutableListOf<String>()

        message.mentionedChannelIds.forEach { id ->

            message.kord.getChannel(id)?.let {

                val name = "#${it.data.name.value}"

                channels += name

                content = content.replace(
                    "<#$id>",
                    name
                )

            }

        }

        if (channels.isNotEmpty()) {
            notes += "Canais mencionados: ${channels.joinToString(", ")}."
        }

        message.getGuildOrNull()?.let { guild ->

            val roles = mutableListOf<String>()

            message.mentionedRoleIds.forEach { id ->

                guild.getRoleOrNull(id)?.let {

                    val role = "@${it.name}"

                    roles += role

                    content = content.replace(
                        "<@&$id>",
                        role
                    )

                }

            }

            if (roles.isNotEmpty()) {
                notes += "Cargos mencionados: ${roles.joinToString(", ")}."
            }

        }

        val emojis = mutableListOf<String>()

        content = Regex("<a?:([A-Za-z0-9_]+):\\d+>")
            .replace(content) {

                val emoji = ":${it.groupValues[1]}:"

                emojis += emoji

                emoji
            }

        if (emojis.isNotEmpty()) {
            notes += "Emojis: ${emojis.distinct().joinToString(", ")}."
        }

        if (message.attachments.isNotEmpty()) {

            val images = message.attachments.count {
                it.contentType?.startsWith("image/") == true
            }

            if (images > 0)
                notes += "Há $images imagem(ns) anexada(s)."

            val files = message.attachments.size - images

            if (files > 0)
                notes += "Há $files arquivo(s) anexado(s)."
        }

        if (message.embeds.isNotEmpty()) {
            notes += "A mensagem contém embed."
        }

        if (content.length > 800) {
            content =
                content.take(800) +
                        "\n\n[Sistema: Mensagem truncada.]"
        }

        return ParsedMessage(
            content,
            notes
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
