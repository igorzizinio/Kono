package me.igorunderplayer.kono

import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.kotlin.client.coroutine.MongoClient
import com.mongodb.kotlin.client.coroutine.MongoCollection
import me.igorunderplayer.kono.entities.UserDB

class DatabaseP {

    lateinit var usersCollection: MongoCollection<UserDB>


    private val connectionString = ConnectionString(Config.mongoUri)

    private val settings = MongoClientSettings.builder()
        .applyConnectionString(connectionString)
        .retryWrites(true)
        .build()

    private val client = MongoClient.create(settings)
    private val db = client.getDatabase("kono-bot")

    fun start() {
        usersCollection = db.getCollection<UserDB>("users")

    }
}