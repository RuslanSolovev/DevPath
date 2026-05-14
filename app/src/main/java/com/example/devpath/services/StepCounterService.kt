package com.example.devpath.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.example.devpath.MainActivity
import com.example.devpath.R
import com.example.devpath.data.repository.YdbRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class StepCounterService : Service(), SensorEventListener {

    @Inject
    lateinit var ydbRepository: YdbRepository

    private val binder = LocalBinder()
    private lateinit var sensorManager: SensorManager
    private var stepCounterSensor: Sensor? = null
    private var stepDetectorSensor: Sensor? = null

    private val _stepCount = MutableStateFlow(0)
    val stepCount: StateFlow<Int> = _stepCount

    private val _todaySteps = MutableStateFlow(0)
    val todaySteps: StateFlow<Int> = _todaySteps

    private var isRunning = false
    private lateinit var sharedPreferences: SharedPreferences
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    companion object {
        const val NOTIFICATION_ID = 1001
        const val CHANNEL_ID = "step_counter_channel"
        const val CHANNEL_NAME = "Шагомер"
        private const val TAG = "StepCounterService"
        private const val PREFS_NAME = "step_counter"
        private const val KEY_TODAY_STEPS = "today_steps"
        private const val KEY_LAST_DATE = "last_date"
    }

    inner class LocalBinder : Binder() {
        fun getService(): StepCounterService = this@StepCounterService
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate - сервис создаётся")

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        stepDetectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)

        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        loadSavedSteps()
        createNotificationChannel()
        startForegroundWithNotification()

        Log.d(TAG, "STEP_COUNTER сенсор: ${stepCounterSensor != null}")
        Log.d(TAG, "STEP_DETECTOR сенсор: ${stepDetectorSensor != null}")
    }

    private fun startForegroundWithNotification() {
        val notification = createNotification()
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_HEALTH)
                Log.d(TAG, "startForeground с типом HEALTH выполнен")
            } else {
                startForeground(NOTIFICATION_ID, notification)
                Log.d(TAG, "startForeground выполнен (без типа)")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка startForeground: ${e.message}")
            try {
                startForeground(NOTIFICATION_ID, notification)
            } catch (e2: Exception) {
                Log.e(TAG, "Критическая ошибка startForeground: ${e2.message}")
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Отслеживание шагов в фоновом режиме"
                setShowBadge(false)
                enableVibration(false)
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
            Log.d(TAG, "Канал уведомлений создан")
        }
    }

    private fun createNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val steps = _todaySteps.value
        val goal = 10000
        val progress = if (goal > 0) ((steps.toFloat() / goal.toFloat()) * 100).toInt() else 0

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Шагомер активен")
            .setContentText("$steps шагов сегодня • $progress% от цели ($goal)")
            .setSmallIcon(R.drawable.ic_step_notification)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }

    private fun updateNotification() {
        try {
            val notification = createNotification()
            val notificationManager = getSystemService(NotificationManager::class.java)
            if (notificationManager != null) {
                notificationManager.notify(NOTIFICATION_ID, notification)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка обновления уведомления: ${e.message}")
        }
    }

    private fun loadSavedSteps() {
        val today = dateFormat.format(Date())
        val savedDate = sharedPreferences.getString(KEY_LAST_DATE, "") ?: ""
        val savedTodaySteps = sharedPreferences.getInt(KEY_TODAY_STEPS, 0)

        Log.d(TAG, "loadSavedSteps: today=$today, savedDate=$savedDate, savedSteps=$savedTodaySteps")

        if (savedDate == today) {
            _todaySteps.value = savedTodaySteps
            _stepCount.value = savedTodaySteps
            Log.d(TAG, "Восстановлены сохранённые шаги: $savedTodaySteps")
        } else {
            Log.d(TAG, "Новый день! Сброс шагов. Было: $savedDate, стало: $today")
            _todaySteps.value = 0
            _stepCount.value = 0

            sharedPreferences.edit()
                .putString(KEY_LAST_DATE, today)
                .putInt(KEY_TODAY_STEPS, 0)
                .apply()
        }
    }

    private fun saveSteps(steps: Int) {
        val today = dateFormat.format(Date())
        val editor = sharedPreferences.edit()
        editor.putInt(KEY_TODAY_STEPS, steps)
        editor.putString(KEY_LAST_DATE, today)
        editor.apply()
        updateNotification()
    }

    /**
     * Сохраняет шаги в YDB (вызывается из фона)
     */
    private fun syncStepsToCloud(steps: Int) {
        serviceScope.launch {
            try {
                val userPrefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                val userId = userPrefs.getString("user_id", "") ?: ""
                val userName = userPrefs.getString("user_name", "Пользователь") ?: "Пользователь"

                if (userId.isEmpty()) {
                    Log.w(TAG, "Нет userId, пропускаем синхронизацию в облако")
                    return@launch
                }

                val today = dateFormat.format(Date())
                val stepId = "$userId`_`$today"

                // Получаем данные пользователя
                val userData = ydbRepository.getUser(userId)
                val avatarUrl = userData?.optJSONObject("avatar_url")?.optString("S", "")?.ifEmpty { null }
                val displayName = userData?.optJSONObject("name")?.optString("S", userName) ?: userName

                val item = JSONObject().apply {
                    put("step_id", JSONObject().put("S", stepId))
                    put("user_id", JSONObject().put("S", userId))
                    put("user_name", JSONObject().put("S", displayName))
                    put("user_avatar", JSONObject().put("S", avatarUrl ?: ""))
                    put("steps", JSONObject().put("N", steps.toString()))
                    put("date", JSONObject().put("S", today))
                    put("updated_at", JSONObject().put("S", System.currentTimeMillis().toString()))
                }

                val body = JSONObject().apply {
                    put("TableName", "steps_doc")
                    put("Item", item)
                }

                val result = ydbRepository.executeSignedRequest("PutItem", body)
                if (result != null) {
                    Log.d(TAG, "☁️ Шаги отправлены в облако: $steps")
                } else {
                    Log.e(TAG, "❌ Ошибка отправки шагов в облако")
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Ошибка синхронизации: ${e.message}")
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand вызван, flags=$flags, startId=$startId")

        val today = dateFormat.format(Date())
        val savedDate = sharedPreferences.getString(KEY_LAST_DATE, "") ?: ""
        if (savedDate != today && savedDate.isNotEmpty()) {
            Log.d(TAG, "Обнаружена смена даты при старте! $savedDate -> $today")
            _todaySteps.value = 0
            _stepCount.value = 0
            sharedPreferences.edit()
                .putString(KEY_LAST_DATE, today)
                .putInt(KEY_TODAY_STEPS, 0)
                .apply()
        }

        try {
            startForegroundWithNotification()
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка в onStartCommand: ${e.message}")
        }

        startStepCounting()

        return START_STICKY
    }

    fun startStepCounting() {
        if (!isRunning) {
            if (!hasPermission()) {
                Log.w(TAG, "Нет разрешения ACTIVITY_RECOGNITION, подсчёт шагов невозможен")
                return
            }

            var sensorRegistered = false

            if (stepCounterSensor != null) {
                val result = sensorManager.registerListener(
                    this,
                    stepCounterSensor,
                    SensorManager.SENSOR_DELAY_NORMAL,
                    SensorManager.SENSOR_DELAY_UI.toInt()
                )
                if (result) {
                    isRunning = true
                    sensorRegistered = true
                    Log.d(TAG, "Слушатель STEP_COUNTER зарегистрирован успешно")
                }
            }

            if (!sensorRegistered && stepDetectorSensor != null) {
                val result = sensorManager.registerListener(
                    this,
                    stepDetectorSensor,
                    SensorManager.SENSOR_DELAY_NORMAL,
                    SensorManager.SENSOR_DELAY_UI.toInt()
                )
                if (result) {
                    isRunning = true
                    sensorRegistered = true
                    Log.d(TAG, "Слушатель STEP_DETECTOR зарегистрирован успешно")
                }
            }

            if (!sensorRegistered) {
                Log.e(TAG, "Не удалось зарегистрировать ни один сенсор шагов!")
            }
        } else {
            Log.d(TAG, "Подсчёт шагов уже запущен")
        }
    }

    fun stopStepCounting() {
        if (isRunning) {
            sensorManager.unregisterListener(this)
            isRunning = false
            Log.d(TAG, "Подсчёт шагов остановлен")
        }
    }

    fun resetStepCount() {
        _stepCount.value = 0
        _todaySteps.value = 0
        saveSteps(0)
        Log.d(TAG, "Счётчик шагов сброшен")
    }

    private fun hasPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACTIVITY_RECOGNITION
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor == null) return

        when (event.sensor.type) {
            Sensor.TYPE_STEP_COUNTER -> {
                val totalStepsFromSensor = event.values[0].toLong()
                val currentDate = dateFormat.format(Date())
                val savedDate = sharedPreferences.getString(KEY_LAST_DATE, "") ?: ""

                if (savedDate.isNotEmpty() && savedDate != currentDate) {
                    Log.d(TAG, "Новый день! Сброс. $savedDate -> $currentDate")
                    _todaySteps.value = 0
                    _stepCount.value = 0
                    saveSteps(0)
                    sharedPreferences.edit()
                        .putLong("last_sensor_value", totalStepsFromSensor)
                        .apply()
                    return
                }

                val savedTodaySteps = _todaySteps.value
                val lastSensorValue = sharedPreferences.getLong("last_sensor_value", -1L)

                if (lastSensorValue < 0) {
                    sharedPreferences.edit()
                        .putLong("last_sensor_value", totalStepsFromSensor)
                        .apply()
                    Log.d(TAG, "Первое событие: sensor=$totalStepsFromSensor, steps=$savedTodaySteps")
                    if (savedTodaySteps > 0) {
                        saveSteps(savedTodaySteps)
                    }
                } else if (totalStepsFromSensor > lastSensorValue) {
                    val newSteps = totalStepsFromSensor - lastSensorValue
                    val updatedSteps = savedTodaySteps + newSteps.toInt()

                    _stepCount.value = updatedSteps
                    _todaySteps.value = updatedSteps

                    sharedPreferences.edit()
                        .putLong("last_sensor_value", totalStepsFromSensor)
                        .apply()

                    // Каждые 50 шагов: сохраняем локально И отправляем в облако
                    if (updatedSteps % 50 == 0) {
                        saveSteps(updatedSteps)
                        syncStepsToCloud(updatedSteps)
                        Log.d(TAG, "💾+☁️ Сохранение: $updatedSteps шагов")
                    } else if (updatedSteps % 10 == 0) {
                        Log.d(TAG, "Шаги: $updatedSteps (+$newSteps новых)")
                    }
                } else if (totalStepsFromSensor < lastSensorValue) {
                    Log.w(TAG, "Сенсор сбросился! Было: $lastSensorValue, стало: $totalStepsFromSensor")
                    sharedPreferences.edit()
                        .putLong("last_sensor_value", totalStepsFromSensor)
                        .apply()
                }
            }
            Sensor.TYPE_STEP_DETECTOR -> {
                val newSteps = _stepCount.value + 1
                _stepCount.value = newSteps
                _todaySteps.value = newSteps

                if (newSteps % 50 == 0) {
                    saveSteps(newSteps)
                    syncStepsToCloud(newSteps)
                    Log.d(TAG, "💾+☁️ Сохранение (детектор): $newSteps шагов")
                } else if (newSteps % 10 == 0) {
                    Log.d(TAG, "Шаги (детектор): $newSteps")
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onBind(intent: Intent?): IBinder {
        Log.d(TAG, "onBind - клиент подключился к сервису")
        return binder
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Log.d(TAG, "onUnbind - клиент отключился от сервиса")
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy - сервис уничтожается, сохраняем шаги")
        stopStepCounting()
        saveSteps(_todaySteps.value)
        syncStepsToCloud(_todaySteps.value) // Последняя синхронизация
        super.onDestroy()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        Log.d(TAG, "onTaskRemoved - приложение удалено из недавних")
        saveSteps(_todaySteps.value)
        syncStepsToCloud(_todaySteps.value) // Синхронизация при удалении
        super.onTaskRemoved(rootIntent)
    }
}