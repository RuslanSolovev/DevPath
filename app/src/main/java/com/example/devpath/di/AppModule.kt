package com.example.devpath.di

import android.content.Context
import com.example.devpath.api.GigaChatService
import com.example.devpath.api.speech.SaluteSpeechService
import com.example.devpath.data.local.AppDatabase
import com.example.devpath.data.repository.ProgressRepository
import com.example.devpath.data.repository.ThemeRepository
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getDatabase(context)
    }

    @Provides
    @Singleton
    fun provideThemeRepository(@ApplicationContext context: Context): ThemeRepository {
        return ThemeRepository(context)
    }

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


    @Provides
    @Singleton
    fun provideProgressRepository(
        firestore: FirebaseFirestore,
        database: AppDatabase
    ): ProgressRepository {
        return ProgressRepository(
            db = firestore,

            localDb = database
        )
    }

}