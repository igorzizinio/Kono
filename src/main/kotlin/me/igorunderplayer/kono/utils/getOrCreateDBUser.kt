package me.igorunderplayer.kono.utils

import com.mongodb.client.model.Filters
import com.mongodb.kotlin.client.coroutine.MongoCollection
import kotlinx.coroutines.flow.singleOrNull
import me.igorunderplayer.kono.entities.UserDB

suspend fun getOrCreateDBUser(usersCollection: MongoCollection<UserDB>, id: String): UserDB? {
    val filter = Filters.eq(UserDB::discordId.name, id)
    var dbUser = usersCollection.find(filter).singleOrNull()

    if (dbUser == null) {
        usersCollection.insertOne(
            UserDB(
                id,
                0
            )
        )

        val newFilter = Filters.eq(UserDB::discordId.name, id)
        dbUser = usersCollection.find(newFilter).singleOrNull()
    }

    return dbUser
}