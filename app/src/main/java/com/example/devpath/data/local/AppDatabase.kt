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

        // Миграция с версии 2 на 3
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {

                // 1. Создаем таблицу chat_sessions с ПРАВИЛЬНЫМИ типами
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

                // 2. Создаем таблицу chat_messages
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

                // 3. ✅ ВАЖНО: Создаем индекс для userId (как ожидает Room)
                database.execSQL("""
                    CREATE INDEX IF NOT EXISTS `index_chat_sessions_userId` 
                    ON `chat_sessions` (`userId`)
                """)

                // 4. ✅ ВАЖНО: Создаем индекс для sessionId
                database.execSQL("""
                    CREATE INDEX IF NOT EXISTS `index_chat_messages_sessionId` 
                    ON `chat_messages` (`sessionId`)
                """)
            }
        }

        // Миграция с версии 3 на 4 - создаем таблицу test_attempts
        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Создаем таблицу test_attempts без индекса
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

                // НЕ создаем индекс здесь - Room создаст его сам при проверке
                // Индекс будет создан автоматически, если он определен в аннотациях @Entity
            }
        }

        // Если нужно добавить индекс позже, используйте отдельную миграцию
        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE INDEX IF NOT EXISTS `index_test_attempts_userId` 
                    ON `test_attempts` (`userId`)
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
                    .addMigrations(MIGRATION_2_3, MIGRATION_3_4)
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}