package com.example.devpath.ui.viewmodel

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.devpath.data.repository.YdbRepository
import com.example.devpath.domain.models.LeaderboardEntry
import com.example.devpath.services.StepCounterService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
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

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            stepService = (service as StepCounterService.LocalBinder).getService()
            bound = true
            viewModelScope.launch {
                stepService?.stepCount?.collect { count -> _stepCount.value = count }
            }
            viewModelScope.launch {
                stepService?.todaySteps?.collect { steps -> _todaySteps.value = steps }
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
            ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_GRANTED
        } else true
    }

    fun bindService(context: Context) {
        val intent = Intent(context, StepCounterService::class.java)
        context.bindService(intent, connection, Context.BIND_AUTO_CREATE)
        context.startService(intent)
    }

    fun unbindService(context: Context) {
        if (bound) {
            context.unbindService(connection)
            bound = false
        }
    }

    fun resetSteps() {
        stepService?.resetStepCount()
        _stepCount.value = 0
        _todaySteps.value = 0
    }

    fun saveSteps(userId: String, userName: String) {
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

                    ydbRepository.executeSignedRequest("PutItem", body)
                } catch (e: Exception) {
                    println("StepCounter ERROR: ${e.message}")
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
            put("Limit", 10)
        }
        val result = ydbRepository.executeSignedRequest("Scan", body)
        val items = result?.optJSONArray("Items") ?: JSONArray()
        val list = (0 until items.length()).map { items.getJSONObject(it) }
        return list.sortedByDescending { it.optJSONObject("steps")?.optString("N")?.toIntOrNull() ?: 0 }
            .mapIndexed { index, json ->
                LeaderboardEntry(
                    rank = index + 1,
                    userId = json.optJSONObject("user_id")?.optString("S") ?: "",
                    userName = json.optJSONObject("user_name")?.optString("S") ?: "",
                    totalSteps = json.optJSONObject("steps")?.optString("N")?.toIntOrNull() ?: 0,
                    userAvatar = json.optJSONObject("user_avatar")?.optString("S")?.ifEmpty { null }
                )
            }
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

    private suspend fun getLeaderboardForRange(startDate: String, endDate: String): List<LeaderboardEntry> {
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
                    val date = try { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(dateStr) } catch (e: Exception) { null }
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
            }
        }
    }
}