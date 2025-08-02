package com.psipro.app.data.converters

import androidx.room.TypeConverter
import com.psipro.app.data.entities.AnamneseGroup

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



