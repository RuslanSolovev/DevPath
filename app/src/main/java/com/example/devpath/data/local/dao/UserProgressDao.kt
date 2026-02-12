package com.example.devpath.data.local.dao

import androidx.room.*
import com.example.devpath.data.local.entity.UserProgressEntity

@Dao
interface UserProgressDao {
    @Query("SELECT * FROM user_progress WHERE userId = :userId")
    suspend fun getProgress(userId: String): UserProgressEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProgress(progress: UserProgressEntity)

    @Query("DELETE FROM user_progress WHERE userId = :userId")
    suspend fun deleteProgress(userId: String)
}