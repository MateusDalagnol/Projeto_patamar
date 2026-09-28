package com.patamar.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import java.time.LocalDate
import java.time.LocalTime

@Entity(tableName = "events")
@TypeConverters(EventTypeConverters::class)
data class Event(
    @PrimaryKey val id: String,
    val name: String,
    val category: EventCategory,
    val description: String,
    val lat: Double,
    val lng: Double,
    val address: String,
    val date: LocalDate,
    val time: LocalTime,
    val distanceMeters: Int,
    val isFree: Boolean,
    val isHighlighted: Boolean,
    val imageUrl: String
)

class EventTypeConverters {
    @TypeConverter
    fun fromCategory(category: EventCategory): String = category.name

    @TypeConverter
    fun toCategory(value: String): EventCategory = EventCategory.valueOf(value)

    @TypeConverter
    fun fromLocalDate(date: LocalDate): String = date.toString()

    @TypeConverter
    fun toLocalDate(value: String): LocalDate = LocalDate.parse(value)

    @TypeConverter
    fun fromLocalTime(time: LocalTime): String = time.toString()

    @TypeConverter
    fun toLocalTime(value: String): LocalTime = LocalTime.parse(value)
}
