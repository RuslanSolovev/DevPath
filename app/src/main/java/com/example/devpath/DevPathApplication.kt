package com.example.devpath

import android.app.Application
import androidx.lifecycle.ProcessLifecycleOwner
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class DevPathApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Отслеживаем жизненный цикл приложения
        ProcessLifecycleOwner.get().lifecycle.addObserver(AppLifecycleObserver())

        println("DEBUG: DevPathApplication создан")
    }

    class AppLifecycleObserver : androidx.lifecycle.LifecycleObserver {

        @androidx.lifecycle.OnLifecycleEvent(androidx.lifecycle.Lifecycle.Event.ON_CREATE)
        fun onCreate() {
            println("DEBUG: LifecycleObserver - Приложение создано")
        }

        @androidx.lifecycle.OnLifecycleEvent(androidx.lifecycle.Lifecycle.Event.ON_START)
        fun onEnterForeground() {
            println("DEBUG: LifecycleObserver - Приложение в foreground (открыто)")
        }

        @androidx.lifecycle.OnLifecycleEvent(androidx.lifecycle.Lifecycle.Event.ON_STOP)
        fun onEnterBackground() {
            println("DEBUG: LifecycleObserver - Приложение в background (свернуто)")
        }

        @androidx.lifecycle.OnLifecycleEvent(androidx.lifecycle.Lifecycle.Event.ON_DESTROY)
        fun onDestroy() {
            println("DEBUG: LifecycleObserver - Приложение уничтожается")
        }
    }
}