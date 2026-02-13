// data/local/dao/ChatSessionDao.kt
package com.example.devpath.data.local.dao

import androidx.room.*
import com.example.devpath.domain.models.ChatSession
import com.example.devpath.domain.models.StoredMessage
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatSessionDao {

    // Для Flow (реактивное обновление)
    @Query("SELECT * FROM chat_sessions WHERE userId = :userId ORDER BY timestamp DESC")
    fun getAllSessions(userId: String): Flow<List<ChatSession>>

    // Для suspend (однократная загрузка)
    @Query("SELECT * FROM chat_sessions WHERE userId = :userId ORDER BY timestamp DESC")
    suspend fun getAllSessionsSync(userId: String): List<ChatSession>

    @Query("SELECT * FROM chat_sessions WHERE id = :sessionId")
    suspend fun getSession(sessionId: Long): ChatSession?

    @Insert
    suspend fun insertSession(session: ChatSession): Long

    @Update
    suspend fun updateSession(session: ChatSession)

    @Delete
    suspend fun deleteSession(session: ChatSession)

    @Query("DELETE FROM chat_sessions WHERE userId = :userId")
    suspend fun deleteAllSessions(userId: String)

    @Query("DELETE FROM chat_sessions WHERE id = :sessionId")
    suspend fun deleteSessionById(sessionId: Long)

    @Query("SELECT * FROM chat_messages WHERE sessionId = :sessionId ORDER BY orderIndex ASC")
    fun getMessages(sessionId: Long): Flow<List<StoredMessage>>

    @Insert
    suspend fun insertMessages(vararg messages: StoredMessage)

    @Query("DELETE FROM chat_messages WHERE sessionId = :sessionId")
    suspend fun deleteMessages(sessionId: Long)
}