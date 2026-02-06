package com.example.devpath.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.devpath.data.local.dao.UserProgressDao
import com.example.devpath.data.local.entity.UserProgressEntity

@Database(
    entities = [UserProgressEntity::class],
    version = 2, // ⬅️ УВЕЛИЧИЛИ С 1 НА 2
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userProgressDao(): UserProgressDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // Миграция с версии 1 на 2
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Если не меняли структуру таблицы, оставляем пустым
                // Если меняли, добавьте SQL запросы:
                // database.execSQL("ALTER TABLE user_progress ADD COLUMN new_column TEXT")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "devpath_database"
                )
                    // Выберите один из вариантов:

                    // Вариант 1: Деструктивная миграция (данные удалятся)
                    .fallbackToDestructiveMigration()

                    // ИЛИ Вариант 2: Сохранить данные через миграцию
                    // .addMigrations(MIGRATION_1_2)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}