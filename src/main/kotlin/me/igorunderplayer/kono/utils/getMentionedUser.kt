package me.igorunderplayer.kono.utils

import dev.kord.common.entity.Snowflake
import dev.kord.core.entity.Message
import dev.kord.core.entity.User
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.firstOrNull

suspend fun getMentionedUser(message: Message, args: Array<String>): User? {
    val mentionRegExp = Regex("^<@!?${message.kord.selfId}>$")
    val countSelf = args.count { mentionRegExp.containsMatchIn(it) } > 0

    val user = if (args.isNotEmpty()) {
        message.mentionedUsers.filter { it.id != message.kord.selfId || countSelf }
            .firstOrNull()  ?: message.kord.getUser(
                 Snowflake(args[0].toULongOrNull() ?: ULong.MIN_VALUE)
            )?.asUserOrNull()
    } else {
        null
    }

    return user
}