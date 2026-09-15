package com.exitguard.app.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException

class SettingsRepository(private val context: Context) {

    companion object {
        const val DEFAULT_PRIMARY_API = "https://ipwho.is/"
        const val DEFAULT_FALLBACK_API = "https://api.ip.sb/geoip"

        val PRESET_APIS = listOf(
            "https://ipwho.is/",
            "https://api.ip.sb/geoip",
            "https://ipapi.co/json/"
        )

        private val PRIMARY_API_KEY = stringPreferencesKey("primary_api_url")
        private val CUSTOM_APIS_KEY = stringSetPreferencesKey("custom_api_urls")
    }

    val primaryApiFlow: Flow<String> = context.exitGuardDataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences ->
            preferences[PRIMARY_API_KEY] ?: DEFAULT_PRIMARY_API
        }

    val allApisFlow: Flow<List<String>> = context.exitGuardDataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences ->
            val custom = preferences[CUSTOM_APIS_KEY] ?: emptySet()
            val combined = (PRESET_APIS + custom).distinct()
            combined
        }

    suspend fun getPrimaryApi(): String {
        return primaryApiFlow.first()
    }

    suspend fun setPrimaryApi(url: String) {
        val trimmed = url.trim()
        if (trimmed.isEmpty()) return
        context.exitGuardDataStore.edit { preferences ->
            preferences[PRIMARY_API_KEY] = trimmed
            val custom = preferences[CUSTOM_APIS_KEY] ?: emptySet()
            if (!PRESET_APIS.contains(trimmed)) {
                preferences[CUSTOM_APIS_KEY] = custom + trimmed
            }
        }
    }

    suspend fun addCustomApi(url: String) {
        val trimmed = url.trim()
        if (trimmed.isEmpty()) return
        context.exitGuardDataStore.edit { preferences ->
            val custom = preferences[CUSTOM_APIS_KEY] ?: emptySet()
            preferences[CUSTOM_APIS_KEY] = custom + trimmed
        }
    }

    suspend fun removeCustomApi(url: String) {
        val trimmed = url.trim()
        context.exitGuardDataStore.edit { preferences ->
            val custom = preferences[CUSTOM_APIS_KEY] ?: emptySet()
            preferences[CUSTOM_APIS_KEY] = custom - trimmed
            if (preferences[PRIMARY_API_KEY] == trimmed) {
                preferences[PRIMARY_API_KEY] = DEFAULT_PRIMARY_API
            }
        }
    }

    suspend fun resetToDefault() {
        context.exitGuardDataStore.edit { preferences ->
            preferences[PRIMARY_API_KEY] = DEFAULT_PRIMARY_API
        }
    }
}
