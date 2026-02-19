package com.example.devpath.utils

import android.app.Application
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppStateManager @Inject constructor(
    application: Application
) {
    private var activeActivities = 0
    private var isAppInBackground = false

    fun onActivityStarted() {
        activeActivities++
        if (activeActivities == 1) {
            isAppInBackground = false
            println("DEBUG: AppState - приложение в foreground")
        }
    }

    fun onActivityStopped() {
        activeActivities--
        if (activeActivities == 0) {
            isAppInBackground = true
            println("DEBUG: AppState - приложение в background")
        }
    }

    fun isAppInBackground(): Boolean = isAppInBackground
}