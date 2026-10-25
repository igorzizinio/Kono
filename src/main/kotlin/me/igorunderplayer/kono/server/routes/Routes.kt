package me.igorunderplayer.kono.server.routes

import io.ktor.server.response.respond
import io.ktor.server.routing.Routing
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import me.igorunderplayer.kono.domain.card.CardCatalog

fun Routing.konoRoutes() {
    route("/kono") {
        get("cards") {
            val cards = CardCatalog.all
            call.respond(cards)
        }
    }
}
