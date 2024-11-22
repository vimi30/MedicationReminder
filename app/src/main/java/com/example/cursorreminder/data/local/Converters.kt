package com.example.cursorreminder.data.local

import androidx.room.TypeConverter
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class Converters {
    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    private val dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    @TypeConverter
    fun fromLocalDateTime(value: LocalDateTime?): String? {
        return value?.format(dateTimeFormatter)
    }

    @TypeConverter
    fun toLocalDateTime(value: String?): LocalDateTime? {
        return value?.let { LocalDateTime.parse(it, dateTimeFormatter) }
    }

    @TypeConverter
    fun fromLocalDateSet(dates: Set<LocalDate>): String {
        return dates.joinToString(",") { it.format(dateFormatter) }
    }

    @TypeConverter
    fun toLocalDateSet(value: String): Set<LocalDate> {
        return if (value.isEmpty()) {
            emptySet()
        } else {
            value.split(",").map { LocalDate.parse(it, dateFormatter) }.toSet()
        }
    }

    @TypeConverter
    fun fromScheduledDays(value: String): List<DayOfWeek> {
        if (value.isEmpty()) return emptyList()
        return value.split(",").map { DayOfWeek.of(it.toInt()) }
    }

    @TypeConverter
    fun toScheduledDays(days: List<DayOfWeek>): String {
        return days.joinToString(",") { it.value.toString() }
    }
} 