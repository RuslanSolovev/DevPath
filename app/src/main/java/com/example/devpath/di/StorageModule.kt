package com.example.devpath.di

import android.content.Context
import com.example.devpath.BuildConfig
import com.example.devpath.data.storage.YandexStorageClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object StorageModule {

    @Provides
    @Singleton
    fun provideYandexStorageClient(
        @ApplicationContext context: Context
    ): YandexStorageClient {
        return YandexStorageClient(
            context = context,
            accessKey = BuildConfig.YC_ACCESS_KEY,
            secretKey = BuildConfig.YC_SECRET_KEY,
            bucketName = BuildConfig.YC_BUCKET_NAME
        )
    }
}