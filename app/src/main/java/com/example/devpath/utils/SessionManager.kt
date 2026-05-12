package com.example.devpath.utils

import android.content.Context
import android.content.SharedPreferences

object SessionManager {
    private const val PREFS_NAME = "user_prefs"
    private const val KEY_USER_ID = "user_id"

    private var prefs: SharedPreferences? = null

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun getUserId(): String? {
        return prefs?.getString(KEY_USER_ID, null)
    }

    fun isLoggedIn(): Boolean {
        return prefs?.getBoolean("is_logged_in", false) ?: false
    }

    fun clearSession() {
        prefs?.edit()?.clear()?.apply()
    }
}