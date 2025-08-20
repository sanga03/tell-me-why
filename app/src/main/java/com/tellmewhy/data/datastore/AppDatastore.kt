package com.tellmewhy.data.datastore // Or your preferred package

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.IOException
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking // Use with caution, see notes

// Define a top-level property for the DataStore instance
// The name "overlay_timestamps_prefs" will be the filename for the DataStore
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "overlay_cooldown_prefs")

object AppSettingsDataStore {

    private val LAST_OVERLAY_TIMES_KEY = stringPreferencesKey("last_overlay_times_json")
    val OPENROUTER_API_KEY = stringPreferencesKey("openrouter_api_key") // New key for API
    val COOLDOWN_MINUTES_KEY = intPreferencesKey("cooldown_minutes") // New key
    private val GSON = Gson()
    const val DEFAULT_COOLDOWN_MINUTES = 2 // Default value
    // --- Suspending functions for use in coroutines ---

    suspend fun saveOverlayTimestamps(context: Context, timestamps: Map<String, Long>) {
        context.dataStore.edit { preferences ->
            val json = GSON.toJson(timestamps)
            preferences[LAST_OVERLAY_TIMES_KEY] = json
        }
    }

    fun getOverlayTimestampsFlow(context: Context): Flow<Map<String, Long>> {
        return context.dataStore.data.map { preferences ->
            val json = preferences[LAST_OVERLAY_TIMES_KEY]
            if (json != null) {
                try {
                    val type = object : TypeToken<MutableMap<String, Long>>() {}.type
                    GSON.fromJson(json, type) ?: emptyMap()
                } catch (e: Exception) {
                    // Log error
                    emptyMap()
                }
            } else {
                emptyMap()
            }
        }
    }

    suspend fun getOverlayTimestampsOnce(context: Context): Map<String, Long> {
        return getOverlayTimestampsFlow(context).firstOrNull() ?: emptyMap()
    }


    // --- Blocking functions for use in non-coroutine contexts (like AccessibilityService's main thread methods) ---
    // Be cautious with these as they block the calling thread.
    // Prefer using Flow and collecting in a coroutine scope if possible.

    fun saveOverlayTimestampsBlocking(context: Context, timestamps: Map<String, Long>) {
        runBlocking { // This will block the thread until the DataStore operation is complete
            saveOverlayTimestamps(context, timestamps)
        }
    }

    fun getOverlayTimestampsBlocking(context: Context): Map<String, Long> {
        return runBlocking { // This will block the thread
            getOverlayTimestampsOnce(context)
        }
    }

    suspend fun saveOpenRouterApiKey(context: Context, apiKey: String) {
        context.dataStore.edit { preferences ->
            preferences[OPENROUTER_API_KEY] = apiKey
        }
    }

    fun getOpenRouterApiKeyFlow(context: Context): Flow<String?> {
        return context.dataStore.data
            .map { preferences ->
                preferences[OPENROUTER_API_KEY] // Can be null if not set
            }
    }

    suspend fun getOpenRouterApiKeyOnce(context: Context): String? {
        return getOpenRouterApiKeyFlow(context).firstOrNull()
    }

    fun saveOpenRouterApiKeyBlocking(context: Context, apiKey: String) {
        runBlocking {
            saveOpenRouterApiKey(context, apiKey)
        }
    }

    fun getOpenRouterApiKeyBlocking(context: Context): String? {
        return runBlocking {
            getOpenRouterApiKeyOnce(context)
        }
    }

    suspend fun saveCooldownTime(context: Context, minutes: Int) {
        context.dataStore.edit { preferences ->
            preferences[COOLDOWN_MINUTES_KEY] = minutes
        }
    }

    fun getCooldownTimeFlow(context: Context): Flow<Int> {
        return context.dataStore.data

            .map { preferences ->
                // Provide a default value if not set
                preferences[COOLDOWN_MINUTES_KEY] ?: DEFAULT_COOLDOWN_MINUTES
            }
    }

    suspend fun getCooldownTimeOnce(context: Context): Int {
        return getCooldownTimeFlow(context).firstOrNull() ?: DEFAULT_COOLDOWN_MINUTES
    }

    fun saveCooldownTimeBlocking(context: Context, minutes: Int) {
        runBlocking {
            saveCooldownTime(context, minutes)
        }
    }

    fun getCooldownTimeBlocking(context: Context): Int {
        return runBlocking {
            getCooldownTimeOnce(context)
        }
    }
}

