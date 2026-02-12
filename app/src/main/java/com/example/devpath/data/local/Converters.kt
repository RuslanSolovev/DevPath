// data/local/Converters.kt
package com.example.devpath.data.local

import androidx.room.TypeConverter
import java.util.Date

class Converters {

    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    @TypeConverter
    fun fromStringList(value: List<String>?): String {
        return value?.joinToString("␟") ?: ""
    }

    @TypeConverter
    fun toStringList(value: String): List<String> {
        return if (value.isBlank()) emptyList() else value.split("␟")
    }

    @TypeConverter
    fun fromLongList(value: List<Long>?): String {
        return value?.joinToString("␟") ?: ""
    }

    @TypeConverter
    fun toLongList(value: String): List<Long> {
        return if (value.isBlank()) emptyList() else value.split("␟").map { it.toLong() }
    }

    @TypeConverter
    fun fromBoolean(value: Boolean): Int {
        return if (value) 1 else 0
    }

    @TypeConverter
    fun toBoolean(value: Int): Boolean {
        return value == 1
    }
}