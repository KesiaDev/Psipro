package com.example.psipro.data.converters

import androidx.room.TypeConverter
import com.example.psipro.data.entities.AnamneseGroup

class AnamneseGroupConverter {
    @TypeConverter
    fun fromAnamneseGroup(value: AnamneseGroup): String = value.name
    
    @TypeConverter
    fun toAnamneseGroup(value: String?): AnamneseGroup {
        return try {
            if (value == null || value.isBlank()) {
                return AnamneseGroup.ADULTO
            }
            AnamneseGroup.valueOf(value)
        } catch (e: Exception) {
            AnamneseGroup.ADULTO
        }
    }
} 