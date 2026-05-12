package com.example.devpath.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.devpath.data.local.dao.ChatSessionDao
import com.example.devpath.data.local.dao.TestAttemptDao
import com.example.devpath.data.local.dao.UserProgressDao
import com.example.devpath.data.local.entity.TestAttemptEntity
import com.example.devpath.data.local.entity.UserProgressEntity
import com.example.devpath.domain.models.ChatSession
import com.example.devpath.domain.models.StoredMessage

@Database(
    entities = [
        UserProgressEntity::class,
        ChatSession::class,
        StoredMessage::class,
        TestAttemptEntity::class
    ],
    version = 4,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userProgressDao(): UserProgressDao
    abstract fun chatSessionDao(): ChatSessionDao
    abstract fun testAttemptDao(): TestAttemptDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // ✅ Миграция 1→2: создание user_progress
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `user_progress` (
                        `userId` TEXT NOT NULL PRIMARY KEY,
                        `displayName` TEXT NOT NULL,
                        `completedLessons` TEXT NOT NULL,
                        `completedPracticeTasks` TEXT NOT NULL,
                        `quizResults` TEXT NOT NULL,
                        `favoriteInterviewQuestions` TEXT NOT NULL,
                        `totalXP` INTEGER NOT NULL,
                        `level` INTEGER NOT NULL,
                        `generalTestHistory` TEXT NOT NULL
                    )
                """)
            }
        }

        // Миграция 2→3: создание chat_sessions и chat_messages
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `chat_sessions` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `title` TEXT NOT NULL,
                        `preview` TEXT NOT NULL,
                        `timestamp` INTEGER NOT NULL,
                        `messageCount` INTEGER NOT NULL,
                        `userId` TEXT NOT NULL
                    )
                """)

                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `chat_messages` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `sessionId` INTEGER NOT NULL,
                        `text` TEXT NOT NULL,
                        `isUser` INTEGER NOT NULL,
                        `timestamp` INTEGER NOT NULL,
                        `orderIndex` INTEGER NOT NULL,
                        FOREIGN KEY(`sessionId`) REFERENCES `chat_sessions`(`id`) ON DELETE CASCADE
                    )
                """)

                database.execSQL("""
                    CREATE INDEX IF NOT EXISTS `index_chat_sessions_userId` 
                    ON `chat_sessions` (`userId`)
                """)

                database.execSQL("""
                    CREATE INDEX IF NOT EXISTS `index_chat_messages_sessionId` 
                    ON `chat_messages` (`sessionId`)
                """)
            }
        }

        // Миграция 3→4: создание test_attempts
        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `test_attempts` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `userId` TEXT NOT NULL,
                        `timestamp` INTEGER NOT NULL,
                        `totalQuestions` INTEGER NOT NULL,
                        `correctAnswers` INTEGER NOT NULL,
                        `detailsJson` TEXT NOT NULL
                    )
                """)
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "devpath_database"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                    .fallbackToDestructiveMigration() // На крайний случай — пересоздаст БД
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}