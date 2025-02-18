package me.igorunderplayer.kono.data

import me.igorunderplayer.kono.data.repositories.UserRepository
import org.ktorm.database.Database

class DatabaseManager {
    lateinit var db: Database
        private set

    lateinit var userRepository: UserRepository
        private set


    fun start(databaseUrl: String, user: String, password: String) {
        db = Database.connect(databaseUrl, user = user, password =  password)

        userRepository = UserRepository(db)

    }
}