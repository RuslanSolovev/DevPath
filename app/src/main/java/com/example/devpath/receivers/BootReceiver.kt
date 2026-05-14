package com.example.devpath.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.devpath.services.StepCounterService

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (Intent.ACTION_BOOT_COMPLETED == intent.action) {
            Log.d("BootReceiver", "Устройство загружено, запускаем StepCounterService")

            val serviceIntent = Intent(context, StepCounterService::class.java)

            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(serviceIntent)
                } else {
                    context.startService(serviceIntent)
                }
                Log.d("BootReceiver", "StepCounterService успешно запущен")
            } catch (e: Exception) {
                Log.e("BootReceiver", "Ошибка запуска StepCounterService: ${e.message}")
            }
        }
    }
}