// data/local/AppDatabase.kt
package com.example.devpath.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.devpath.data.local.dao.ChatSessionDao
import com.example.devpath.data.local.dao.UserProgressDao
import com.example.devpath.data.local.entity.UserProgressEntity
import com.example.devpath.domain.models.ChatSession
import com.example.devpath.domain.models.StoredMessage

@Database(
    entities = [
        UserProgressEntity::class,
        ChatSession::class,
        StoredMessage::class
    ],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userProgressDao(): UserProgressDao
    abstract fun chatSessionDao(): ChatSessionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // ✅ ИСПРАВЛЕННАЯ миграция с версии 2 на 3
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

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "devpath_database"
                )
                    .addMigrations(MIGRATION_2_3)
                    .fallbackToDestructiveMigration() // На случай если что-то пойдет не так
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}