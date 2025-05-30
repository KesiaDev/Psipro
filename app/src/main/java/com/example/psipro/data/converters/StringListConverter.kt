package com.example.psipro.data.converters

import androidx.room.TypeConverter

object StringListConverter {
    @TypeConverter
    @JvmStatic
    fun fromString(value: String?): List<String> {
        return value?.split(",")?.map { it.trim() } ?: emptyList()
    }

    @TypeConverter
    @JvmStatic
    fun listToString(list: List<String>?): String {
        return list?.joinToString(",") ?: ""
    }
} 