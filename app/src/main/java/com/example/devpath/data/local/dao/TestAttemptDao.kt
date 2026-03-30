package com.example.devpath.data.local.dao

import androidx.room.*
import com.example.devpath.data.local.entity.TestAttemptEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TestAttemptDao {
    @Query("SELECT * FROM test_attempts WHERE userId = :userId ORDER BY timestamp DESC")
    fun getAllAttempts(userId: String): Flow<List<TestAttemptEntity>>

    @Query("SELECT * FROM test_attempts WHERE userId = :userId ORDER BY timestamp DESC")
    suspend fun getAttemptsByUserId(userId: String): List<TestAttemptEntity>

    @Query("SELECT * FROM test_attempts WHERE id = :id")
    suspend fun getAttemptById(id: Long): TestAttemptEntity?

    @Insert
    suspend fun insertAttempt(attempt: TestAttemptEntity): Long
}