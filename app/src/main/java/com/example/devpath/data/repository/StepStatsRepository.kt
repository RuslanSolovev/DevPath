package com.example.devpath.data.repository

import com.example.devpath.domain.models.LeaderboardEntry
import com.example.devpath.domain.models.StepStats
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StepStatsRepository @Inject constructor() {

    private val db: FirebaseFirestore = Firebase.firestore
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    // Кеш для данных пользователя
    private val userStatsCache = mutableMapOf<String, List<StepStats>>()

    // В методе saveStepStats добавьте обновление аватара в документ
    suspend fun saveStepStats(userId: String, userName: String, userAvatar: String?, steps: Int) {
        try {
            val today = dateFormat.format(Date())

            val stats = StepStats(
                userId = userId,
                userName = userName,
                userAvatar = userAvatar,
                steps = steps,
                date = today,
                timestamp = com.google.firebase.Timestamp.now()
            )
            db.collection("step_stats").document("${userId}_$today").set(stats).await()

            // Очищаем кеш после сохранения
            userStatsCache.remove(userId)

            // Обновляем общую статистику с аватаром
            updateUserTotalStats(userId, userName, userAvatar)

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    // Обновление аватаров для всех топов
    suspend fun updateAllAvatarsInLeaderboards() {
        try {
            // Обновляем аватары в step_stats
            val stepStatsSnapshot = db.collection("step_stats").get().await()
            for (doc in stepStatsSnapshot.documents) {
                val userId = doc.id.split("_")[0]
                val currentAvatar = doc.getString("userAvatar")

                if (currentAvatar.isNullOrEmpty()) {
                    val userProfile = db.collection("users").document(userId).get().await()
                    val avatarUrl = userProfile.getString("avatarUrl")
                    val userName = userProfile.getString("name") ?: "Пользователь"

                    if (!avatarUrl.isNullOrEmpty()) {
                        doc.reference.update(mapOf(
                            "userAvatar" to avatarUrl,
                            "userName" to userName
                        )).await()
                        println("Updated avatar in step_stats for user $userId: $avatarUrl")
                    }
                }
            }

            // Обновляем аватары в user_step_stats
            val userStatsSnapshot = db.collection("user_step_stats").get().await()
            for (doc in userStatsSnapshot.documents) {
                val userId = doc.id
                val currentAvatar = doc.getString("userAvatar")

                if (currentAvatar.isNullOrEmpty()) {
                    val userProfile = db.collection("users").document(userId).get().await()
                    val avatarUrl = userProfile.getString("avatarUrl")
                    val userName = userProfile.getString("name") ?: "Пользователь"

                    if (!avatarUrl.isNullOrEmpty()) {
                        doc.reference.update(mapOf(
                            "userAvatar" to avatarUrl,
                            "userName" to userName
                        )).await()
                        println("Updated avatar in user_step_stats for user $userId: $avatarUrl")
                    }
                }
            }
        } catch (e: Exception) {
            println("Error updating all avatars: ${e.message}")
        }
    }


    private suspend fun updateUserTotalStats(userId: String, userName: String, userAvatar: String?) {
        try {
            val allStats = getAllUserStats(userId)

            var totalSteps = 0
            var bestDay = 0

            for (stat in allStats) {
                totalSteps += stat.steps
                if (stat.steps > bestDay) {
                    bestDay = stat.steps
                }
            }

            val userStatsRef = db.collection("user_step_stats").document(userId)
            userStatsRef.set(mapOf(
                "userId" to userId,
                "userName" to userName,
                "userAvatar" to userAvatar,
                "totalSteps" to totalSteps,
                "bestDay" to bestDay,
                "lastUpdated" to com.google.firebase.Timestamp.now()
            ), com.google.firebase.firestore.SetOptions.merge()).await()

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Топ за все время - ОДИН РАЗ, без постоянного слушателя
    suspend fun getAllTimeLeaderboard(limit: Int = 10): List<LeaderboardEntry> {
        return try {
            val snapshot = db.collection("user_step_stats")
                .orderBy("totalSteps", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .get()
                .await()

            snapshot.documents.mapIndexed { index, doc ->
                LeaderboardEntry(
                    userId = doc.id,
                    userName = doc.getString("userName") ?: "Пользователь",
                    userAvatar = doc.getString("userAvatar"),
                    totalSteps = doc.getLong("totalSteps")?.toInt() ?: 0,
                    dailyAverage = 0,
                    streak = 0,
                    rank = index + 1
                )
            }
        } catch (e: Exception) {
            println("Error loading all time leaderboard: ${e.message}")
            emptyList()
        }
    }

    // Топ за сегодня
    // Топ за сегодня
    suspend fun getTodayLeaderboard(limit: Int = 10): List<LeaderboardEntry> {
        return try {
            val today = dateFormat.format(Date())
            val snapshot = db.collection("step_stats")
                .whereEqualTo("date", today)
                .get()
                .await()

            snapshot.documents
                .mapNotNull { doc ->
                    val userId = doc.id.split("_")[0]
                    var avatarUrl = doc.getString("userAvatar")
                    var userName = doc.getString("userName") ?: "Пользователь"

                    // Если аватар отсутствует, пробуем получить из профиля пользователя
                    if (avatarUrl.isNullOrEmpty()) {
                        val userProfile = db.collection("users").document(userId).get().await()
                        avatarUrl = userProfile.getString("avatarUrl")
                        userName = userProfile.getString("name") ?: userName

                        // Обновляем аватар в документе для будущих запросов
                        if (!avatarUrl.isNullOrEmpty()) {
                            doc.reference.update(mapOf(
                                "userAvatar" to avatarUrl,
                                "userName" to userName
                            )).await()
                        }
                    }

                    LeaderboardEntry(
                        userId = userId,
                        userName = userName,
                        userAvatar = avatarUrl,
                        totalSteps = doc.getLong("steps")?.toInt() ?: 0,
                        dailyAverage = 0,
                        streak = 0,
                        rank = 0
                    )
                }
                .sortedByDescending { it.totalSteps }
                .take(limit)
                .mapIndexed { index, entry -> entry.copy(rank = index + 1) }
        } catch (e: Exception) {
            println("Error loading today leaderboard: ${e.message}")
            emptyList()
        }
    }


    // Топ за неделю
    suspend fun getWeeklyLeaderboard(limit: Int = 10): List<LeaderboardEntry> {
        return try {
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)

            val dates = mutableListOf<String>()
            for (i in 0..6) {
                dates.add(dateFormat.format(calendar.time))
                calendar.add(Calendar.DAY_OF_YEAR, 1)
            }

            val weeklyData = mutableMapOf<String, LeaderboardEntry>()

            // Получаем все данные за неделю одним запросом
            val snapshot = db.collection("step_stats")
                .whereIn("date", dates)
                .get()
                .await()

            for (doc in snapshot.documents) {
                val userId = doc.id.split("_")[0]
                val steps = doc.getLong("steps")?.toInt() ?: 0
                var userName = doc.getString("userName") ?: "Пользователь"
                var userAvatar = doc.getString("userAvatar")

                // Если аватар отсутствует, пробуем получить из профиля пользователя
                if (userAvatar.isNullOrEmpty()) {
                    val userProfile = db.collection("users").document(userId).get().await()
                    userAvatar = userProfile.getString("avatarUrl")
                    userName = userProfile.getString("name") ?: userName

                    // Обновляем аватар в документе для будущих запросов
                    if (!userAvatar.isNullOrEmpty()) {
                        doc.reference.update(mapOf(
                            "userAvatar" to userAvatar,
                            "userName" to userName
                        )).await()
                    }
                }

                val existing = weeklyData[userId]
                if (existing != null) {
                    weeklyData[userId] = existing.copy(
                        totalSteps = existing.totalSteps + steps,
                        userAvatar = userAvatar ?: existing.userAvatar,
                        userName = userName
                    )
                } else {
                    weeklyData[userId] = LeaderboardEntry(
                        userId = userId,
                        userName = userName,
                        userAvatar = userAvatar,
                        totalSteps = steps,
                        dailyAverage = 0,
                        streak = 0,
                        rank = 0
                    )
                }
            }

            return weeklyData.values
                .sortedByDescending { it.totalSteps }
                .take(limit)
                .mapIndexed { index, entry -> entry.copy(rank = index + 1) }
        } catch (e: Exception) {
            println("Error loading weekly leaderboard: ${e.message}")
            emptyList()
        }
    }

    // Топ за месяц
    suspend fun getMonthlyLeaderboard(limit: Int = 10): List<LeaderboardEntry> {
        return try {
            val calendar = Calendar.getInstance()
            val currentMonth = calendar.get(Calendar.MONTH)
            val currentYear = calendar.get(Calendar.YEAR)
            calendar.set(Calendar.DAY_OF_MONTH, 1)

            val dates = mutableListOf<String>()
            while (calendar.get(Calendar.MONTH) == currentMonth && calendar.get(Calendar.YEAR) == currentYear) {
                dates.add(dateFormat.format(calendar.time))
                calendar.add(Calendar.DAY_OF_MONTH, 1)
            }

            val monthlyData = mutableMapOf<String, LeaderboardEntry>()

            // Разбиваем на части по 30 дат (ограничение Firestore)
            dates.chunked(30).forEach { chunk ->
                val snapshot = db.collection("step_stats")
                    .whereIn("date", chunk)
                    .get()
                    .await()

                for (doc in snapshot.documents) {
                    val userId = doc.id.split("_")[0]
                    val steps = doc.getLong("steps")?.toInt() ?: 0
                    var userName = doc.getString("userName") ?: "Пользователь"
                    var userAvatar = doc.getString("userAvatar")

                    // Если аватар отсутствует, пробуем получить из профиля пользователя
                    if (userAvatar.isNullOrEmpty()) {
                        val userProfile = db.collection("users").document(userId).get().await()
                        userAvatar = userProfile.getString("avatarUrl")
                        userName = userProfile.getString("name") ?: userName

                        // Обновляем аватар в документе для будущих запросов
                        if (!userAvatar.isNullOrEmpty()) {
                            doc.reference.update(mapOf(
                                "userAvatar" to userAvatar,
                                "userName" to userName
                            )).await()
                        }
                    }

                    val existing = monthlyData[userId]
                    if (existing != null) {
                        monthlyData[userId] = existing.copy(
                            totalSteps = existing.totalSteps + steps,
                            userAvatar = userAvatar ?: existing.userAvatar,
                            userName = userName
                        )
                    } else {
                        monthlyData[userId] = LeaderboardEntry(
                            userId = userId,
                            userName = userName,
                            userAvatar = userAvatar,
                            totalSteps = steps,
                            dailyAverage = 0,
                            streak = 0,
                            rank = 0
                        )
                    }
                }
            }

            return monthlyData.values
                .sortedByDescending { it.totalSteps }
                .take(limit)
                .mapIndexed { index, entry -> entry.copy(rank = index + 1) }
        } catch (e: Exception) {
            println("Error loading monthly leaderboard: ${e.message}")
            emptyList()
        }
    }

    // Получаем все данные пользователя с кешированием
    private suspend fun getAllUserStats(userId: String): List<StepStats> {
        userStatsCache[userId]?.let { return it }

        val snapshot = db.collection("step_stats")
            .whereEqualTo("userId", userId)
            .get()
            .await()

        val stats = snapshot.documents.mapNotNull { doc ->
            doc.toObject(StepStats::class.java)
        }

        userStatsCache[userId] = stats
        return stats
    }

    // Flow для недельной статистики в реальном времени
    fun observeWeeklyStats(userId: String): kotlinx.coroutines.flow.Flow<Map<String, Int>> = kotlinx.coroutines.flow.callbackFlow {
        val subscription = db.collection("step_stats")
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val stats = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(StepStats::class.java)
                } ?: emptyList()

                userStatsCache[userId] = stats
                val weeklyStats = calculateWeeklyStats(stats)
                trySend(weeklyStats)
            }

        awaitClose { subscription.remove() }
    }

    private fun calculateWeeklyStats(allStats: List<StepStats>): Map<String, Int> {
        val result = mutableMapOf<String, Int>()
        val calendar = Calendar.getInstance()
        val days = listOf("ПН", "ВТ", "СР", "ЧТ", "ПТ", "СБ", "ВС")

        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)

        for (i in 0..6) {
            val date = dateFormat.format(calendar.time)
            val steps = allStats.find { it.date == date }?.steps ?: 0
            result[days[i]] = steps
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        return result
    }

    suspend fun getWeeklyTotal(userId: String): Int {
        val allStats = getAllUserStats(userId)
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)

        var total = 0
        for (i in 0..6) {
            val date = dateFormat.format(calendar.time)
            val steps = allStats.find { it.date == date }?.steps ?: 0
            total += steps
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }
        return total
    }

    suspend fun getMonthlyTotal(userId: String): Int {
        val allStats = getAllUserStats(userId)
        val calendar = Calendar.getInstance()
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentYear = calendar.get(Calendar.YEAR)

        var total = 0
        for (stat in allStats) {
            val statCalendar = Calendar.getInstance().apply {
                time = dateFormat.parse(stat.date) ?: Date()
            }
            if (statCalendar.get(Calendar.MONTH) == currentMonth &&
                statCalendar.get(Calendar.YEAR) == currentYear) {
                total += stat.steps
            }
        }
        return total
    }

    suspend fun getYearlyTotal(userId: String): Int {
        val allStats = getAllUserStats(userId)
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)

        return allStats.filter { stat ->
            val statCalendar = Calendar.getInstance().apply {
                time = dateFormat.parse(stat.date) ?: Date()
            }
            statCalendar.get(Calendar.YEAR) == currentYear
        }.sumOf { it.steps }
    }

    suspend fun updateMissingAvatars() {
        try {
            val leaderboardSnapshot = db.collection("user_step_stats").get().await()

            for (doc in leaderboardSnapshot.documents) {
                val userId = doc.id
                val currentAvatar = doc.getString("userAvatar")

                if (currentAvatar.isNullOrEmpty()) {
                    val userProfile = db.collection("users").document(userId).get().await()
                    val avatarUrl = userProfile.getString("avatarUrl")
                    val userName = userProfile.getString("name") ?: "Пользователь"

                    if (!avatarUrl.isNullOrEmpty()) {
                        doc.reference.update(mapOf(
                            "userAvatar" to avatarUrl,
                            "userName" to userName
                        )).await()
                        println("Updated avatar for user $userId: $avatarUrl")
                    }
                }
            }
        } catch (e: Exception) {
            println("Error updating missing avatars: ${e.message}")
        }
    }

    suspend fun updateUserAvatar(userId: String) {
        try {
            val userProfile = db.collection("users").document(userId).get().await()
            val avatarUrl = userProfile.getString("avatarUrl")
            val userName = userProfile.getString("name") ?: "Пользователь"

            if (!avatarUrl.isNullOrEmpty()) {
                val userStatsRef = db.collection("user_step_stats").document(userId)
                userStatsRef.update(mapOf(
                    "userAvatar" to avatarUrl,
                    "userName" to userName
                )).await()
                println("Updated avatar for user $userId: $avatarUrl")
            }
        } catch (e: Exception) {
            println("Error updating avatar for user $userId: ${e.message}")
        }
    }

    suspend fun getTodaySteps(userId: String): Int {
        val today = dateFormat.format(Date())
        val allStats = getAllUserStats(userId)
        return allStats.find { it.date == today }?.steps ?: 0
    }

    suspend fun getTotalSteps(userId: String): Int {
        val allStats = getAllUserStats(userId)
        return allStats.sumOf { it.steps }
    }
}