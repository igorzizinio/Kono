package me.igorunderplayer.kono.commands.text.testing

import dev.kord.core.behavior.interaction.respondPublic
import dev.kord.core.behavior.reply
import dev.kord.core.event.message.MessageCreateEvent
import kotlinx.coroutines.flow.toList
import me.igorunderplayer.kono.commands.BaseCommand
import me.igorunderplayer.kono.utils.getMentionedUser

class DestinoTextCommand: BaseCommand(
    name = "destino",
    description = "descubra o seu destino, ou de outra pessoa..."
) {
    override suspend fun run(event: MessageCreateEvent, args: Array<String>) {
        val user = getMentionedUser(event.message, args)

        if (user == null) {
            event.message.reply {
                content = "ue?"
            }

            return
        }

        val guild = event.message.getGuildOrNull()

        if (guild == null) {
            event.message.reply {
                content = "vc so pode usar isso num servidor"
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

            val randomMember = guild.members.toList().random()
            event.message.reply {
                content = "${user.mention} seu destino é... \n ... ${destino.replace("{randomMember}", randomMember.mention)}"
            }

        } else {
            event.message.reply {
                content = "${user.mention} seu destino é... \n ... $destino"
            }
        }
    }
}