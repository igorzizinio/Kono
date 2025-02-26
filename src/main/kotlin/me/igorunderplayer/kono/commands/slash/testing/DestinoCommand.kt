package me.igorunderplayer.kono.commands.slash.testing

import dev.kord.core.Kord
import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.behavior.interaction.respondPublic
import dev.kord.core.entity.application.GlobalChatInputCommand
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import dev.kord.rest.builder.interaction.user
import kotlinx.coroutines.flow.toList
import me.igorunderplayer.kono.commands.KonoSlashCommand

class DestinoCommand: KonoSlashCommand {
    override val name = "destino"
    override val description = "descubra seu destino..."

    override suspend fun setup(kord: Kord): GlobalChatInputCommand {
        return kord.createGlobalChatInputCommand(
            this.name,
            this.description
        ) {
            user("user", "selecione um usuario para descobrir o destino...") {
                required = false
            }
        }
    }

    override suspend fun run(event: ChatInputCommandInteractionCreateEvent) {
        val user = event.interaction.command.users["user"] ?: event.interaction.user

        val guildId = event.interaction.invokedCommandGuildId

        if (guildId == null) {
            event.interaction.respondEphemeral {
                content = "vc so pode executar isso em um servidor"
            }
            return
        }

        val destinos = arrayOf(
            "casar comigo",
            "me dar um beijo",
            "dar a bunda para {randomMember}",
            "banir {randomMember}",
            "ser banido",
            "ser promovido",
            "virar adm",
            "perder adm",
            "ser rebaixado",
            "dar uma mamada em {randomMember}",
            "me adicionar no seu servidor",
            "ficar famoso",
            "me dar adm",
            "\uD83D\uDE33 me dar uma mamada",
            "virar femboy",
            "fazer um filme porno",
            "virar artista hentai",
            "me passar seu numero",
            "me dar nitro",
            "dar nitro para {randomMember}",
            "dar nitro para meu criador",
            "assistir hentai comigo",
            "ficar milionario",
            "tropeçar",
            "ganhar um pc gamer",
            "sair do armario",
            "ficar pobre",
            "morrer",
            "transcender",
            "dar um tapa em {randomMember}",
            "receber um tapa de {randomMember}",
            "ficar careca",
            "ficar cabeludo",
            "beber agua",
            "mandar nsfw no #geral",
            "sair do servidor",
            "virar gay",
            "virar mulher",
            "virar homem",
            "virar trans",
            "ganhar na loteria",
            "ficar burro",
            "decifrar meu enigma",
            "participar de um rpg",
            "cair da escada",
            "cair",
            "virar deus de um novo mundo",
            "tropecar numa pedra",
            "nenhum",
            "achar um caderno estranho",
            "virar otaku",
            "virar trap",
            "virar emo",
            "comer a bunda de {randomMember}",
            "criar um servidor e me adicionar",
            "ser travado no zap",
            "travar o zap de {randomMember}",
            "ser travado no zap por {randomMember}",
            "ganhar o numero de {randomMember}",
            "\uD83D\uDE33 fazer websexu ",
            "\uD83D\uDE33 fazer websexu com {randomMember}"
        )


        val destino = destinos.random()

        if (destino.contains("{randomMember}")) {

            val randomMember = event.kord.getGuild(guildId).members.toList().random()
            event.interaction.respondPublic {
                content = "${user.mention} seu destino é... \n ... ${destino.replace("{randommember}", randomMember.mention)}"
            }

        } else {
            event.interaction.respondPublic {
                content = "${user.mention} seu destino é... \n ... $destino"
            }
        }



    }

}