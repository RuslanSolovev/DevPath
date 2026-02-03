package com.example.devpath

import android.app.Application
import com.example.devpath.data.repository.ThemeRepository
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class DevPathApplication : Application() {
    lateinit var themeRepository: ThemeRepository

    override fun onCreate() {
        super.onCreate()
        themeRepository = ThemeRepository(this)
    }
}