package com.example.devpath

import android.app.Application
import androidx.lifecycle.ProcessLifecycleOwner
import com.yandex.mapkit.MapKitFactory
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class DevPathApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // 🔑 ИНИЦИАЛИЗАЦИЯ YANDEX MAPKIT
        // ВАЖНО: setApiKey() должен быть вызван ДО initialize()
        // Используйте ваш реальный API-ключ из кабинета разработчика
        MapKitFactory.setApiKey("6b7f7e6b-d322-42b2-8471-d8aecc6570d1")
        MapKitFactory.initialize(applicationContext)

        // Отслеживаем жизненный цикл приложения
        ProcessLifecycleOwner.get().lifecycle.addObserver(AppLifecycleObserver())

        println("DEBUG: DevPathApplication создан, MapKit инициализирован")
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