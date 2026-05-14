package com.example.devpath.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.devpath.data.repository.YdbRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@HiltWorker
class StepSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val ydbRepository: YdbRepository
) : CoroutineWorker(context, params) {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    override suspend fun doWork(): Result {
        return try {
            println("DEBUG: StepSyncWorker - начинаю синхронизацию шагов")

            val stepPrefs = applicationContext.getSharedPreferences(
                "step_counter",
                Context.MODE_PRIVATE
            )
            val userPrefs = applicationContext.getSharedPreferences(
                "user_prefs",
                Context.MODE_PRIVATE
            )

            val todaySteps = stepPrefs.getInt("today_steps", 0)
            val today = dateFormat.format(Date())

            val userId = userPrefs.getString("user_id", "") ?: ""
            val userName = userPrefs.getString("user_name", "Пользователь") ?: "Пользователь"

            if (userId.isEmpty()) {
                println("DEBUG: StepSyncWorker - пользователь не авторизован, пропускаем синхронизацию")
                return Result.success()
            }

            if (todaySteps <= 0) {
                println("DEBUG: StepSyncWorker - нет шагов для синхронизации (todaySteps=$todaySteps)")
                return Result.success()
            }

            // Получаем аватар пользователя из YDB
            val userData = ydbRepository.getUser(userId)
            val avatarUrl = userData?.optJSONObject("avatar_url")
                ?.optString("S", "")?.ifEmpty { null }
            val displayName = userData?.optJSONObject("name")
                ?.optString("S", userName) ?: userName

            val stepId = "$userId`_`$today"

            val item = JSONObject().apply {
                put("step_id", JSONObject().put("S", stepId))
                put("user_id", JSONObject().put("S", userId))
                put("user_name", JSONObject().put("S", displayName))
                put("user_avatar", JSONObject().put("S", avatarUrl ?: ""))
                put("steps", JSONObject().put("N", todaySteps.toString()))
                put("date", JSONObject().put("S", today))
                put(
                    "updated_at",
                    JSONObject().put("S", System.currentTimeMillis().toString())
                )
            }

            val body = JSONObject().apply {
                put("TableName", "steps_doc")
                put("Item", item)
            }

            val result = ydbRepository.executeSignedRequest("PutItem", body)

            if (result != null) {
                println("DEBUG: StepSyncWorker - ✅ шаги синхронизированы: userId=$userId, steps=$todaySteps, date=$today")
                Result.success()
            } else {
                println("DEBUG: StepSyncWorker - ⚠️ ошибка синхронизации шагов (result=null)")
                Result.retry()
            }
        } catch (e: Exception) {
            println("DEBUG: StepSyncWorker - ❌ исключение: ${e.message}")
            e.printStackTrace()
            Result.retry()
        }
    }
}