package com.example.devpath

import android.app.Application
import androidx.lifecycle.ProcessLifecycleOwner
import com.example.devpath.data.repository.YdbRepository
import com.yandex.mapkit.MapKitFactory
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class DevPathApplication : Application() {

    @Inject
    lateinit var ydbRepository: YdbRepository

    override fun onCreate() {
        super.onCreate()

        // 🔑 ИНИЦИАЛИЗАЦИЯ YANDEX MAPKIT
        MapKitFactory.setApiKey("6b7f7e6b-d322-42b2-8471-d8aecc6570d1")
        MapKitFactory.initialize(applicationContext)

        // Отслеживаем жизненный цикл приложения
        ProcessLifecycleOwner.get().lifecycle.addObserver(AppLifecycleObserver())

        // ✅ ИНИЦИАЛИЗАЦИЯ БАЗЫ ДАННЫХ YDB
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val success = ydbRepository.initDatabase()
                if (success) {
                    println("DEBUG: YDB база данных готова к работе")
                } else {
                    println("DEBUG: ⚠️ Не удалось инициализировать базу данных YDB")
                }
            } catch (e: Exception) {
                println("DEBUG: ❌ Ошибка инициализации YDB: ${e.message}")
            }
        }

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