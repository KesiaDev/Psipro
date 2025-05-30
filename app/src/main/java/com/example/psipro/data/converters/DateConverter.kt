package com.example.psipro.data.converters

import androidx.room.TypeConverter
import java.util.Date
import com.example.psipro.data.entities.RecurrenceType

class DateConverter {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    // --- RecurrenceType Converters ---
    @TypeConverter
    fun fromRecurrenceType(value: String?): RecurrenceType? {
        return value?.let { RecurrenceType.valueOf(it) }
    }

    @TypeConverter
    fun recurrenceTypeToString(type: RecurrenceType?): String? {
        return type?.name
    }
} 