package com.patamar.app.data.model

data class FilterPreferences(
    val categories: Set<EventCategory> = emptySet(),
    val radiusMeters: Int = 3000
)
