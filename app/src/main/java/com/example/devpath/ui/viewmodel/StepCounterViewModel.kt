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
import com.example.devpath.data.repository.ChatRepository
import com.example.devpath.data.repository.StepStatsRepository
import com.example.devpath.domain.models.LeaderboardEntry
import com.example.devpath.services.StepCounterService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StepCounterViewModel @Inject constructor(
    private val stepStatsRepository: StepStatsRepository,
    private val chatRepository: ChatRepository
) : ViewModel() {

    private var stepService: StepCounterService? = null
    private var bound = false

    private val _stepCount = MutableStateFlow(0)
    val stepCount: StateFlow<Int> = _stepCount

    private val _todaySteps = MutableStateFlow(0)
    val todaySteps: StateFlow<Int> = _todaySteps

    // Топы
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
                stepService?.stepCount?.collect { count ->
                    _stepCount.value = count
                }
            }
            viewModelScope.launch {
                stepService?.todaySteps?.collect { steps ->
                    _todaySteps.value = steps
                }
            }
            stepService?.startStepCounting()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            bound = false
            stepService = null
        }
    }

    init {
        viewModelScope.launch {
            stepStatsRepository.updateMissingAvatars()
            stepStatsRepository.updateAllAvatarsInLeaderboards()
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
                stepStatsRepository.updateUserAvatar(userId)

                val userProfile = chatRepository.getUser(userId)
                val avatarUrl = userProfile?.avatarUrl
                val displayName = userProfile?.name ?: userName

                println("StepCounter DEBUG: Saving steps for $displayName, avatarUrl: $avatarUrl")
                stepStatsRepository.saveStepStats(userId, displayName, avatarUrl, steps)
            }
        }
    }

    fun loadAllLeaderboards() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Загружаем все топы параллельно
                val allTimeDeferred = async { stepStatsRepository.getAllTimeLeaderboard(10) }
                val todayDeferred = async { stepStatsRepository.getTodayLeaderboard(10) }
                val weeklyDeferred = async { stepStatsRepository.getWeeklyLeaderboard(10) }
                val monthlyDeferred = async { stepStatsRepository.getMonthlyLeaderboard(10) }

                _allTimeLeaderboard.value = allTimeDeferred.await()
                _todayLeaderboard.value = todayDeferred.await()
                _weeklyLeaderboard.value = weeklyDeferred.await()
                _monthlyLeaderboard.value = monthlyDeferred.await()

            } catch (e: Exception) {
                println("Error loading leaderboards: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun observeWeeklyStats(userId: String) {
        viewModelScope.launch {
            stepStatsRepository.observeWeeklyStats(userId).collectLatest { stats ->
                _weeklyStats.value = stats
                val total = stats.values.sum()
                _weeklyTotal.value = total
            }
        }
    }

    fun loadTotals(userId: String) {
        viewModelScope.launch {
            try {
                val weeklyDeferred = async { stepStatsRepository.getWeeklyTotal(userId) }
                val monthlyDeferred = async { stepStatsRepository.getMonthlyTotal(userId) }
                val yearlyDeferred = async { stepStatsRepository.getYearlyTotal(userId) }

                _weeklyTotal.value = weeklyDeferred.await()
                _monthlyTotal.value = monthlyDeferred.await()
                _yearlyTotal.value = yearlyDeferred.await()

            } catch (e: Exception) {
                println("StepCounter ERROR: ${e.message}")
            }
        }
    }
}