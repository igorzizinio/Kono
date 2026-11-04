package me.igorunderplayer.kono.server.routes

import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Routing
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import me.igorunderplayer.kono.data.dto.CardSummaryDTO
import me.igorunderplayer.kono.data.dto.CreateMatchRequest
import me.igorunderplayer.kono.data.dto.MatchCreatedDTO
import me.igorunderplayer.kono.data.dto.MatchActionRequest
import me.igorunderplayer.kono.services.CardMatchManager
import me.igorunderplayer.kono.services.CardService

fun Routing.konoRoutes(
    cardService: CardService,
    cardMatchManager: CardMatchManager
) {
    route("/kono") {
        get("cards") {
            val cards = cardService.getCardDefinitions().map { card ->
                CardSummaryDTO(
                    id = card.id,
                    name = card.name,
                    description = card.description,
                    type = card.type.name,
                    rarity = card.rarity.name,
                    attackCost = card.attackCost,
                    apCost = card.apCost,
                    baseStats = card.baseStats.mapKeys { it.key.name },
                    tags = card.tags.sorted()
                )
            }
            call.respond(cards)
        }

        route("matches") {
            post {
                try {
                    val request = call.receive<CreateMatchRequest>()
                    call.respond(MatchCreatedDTO(cardMatchManager.create(request)))
                } catch (exception: IllegalArgumentException) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        mapOf("error" to (exception.message ?: "Invalid match request"))
                    )
                }
            }

            get("{id}") {
                val id = call.parameters["id"]
                    ?: return@get call.respond(HttpStatusCode.BadRequest)
                val match = cardMatchManager.get(id)
                    ?: return@get call.respond(HttpStatusCode.NotFound)
                call.respond(match)
            }

            post("{id}/actions") {
                val id = call.parameters["id"]
                    ?: return@post call.respond(HttpStatusCode.BadRequest)
                try {
                    val result = cardMatchManager.execute(
                        id,
                        call.receive<MatchActionRequest>()
                    ) ?: return@post call.respond(HttpStatusCode.NotFound)
                    call.respond(result)
                } catch (exception: IllegalArgumentException) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        mapOf("error" to (exception.message ?: "Invalid action request"))
                    )
                }
            }

            delete("{id}") {
                val id = call.parameters["id"]
                    ?: return@delete call.respond(HttpStatusCode.BadRequest)
                if (cardMatchManager.remove(id)) {
                    call.respond(HttpStatusCode.NoContent)
                } else {
                    call.respond(HttpStatusCode.NotFound)
                }
            }
        }
    }
}
