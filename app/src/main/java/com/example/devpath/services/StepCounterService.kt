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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.text.SimpleDateFormat
import java.util.*

class StepCounterService : Service(), SensorEventListener {

    private val binder = LocalBinder()
    private lateinit var sensorManager: SensorManager
    private var stepCounterSensor: Sensor? = null
    private var stepDetectorSensor: Sensor? = null

    private val _stepCount = MutableStateFlow(0)
    val stepCount: StateFlow<Int> = _stepCount

    private val _todaySteps = MutableStateFlow(0)
    val todaySteps: StateFlow<Int> = _todaySteps

    private var isRunning = false
    private var initialStepCount = 0L
    private lateinit var sharedPreferences: SharedPreferences

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    companion object {
        const val NOTIFICATION_ID = 1001
        const val CHANNEL_ID = "step_counter_channel"
    }

    inner class LocalBinder : Binder() {
        fun getService(): StepCounterService = this@StepCounterService
    }

    override fun onCreate() {
        super.onCreate()
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        stepDetectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)

        sharedPreferences = getSharedPreferences("step_counter", Context.MODE_PRIVATE)

        loadSavedSteps()
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification())

        Log.d("StepCounter", "STEP_COUNTER: ${stepCounterSensor != null}")
        Log.d("StepCounter", "STEP_DETECTOR: ${stepDetectorSensor != null}")
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Шагомер",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Отслеживание шагов в фоне"
                setShowBadge(false)
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
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
        val progress = (steps.toFloat() / goal * 100).toInt()

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Шагомер")
            .setContentText("$steps шагов сегодня • $progress% от цели")
            .setSmallIcon(R.drawable.ic_step_notification)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun updateNotification() {
        val notification = createNotification()
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun loadSavedSteps() {
        val today = dateFormat.format(Date())
        val savedDate = sharedPreferences.getString("last_date", "")
        val savedSteps = sharedPreferences.getInt("today_steps", 0)

        if (savedDate == today) {
            _todaySteps.value = savedSteps
            _stepCount.value = savedSteps
            initialStepCount = sharedPreferences.getLong("initial_step_count", 0)
            Log.d("StepCounter", "Загружены сохраненные шаги: $savedSteps")
        } else {
            sharedPreferences.edit().putString("last_date", today).apply()
            sharedPreferences.edit().putInt("today_steps", 0).apply()
            _todaySteps.value = 0
            _stepCount.value = 0
            initialStepCount = 0
            Log.d("StepCounter", "Новый день, шаги сброшены")
        }
    }

    private fun saveSteps(steps: Int) {
        val today = dateFormat.format(Date())
        sharedPreferences.edit().putInt("today_steps", steps).apply()
        sharedPreferences.edit().putString("last_date", today).apply()
        sharedPreferences.edit().putLong("initial_step_count", initialStepCount).apply()
        updateNotification()
        Log.d("StepCounter", "Шаги сохранены: $steps")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startStepCounting()
        return START_STICKY
    }

    fun startStepCounting() {
        if (!isRunning) {
            if (!hasPermission()) return

            if (stepCounterSensor != null) {
                sensorManager.registerListener(this, stepCounterSensor, SensorManager.SENSOR_DELAY_NORMAL)
                isRunning = true
                Log.d("StepCounter", "Started with STEP_COUNTER")
            } else if (stepDetectorSensor != null) {
                sensorManager.registerListener(this, stepDetectorSensor, SensorManager.SENSOR_DELAY_NORMAL)
                isRunning = true
                Log.d("StepCounter", "Started with STEP_DETECTOR")
            } else {
                Log.e("StepCounter", "No step sensor available!")
            }
        }
    }

    fun stopStepCounting() {
        if (isRunning) {
            sensorManager.unregisterListener(this)
            isRunning = false
        }
    }

    fun resetStepCount() {
        _stepCount.value = 0
        _todaySteps.value = 0
        initialStepCount = 0
        saveSteps(0)
        Log.d("StepCounter", "Step count reset")
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
        if (event?.sensor != null) {
            when (event.sensor.type) {
                Sensor.TYPE_STEP_COUNTER -> {
                    val totalSteps = event.values[0].toLong()
                    if (initialStepCount == 0L) {
                        initialStepCount = totalSteps
                        val savedTodaySteps = sharedPreferences.getInt("today_steps", 0)
                        if (savedTodaySteps > 0) {
                            initialStepCount = totalSteps - savedTodaySteps
                        }
                    }
                    val currentSteps = (totalSteps - initialStepCount).toInt()
                    _stepCount.value = currentSteps

                    val currentDate = dateFormat.format(Date())
                    val savedDate = sharedPreferences.getString("last_date", "")

                    if (currentDate != savedDate) {
                        _todaySteps.value = currentSteps
                        saveSteps(currentSteps)
                    } else {
                        _todaySteps.value = currentSteps
                        saveSteps(currentSteps)
                    }
                }
                Sensor.TYPE_STEP_DETECTOR -> {
                    val newSteps = _stepCount.value + 1
                    _stepCount.value = newSteps
                    _todaySteps.value = newSteps
                    saveSteps(newSteps)
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onDestroy() {
        stopStepCounting()
        super.onDestroy()
    }
}