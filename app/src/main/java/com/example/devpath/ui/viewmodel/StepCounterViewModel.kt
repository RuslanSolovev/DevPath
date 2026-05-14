package com.example.devpath.ui.viewmodel

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.devpath.data.repository.YdbRepository
import com.example.devpath.domain.models.LeaderboardEntry
import com.example.devpath.services.StepCounterService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class StepCounterViewModel @Inject constructor(
    private val ydbRepository: YdbRepository
) : ViewModel() {

    private var stepService: StepCounterService? = null
    private var bound = false

    private val _stepCount = MutableStateFlow(0)
    val stepCount: StateFlow<Int> = _stepCount

    private val _todaySteps = MutableStateFlow(0)
    val todaySteps: StateFlow<Int> = _todaySteps

    private val _allTimeLeaderboard = MutableStateFlow<List<LeaderboardEntry>>(emptyList())
    val allTimeLeaderboard: StateFlow<List<LeaderboardEntry>> = _allTimeLeaderboard.asStateFlow()

    private val _todayLeaderboard = MutableStateFlow<List<LeaderboardEntry>>(emptyList())
    val todayLeaderboard: StateFlow<List<LeaderboardEntry>> = _todayLeaderboard.asStateFlow()

    private val _weeklyLeaderboard = MutableStateFlow<List<LeaderboardEntry>>(emptyList())
    val weeklyLeaderboard: StateFlow<List<LeaderboardEntry>> = _weeklyLeaderboard.asStateFlow()

    private val _monthlyLeaderboard = MutableStateFlow<List<LeaderboardEntry>>(emptyList())
    val monthlyLeaderboard: StateFlow<List<LeaderboardEntry>> = _monthlyLeaderboard.asStateFlow()

    private val _weeklyStats = MutableStateFlow<Map<String, Int>>(emptyMap())
    val weeklyStats: StateFlow<Map<String, Int>> = _weeklyStats.asStateFlow()

    private val _weeklyTotal = MutableStateFlow(0)
    val weeklyTotal: StateFlow<Int> = _weeklyTotal.asStateFlow()

    private val _monthlyTotal = MutableStateFlow(0)
    val monthlyTotal: StateFlow<Int> = _monthlyTotal.asStateFlow()

    private val _yearlyTotal = MutableStateFlow(0)
    val yearlyTotal: StateFlow<Int> = _yearlyTotal.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Для троттлинга сохранения шагов
    private var lastSavedSteps = 0
    private var lastSaveTimeMs = 0L
    private val minSaveIntervalMs = 120_000L // 2 минуты для более частого обновления
    private val minStepDifference = 25 // сохраняем если изменилось больше чем на 25 шагов

    // ID текущего пользователя (устанавливается при загрузке)
    private var currentUserId: String = ""
    private var currentUserName: String = ""

    // Флаг для периодического обновления
    private var isPeriodicUpdateActive = false

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            stepService = (service as StepCounterService.LocalBinder).getService()
            bound = true
            viewModelScope.launch {
                stepService?.stepCount?.collect { count ->
                    _stepCount.value = count
                }
            }
            viewModelScope.launch {
                stepService?.todaySteps?.collect { steps ->
                    _todaySteps.value = steps
                    // При каждом изменении шагов обновляем totals в фоне
                    if (currentUserId.isNotEmpty()) {
                        launch {
                            delay(2000) // Небольшая задержка чтобы не дёргать БД на каждый шаг
                            loadTotals(currentUserId)
                        }
                    }
                }
            }
            stepService?.startStepCounting()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            bound = false
            stepService = null
        }
    }

    fun hasStepPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ActivityCompat.checkSelfPermission(
                context,
                android.Manifest.permission.ACTIVITY_RECOGNITION
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun isBatteryOptimizationDisabled(context: Context): Boolean {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        return powerManager.isIgnoringBatteryOptimizations(context.packageName)
    }

    fun bindService(context: Context) {
        val intent = Intent(context, StepCounterService::class.java)
        try {
            context.bindService(intent, connection, Context.BIND_AUTO_CREATE)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        } catch (e: Exception) {
            println("StepCounter ERROR: bindService failed - ${e.message}")
        }
    }

    fun unbindService(context: Context) {
        if (bound) {
            try {
                context.unbindService(connection)
                bound = false
            } catch (e: Exception) {
                println("StepCounter ERROR: unbindService failed - ${e.message}")
            }
        }
    }

    fun resetSteps() {
        stepService?.resetStepCount()
        _stepCount.value = 0
        _todaySteps.value = 0
        lastSavedSteps = 0
        lastSaveTimeMs = 0L
    }

    /**
     * Инициализация экрана - загружаем всё и запускаем периодическое обновление
     */
    fun initialize(userId: String, userName: String) {
        currentUserId = userId
        currentUserName = userName

        loadAllLeaderboards()
        observeWeeklyStats(userId)
        loadTotals(userId)

        // Запускаем периодическое обновление лидербордов
        startPeriodicUpdate()
    }

    /**
     * Периодическое обновление лидербордов каждые 30 секунд
     */
    private fun startPeriodicUpdate() {
        if (isPeriodicUpdateActive) return
        isPeriodicUpdateActive = true

        viewModelScope.launch {
            while (isActive && isPeriodicUpdateActive) {
                delay(30_000L) // 30 секунд
                if (currentUserId.isNotEmpty()) {
                    println("DEBUG: Периодическое обновление лидербордов...")
                    loadAllLeaderboards()
                    loadTotals(currentUserId)
                }
            }
        }
    }

    fun stopPeriodicUpdate() {
        isPeriodicUpdateActive = false
    }

    fun saveStepsThrottled(userId: String, userName: String) {
        val steps = _todaySteps.value
        val now = System.currentTimeMillis()

        // Проверяем условия для сохранения
        val stepsChanged = kotlin.math.abs(steps - lastSavedSteps) >= minStepDifference
        val timePassed = (now - lastSaveTimeMs) >= minSaveIntervalMs
        val isFirstSave = lastSaveTimeMs == 0L

        if (steps > 0 && (isFirstSave || stepsChanged || timePassed)) {
            lastSavedSteps = steps
            lastSaveTimeMs = now
            performSaveSteps(userId, userName)
        }
    }

    private fun performSaveSteps(userId: String, userName: String) {
        viewModelScope.launch {
            val steps = _todaySteps.value
            if (steps > 0) {
                try {
                    val user = ydbRepository.getUser(userId)
                    val avatarUrl = user?.optJSONObject("avatar_url")?.optString("S", "")?.ifEmpty { null }
                    val displayName = user?.optJSONObject("name")?.optString("S", userName) ?: userName

                    val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                    val stepId = "$userId`_`$today"

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
                        println("StepCounter: ✅ шаги сохранены в YDB: userId=$userId, steps=$steps")
                        // После сохранения обновляем лидерборды
                        loadAllLeaderboards()
                        loadTotals(userId)
                    } else {
                        println("StepCounter: ⚠️ не удалось сохранить шаги в YDB")
                    }
                } catch (e: Exception) {
                    println("StepCounter ERROR: ${e.message}")
                    e.printStackTrace()
                }
            }
        }
    }

    fun loadAllLeaderboards() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

                val todayDeferred = async { getLeaderboard(today) }
                val weeklyDeferred = async { getWeeklyLeaderboard() }
                val monthlyDeferred = async { getMonthlyLeaderboard() }
                val allTimeDeferred = async { getAllTimeLeaderboard() }

                _todayLeaderboard.value = todayDeferred.await()
                _weeklyLeaderboard.value = weeklyDeferred.await()
                _monthlyLeaderboard.value = monthlyDeferred.await()
                _allTimeLeaderboard.value = allTimeDeferred.await()

            } catch (e: Exception) {
                println("Error loading leaderboards: ${e.message}")
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun getLeaderboard(date: String): List<LeaderboardEntry> {
        val body = JSONObject().apply {
            put("TableName", "steps_doc")
            put("FilterExpression", "#date = :date")
            put("ExpressionAttributeNames", JSONObject().apply { put("#date", "date") })
            put("ExpressionAttributeValues", JSONObject().apply {
                put(":date", JSONObject().put("S", date))
            })
            put("Limit", 100)
        }

        val result = ydbRepository.executeSignedRequest("Scan", body)
        val items = result?.optJSONArray("Items") ?: JSONArray()

        println("DEBUG: getLeaderboard - получено записей за $date: ${items.length()}")

        val list = (0 until items.length()).map { index ->
            val json = items.getJSONObject(index)
            val userId = json.optJSONObject("user_id")?.optString("S") ?: ""
            val steps = json.optJSONObject("steps")?.optString("N")?.toIntOrNull() ?: 0
            val userName = json.optJSONObject("user_name")?.optString("S") ?: ""
            val userAvatar = json.optJSONObject("user_avatar")?.optString("S")?.ifEmpty { null }

            println("DEBUG: getLeaderboard - пользователь $userName: $steps шагов")

            LeaderboardEntry(
                rank = 0,
                userId = userId,
                userName = userName,
                totalSteps = steps,
                userAvatar = userAvatar
            )
        }

        val sortedList = list
            .sortedByDescending { it.totalSteps }
            .take(10)
            .mapIndexed { index, entry ->
                entry.copy(rank = index + 1)
            }

        println("DEBUG: getLeaderboard - итоговый список: ${sortedList.size} записей")
        sortedList.forEach { entry ->
            println("DEBUG: getLeaderboard - #${entry.rank} ${entry.userName}: ${entry.totalSteps} шагов")
        }

        return sortedList
    }

    private suspend fun getWeeklyLeaderboard(): List<LeaderboardEntry> {
        val calendar = Calendar.getInstance()
        val endDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
        calendar.add(Calendar.DAY_OF_YEAR, -7)
        val startDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)

        val body = JSONObject().apply {
            put("TableName", "steps_doc")
            put("FilterExpression", "#date BETWEEN :start AND :end")
            put("ExpressionAttributeNames", JSONObject().apply { put("#date", "date") })
            put("ExpressionAttributeValues", JSONObject().apply {
                put(":start", JSONObject().put("S", startDate))
                put(":end", JSONObject().put("S", endDate))
            })
            put("Limit", 100)
        }
        val result = ydbRepository.executeSignedRequest("Scan", body)
        val items = result?.optJSONArray("Items") ?: JSONArray()
        val userSteps = mutableMapOf<String, LeaderboardEntry>()
        for (i in 0 until items.length()) {
            val json = items.getJSONObject(i)
            val userId = json.optJSONObject("user_id")?.optString("S") ?: ""
            val steps = json.optJSONObject("steps")?.optString("N")?.toIntOrNull() ?: 0
            val existing = userSteps[userId]
            if (existing != null) {
                userSteps[userId] = existing.copy(totalSteps = existing.totalSteps + steps)
            } else {
                userSteps[userId] = LeaderboardEntry(
                    userId = userId,
                    userName = json.optJSONObject("user_name")?.optString("S") ?: "",
                    totalSteps = steps,
                    userAvatar = json.optJSONObject("user_avatar")?.optString("S")?.ifEmpty { null }
                )
            }
        }
        return userSteps.values.sortedByDescending { it.totalSteps }.take(10).mapIndexed { index, entry -> entry.copy(rank = index + 1) }
    }

    private suspend fun getMonthlyLeaderboard(): List<LeaderboardEntry> {
        val calendar = Calendar.getInstance()
        val endDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
        calendar.add(Calendar.DAY_OF_YEAR, -30)
        val startDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
        return getLeaderboardForRange(startDate, endDate)
    }

    private suspend fun getAllTimeLeaderboard(): List<LeaderboardEntry> {
        val body = JSONObject().apply {
            put("TableName", "steps_doc")
            put("Limit", 200)
        }
        val result = ydbRepository.executeSignedRequest("Scan", body)
        val items = result?.optJSONArray("Items") ?: JSONArray()
        val userSteps = mutableMapOf<String, LeaderboardEntry>()
        for (i in 0 until items.length()) {
            val json = items.getJSONObject(i)
            val userId = json.optJSONObject("user_id")?.optString("S") ?: ""
            val steps = json.optJSONObject("steps")?.optString("N")?.toIntOrNull() ?: 0
            val existing = userSteps[userId]
            if (existing != null) {
                userSteps[userId] = existing.copy(totalSteps = existing.totalSteps + steps)
            } else {
                userSteps[userId] = LeaderboardEntry(
                    userId = userId,
                    userName = json.optJSONObject("user_name")?.optString("S") ?: "",
                    totalSteps = steps,
                    userAvatar = json.optJSONObject("user_avatar")?.optString("S")?.ifEmpty { null }
                )
            }
        }
        return userSteps.values.sortedByDescending { it.totalSteps }.take(10).mapIndexed { index, entry -> entry.copy(rank = index + 1) }
    }

    private suspend fun getLeaderboardForRange(startDate: String, endDate: String): List<LeaderboardEntry> {
        val body = JSONObject().apply {
            put("TableName", "steps_doc")
            put("FilterExpression", "#date BETWEEN :start AND :end")
            put("ExpressionAttributeNames", JSONObject().apply { put("#date", "date") })
            put("ExpressionAttributeValues", JSONObject().apply {
                put(":start", JSONObject().put("S", startDate))
                put(":end", JSONObject().put("S", endDate))
            })
            put("Limit", 200)
        }
        val result = ydbRepository.executeSignedRequest("Scan", body)
        val items = result?.optJSONArray("Items") ?: JSONArray()
        val userSteps = mutableMapOf<String, LeaderboardEntry>()
        for (i in 0 until items.length()) {
            val json = items.getJSONObject(i)
            val userId = json.optJSONObject("user_id")?.optString("S") ?: ""
            val steps = json.optJSONObject("steps")?.optString("N")?.toIntOrNull() ?: 0
            val existing = userSteps[userId]
            if (existing != null) {
                userSteps[userId] = existing.copy(totalSteps = existing.totalSteps + steps)
            } else {
                userSteps[userId] = LeaderboardEntry(
                    userId = userId,
                    userName = json.optJSONObject("user_name")?.optString("S") ?: "",
                    totalSteps = steps,
                    userAvatar = json.optJSONObject("user_avatar")?.optString("S")?.ifEmpty { null }
                )
            }
        }
        return userSteps.values.sortedByDescending { it.totalSteps }.take(10).mapIndexed { index, entry -> entry.copy(rank = index + 1) }
    }

    fun observeWeeklyStats(userId: String) {
        viewModelScope.launch {
            try {
                val calendar = Calendar.getInstance()
                calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
                val startDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)

                val body = JSONObject().apply {
                    put("TableName", "steps_doc")
                    put("FilterExpression", "user_id = :userId AND #date >= :startDate")
                    put("ExpressionAttributeNames", JSONObject().apply { put("#date", "date") })
                    put("ExpressionAttributeValues", JSONObject().apply {
                        put(":userId", JSONObject().put("S", userId))
                        put(":startDate", JSONObject().put("S", startDate))
                    })
                    put("Limit", 7)
                }
                val result = ydbRepository.executeSignedRequest("Scan", body)
                val items = result?.optJSONArray("Items") ?: JSONArray()
                val dayNames = listOf("ПН", "ВТ", "СР", "ЧТ", "ПТ", "СБ", "ВС")
                val stats = mutableMapOf<String, Int>()
                dayNames.forEach { stats[it] = 0 }
                for (i in 0 until items.length()) {
                    val json = items.getJSONObject(i)
                    val date = json.optJSONObject("date")?.optString("S") ?: ""
                    val steps = json.optJSONObject("steps")?.optString("N")?.toIntOrNull() ?: 0
                    val dayCal = Calendar.getInstance()
                    dayCal.time = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(date) ?: continue
                    val dayIndex = (dayCal.get(Calendar.DAY_OF_WEEK) + 5) % 7
                    stats[dayNames[dayIndex]] = (stats[dayNames[dayIndex]] ?: 0) + steps
                }
                _weeklyStats.value = stats
                _weeklyTotal.value = stats.values.sum()
            } catch (e: Exception) {
                println("StepCounter observeWeeklyStats ERROR: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    fun loadTotals(userId: String) {
        viewModelScope.launch {
            try {
                val body = JSONObject().apply {
                    put("TableName", "steps_doc")
                    put("FilterExpression", "user_id = :userId")
                    put("ExpressionAttributeValues", JSONObject().apply {
                        put(":userId", JSONObject().put("S", userId))
                    })
                    put("Limit", 365)
                }
                val result = ydbRepository.executeSignedRequest("Scan", body)
                val items = result?.optJSONArray("Items") ?: JSONArray()

                var weekly = 0
                var monthly = 0
                var yearly = 0

                val now = Calendar.getInstance()
                val weekStart = now.clone() as Calendar
                weekStart.add(Calendar.DAY_OF_YEAR, -7)
                val monthStart = now.clone() as Calendar
                monthStart.add(Calendar.DAY_OF_YEAR, -30)
                val yearStart = now.clone() as Calendar
                yearStart.add(Calendar.DAY_OF_YEAR, -365)

                for (i in 0 until items.length()) {
                    val json = items.getJSONObject(i)
                    val dateStr = json.optJSONObject("date")?.optString("S") ?: ""
                    val steps = json.optJSONObject("steps")?.optString("N")?.toIntOrNull() ?: 0
                    val date = try {
                        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(dateStr)
                    } catch (e: Exception) {
                        null
                    }
                    if (date != null) {
                        val dateCal = Calendar.getInstance()
                        dateCal.time = date
                        if (dateCal.after(weekStart)) weekly += steps
                        if (dateCal.after(monthStart)) monthly += steps
                        if (dateCal.after(yearStart)) yearly += steps
                    }
                }
                _weeklyTotal.value = weekly
                _monthlyTotal.value = monthly
                _yearlyTotal.value = yearly
            } catch (e: Exception) {
                println("StepCounter loadTotals ERROR: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopPeriodicUpdate()
    }
}