package com.example.devpath.di

import android.content.Context
import com.example.devpath.api.GigaChatService
import com.example.devpath.api.speech.SaluteSpeechService
import com.example.devpath.data.local.AppDatabase
import com.example.devpath.data.local.dao.TestAttemptDao
import com.example.devpath.data.local.dao.UserProgressDao
import com.example.devpath.data.repository.ChatRepository
import com.example.devpath.data.repository.ProgressRepository
import com.example.devpath.data.repository.ThemeRepository
import com.example.devpath.data.storage.YandexStorageClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    // ==================== БАЗА ДАННЫХ ====================

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getDatabase(context)
    }

    @Provides
    fun provideUserProgressDao(database: AppDatabase): UserProgressDao {
        return database.userProgressDao()
    }

    @Provides
    fun provideTestAttemptDao(database: AppDatabase): TestAttemptDao {
        return database.testAttemptDao()
    }

    // ==================== РЕПОЗИТОРИИ ====================

    @Provides
    @Singleton
    fun provideProgressRepository(
        database: AppDatabase  // ← Только локальная БД, без Firebase!
    ): ProgressRepository {
        return ProgressRepository(
            localDb = database
        )
    }

    @Provides
    @Singleton
    fun provideThemeRepository(@ApplicationContext context: Context): ThemeRepository {
        return ThemeRepository(context)
    }

    @Provides
    @Singleton
    fun provideChatRepository(
        yandexStorageClient: YandexStorageClient
    ): ChatRepository {
        return ChatRepository(yandexStorageClient)
    }

    // ==================== API СЕРВИСЫ ====================

    @Provides
    @Singleton
    fun provideGigaChatService(): GigaChatService {
        return GigaChatService()
    }

    @Provides
    @Singleton
    fun provideSaluteSpeechService(): SaluteSpeechService {
        return SaluteSpeechService()
    }

    // ==================== ХРАНИЛИЩЕ ====================

    @Provides
    @Singleton
    fun provideYandexStorageClient(@ApplicationContext context: Context): YandexStorageClient {
        return YandexStorageClient(
            context = context,
            accessKey = com.example.devpath.utils.Config.YC_ACCESS_KEY,
            secretKey = com.example.devpath.utils.Config.YC_SECRET_KEY,
            bucketName = com.example.devpath.utils.Config.YC_BUCKET_NAME
        )
    }
}