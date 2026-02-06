package com.example.devpath.data.local.converter

import androidx.room.TypeConverter
import com.example.devpath.domain.models.GeneralTestResult
import com.google.gson.Gson

class GeneralTestResultTypeConverter {
    private val gson = Gson()

    @TypeConverter
    fun fromGeneralTestResultList(value: List<GeneralTestResult>?): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toGeneralTestResultList(value: String): List<GeneralTestResult> {
        val listType = object : com.google.common.reflect.TypeToken<List<GeneralTestResult>>() {}.type
        return gson.fromJson(value, listType) ?: emptyList()
    }
}