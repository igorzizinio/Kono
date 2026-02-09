package me.igorunderplayer.kono.data.dto

import kotlinx.serialization.Serializable


@Serializable
data class CreateMessageRequest(
    val message: String
)

@Serializable
data class MessageResponse(
    val msg: String
)