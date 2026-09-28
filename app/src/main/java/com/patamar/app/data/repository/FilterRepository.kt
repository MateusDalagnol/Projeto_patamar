package com.patamar.app.data.repository

import com.patamar.app.core.security.EncryptedPrefsManager
import com.patamar.app.data.model.FilterPreferences
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FilterRepository @Inject constructor(
    private val prefsManager: EncryptedPrefsManager
) {
    fun getFilters(): FilterPreferences = prefsManager.getFilters()

    fun saveFilters(filters: FilterPreferences) = prefsManager.saveFilters(filters)
}
