package me.igorunderplayer.kono

import com.mongodb.client.model.Filters
import dev.kord.common.entity.Snowflake
import freemarker.cache.ClassTemplateLoader
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.freemarker.*
import io.ktor.server.http.content.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import me.igorunderplayer.kono.entities.UserDB

class Server {
    suspend fun start() {
        val port = Config.port

        embeddedServer(Netty, port) {
            install(FreeMarker) {
                templateLoader = ClassTemplateLoader(this::class.java.classLoader, "templates")
            }

            routing {

                staticResources("/static", "static")

                route("/status") {
                    get {
                        call.respondText("alive!")
                    }
                }

                route ("/user") {
                    get ("/{id}") {
                        val userId = call.parameters["id"]

                        val filter = Filters.eq(UserDB::discordId.name, userId)
                        val user = Kono.db.usersCollection.find(filter)
                            ?: return@get call.respondText("User not found!", status = HttpStatusCode.NotFound)

                        val discordUser = Kono.kord.getUser(Snowflake(userId!!))

                        call.respondTemplate("user.ftl", mapOf("user" to user, "discord" to discordUser))
                    }
                }

                get {
                    call.respondTemplate("index.ftl", mapOf("bot" to Kono.kord.getSelf()))
                }
            }
        }.start(wait = true)
    }
}