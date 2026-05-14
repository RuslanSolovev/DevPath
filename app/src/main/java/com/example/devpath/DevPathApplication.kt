package com.example.devpath

import android.app.Application
import android.content.Context
import androidx.hilt.work.HiltWorkerFactory
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.work.Configuration
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.devpath.data.repository.YdbRepository
import com.example.devpath.utils.SessionManager
import com.example.devpath.workers.StepSyncWorker
import com.yandex.mapkit.MapKitFactory
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
class DevPathApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var ydbRepository: YdbRepository

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override fun onCreate() {
        SessionManager.init(this)
        super.onCreate()

        // 🔑 ИНИЦИАЛИЗАЦИЯ YANDEX MAPKIT
        MapKitFactory.setApiKey("6b7f7e6b-d322-42b2-8471-d8aecc6570d1")
        MapKitFactory.initialize(applicationContext)

        // Отслеживаем жизненный цикл приложения
        ProcessLifecycleOwner.get().lifecycle.addObserver(AppLifecycleObserver())

        // ✅ ВСЁ В ОДНОЙ КОРУТИНЕ ДЛЯ ПРАВИЛЬНОГО ПОРЯДКА
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // 1. Инициализация БД
                val usersOk = ydbRepository.initDatabase()
                val chatsOk = ydbRepository.initChatTables()

                if (usersOk && chatsOk) {
                    println("DEBUG: YDB база данных готова к работе")
                } else {
                    println("DEBUG: ⚠️ Частичная инициализация YDB")
                }

                // 2. Ждём полной инициализации
                delay(3000)

                // 3. Запускаем периодическую синхронизацию
                scheduleStepSyncWork()

                // 4. Немедленная синхронизация если есть шаги
                syncStepsImmediately()

            } catch (e: Exception) {
                println("DEBUG: ❌ Ошибка инициализации: ${e.message}")
            }
        }

        println("DEBUG: DevPathApplication создан, MapKit инициализирован")
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .setMinimumLoggingLevel(android.util.Log.DEBUG)
            .build()

    private fun scheduleStepSyncWork() {
        try {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val syncRequest = PeriodicWorkRequestBuilder<StepSyncWorker>(
                15, TimeUnit.MINUTES
            )
                .setConstraints(constraints)
                .addTag("step_sync_work")
                .build()

            WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "step_sync_work",
                ExistingPeriodicWorkPolicy.UPDATE,
                syncRequest
            )

            println("DEBUG: WorkManager - периодическая синхронизация запланирована (15 мин)")
        } catch (e: Exception) {
            println("DEBUG: WorkManager - ошибка планирования: ${e.message}")
        }
    }

    private fun syncStepsImmediately() {
        try {
            val stepPrefs = getSharedPreferences("step_counter", Context.MODE_PRIVATE)
            val todaySteps = stepPrefs.getInt("today_steps", 0)
            if (todaySteps > 0) {
                println("DEBUG: Найдены шаги: $todaySteps, запускаю синхронизацию")
                val request = OneTimeWorkRequestBuilder<StepSyncWorker>()
                    .setConstraints(
                        Constraints.Builder()
                            .setRequiredNetworkType(NetworkType.CONNECTED)
                            .build()
                    )
                    .addTag("step_sync_immediate")
                    .build()
                WorkManager.getInstance(this).enqueue(request)
            } else {
                println("DEBUG: Нет шагов для синхронизации")
            }
        } catch (e: Exception) {
            println("DEBUG: Ошибка синхронизации: ${e.message}")
        }
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