package com.example.psipro.data.converters

import androidx.room.TypeConverter
import org.json.JSONObject

object StringMapConverter {
    @TypeConverter
    @JvmStatic
    fun fromString(value: String?): Map<String, String> {
        return value?.let {
            val json = JSONObject(it)
            json.keys().asSequence().associateWith { key -> json.getString(key) }
        } ?: emptyMap()
    }

    @TypeConverter
    @JvmStatic
    fun mapToString(map: Map<String, String>?): String {
        val json = JSONObject()
        (map ?: emptyMap()).forEach { (key, value) -> json.put(key, value) }
        return json.toString()
    }
} 