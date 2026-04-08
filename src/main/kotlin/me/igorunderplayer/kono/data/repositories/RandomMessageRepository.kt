package me.igorunderplayer.kono.data.repositories

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.igorunderplayer.kono.data.DatabaseManager
import me.igorunderplayer.kono.data.entities.RandomMessages
import org.ktorm.database.Database
import org.ktorm.dsl.*
import org.ktorm.entity.find
import org.ktorm.entity.sequenceOf
import org.ktorm.expression.FunctionExpression
import org.ktorm.schema.DoubleSqlType

fun random() = FunctionExpression(
    functionName = "random",
    arguments = listOf(),
    sqlType = DoubleSqlType
)


class RandomMessageRepository(private val databaseManager: DatabaseManager) {
    private val database: Database
        get() = databaseManager.db

    suspend fun createRandomMessage(content: String) = withContext(Dispatchers.IO) {
        val generatedId = database.insertAndGenerateKey(
            RandomMessages
        ) {
            set(it.content, content)
        }

        return@withContext generatedId as Int
    }

    suspend fun getRandomMessageById(id: Int): String? = withContext(Dispatchers.IO) {
        database.sequenceOf(RandomMessages).find { it.id eq id }?.content
    }

    suspend fun getRandomMessage(): String? = withContext(Dispatchers.IO) {
        database
            .from(RandomMessages)
            .select()
            .orderBy(random().asc())
            .limit(1)
            .map { it[RandomMessages.content] }
            .firstOrNull()
    }
}
