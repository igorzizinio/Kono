package me.igorunderplayer.kono.services

import me.igorunderplayer.kono.data.DatabaseManager

class ServiceManager(private val databaseManager: DatabaseManager) {
    val userService = UserService(databaseManager.userRepository)
}