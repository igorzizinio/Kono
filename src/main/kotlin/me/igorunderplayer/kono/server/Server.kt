package me.igorunderplayer.kono.server

import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.http.content.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
import me.igorunderplayer.kono.data.dto.CreateMessageRequest
import me.igorunderplayer.kono.data.dto.MessageResponse
import me.igorunderplayer.kono.server.routes.konoRoutes
import me.igorunderplayer.kono.services.RandomMessageService

class Server(
    val randomMessageService: RandomMessageService
) {
    suspend fun start() {
        embeddedServer(Netty, 8080, "0.0.0.0") {
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                })
            }
            routing {

                staticResources(
                    remotePath = "/public",
                    basePackage = "public"
                )

                // servir HTML (views)
                staticResources(
                    remotePath = "/",
                    basePackage = "views",
                    index = "index.html"
                )

                konoRoutes()

                get("/random-messages/api") {
                    val msg = randomMessageService.getRandomMessage()

                    if (msg == null) {
                        call.respond(
                            HttpStatusCode.OK,
                            MessageResponse("No messages yet!")
                        )
                    } else {
                        call.respond(
                            HttpStatusCode.OK,
                            MessageResponse(msg)
                        )
                    }
                }

                post("/random-messages/api/create") {
                    val body = call.receive<CreateMessageRequest>()

                    if (body.message.isBlank()) {
                        call.respond(
                            HttpStatusCode.BadRequest,
                            mapOf("error" to "Invalid message")
                        )
                        return@post
                    }

                    println("got request for new message")

                    randomMessageService.createRandomMessage(body.message)
                    call.respond(HttpStatusCode.OK)
                }
            }
        }.startSuspend(wait = true)
    }
}
