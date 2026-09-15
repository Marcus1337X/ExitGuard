package com.exitguard.app.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.exitguard.app.model.AppRule
import com.exitguard.app.model.CheckMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.IOException

val Context.exitGuardDataStore: DataStore<Preferences> by preferencesDataStore(name = "exit_guard_rules")

class RuleRepository(private val context: Context) {

    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = false
        encodeDefaults = true
    }

    companion object {
        private val RULES_KEY = stringPreferencesKey("rules_json")
    }

    val rulesFlow: Flow<List<AppRule>> = context.exitGuardDataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val jsonString = preferences[RULES_KEY] ?: return@map emptyList()
            try {
                json.decodeFromString<List<AppRule>>(jsonString)
            } catch (e: Exception) {
                emptyList()
            }
        }

    suspend fun getRules(): List<AppRule> {
        return rulesFlow.first()
    }

    suspend fun getRule(packageName: String): AppRule? {
        return getRules().firstOrNull { it.packageName == packageName }
    }

    suspend fun saveRule(rule: AppRule) {
        context.exitGuardDataStore.edit { preferences ->
            val currentList = try {
                preferences[RULES_KEY]?.let { json.decodeFromString<List<AppRule>>(it) } ?: emptyList()
            } catch (e: Exception) {
                emptyList()
            }

            val updatedList = currentList.filter { it.packageName != rule.packageName } + rule
            preferences[RULES_KEY] = json.encodeToString(updatedList)
        }
    }

    suspend fun addApps(apps: List<InstalledApp>) {
        if (apps.isEmpty()) return

        context.exitGuardDataStore.edit { preferences ->
            val currentList = try {
                preferences[RULES_KEY]?.let { json.decodeFromString<List<AppRule>>(it) } ?: emptyList()
            } catch (e: Exception) {
                emptyList()
            }

            val existingPackages = currentList.map { it.packageName }.toSet()
            val newRules = apps
                .filter { it.packageName !in existingPackages }
                .map { app ->
                    AppRule(
                        packageName = app.packageName,
                        appName = app.appName,
                        mode = CheckMode.IP_STRICT,
                        allowedIps = emptySet(),
                        allowedCountries = emptySet()
                    )
                }

            val combined = currentList + newRules
            preferences[RULES_KEY] = json.encodeToString(combined)
        }
    }

    suspend fun deleteRule(packageName: String) {
        context.exitGuardDataStore.edit { preferences ->
            val currentList = try {
                preferences[RULES_KEY]?.let { json.decodeFromString<List<AppRule>>(it) } ?: emptyList()
            } catch (e: Exception) {
                emptyList()
            }

            val updatedList = currentList.filter { it.packageName != packageName }
            preferences[RULES_KEY] = json.encodeToString(updatedList)
        }
    }
}
