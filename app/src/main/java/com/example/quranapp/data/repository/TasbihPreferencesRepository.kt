package com.example.quranapp.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.quranapp.presentation.screen.tasbih.TasbihSettings
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for Tasbih preferences using DataStore
 */
@Singleton
class TasbihPreferencesRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    companion object {
        private val KEY_SELECTED_PRESET_ID = stringPreferencesKey("tasbih_selected_preset_id")
        private val KEY_CURRENT_COUNT = intPreferencesKey("tasbih_current_count")
        private val KEY_TARGET_COUNT = intPreferencesKey("tasbih_target_count")
        private val KEY_HAPTIC_FEEDBACK = booleanPreferencesKey("tasbih_haptic_feedback")
        private val KEY_SOUND_FEEDBACK = booleanPreferencesKey("tasbih_sound_feedback")
        private val KEY_AUTO_RESET = booleanPreferencesKey("tasbih_auto_reset")
    }

    /**
     * Get selected preset ID
     */
    suspend fun getSelectedPresetId(): String? {
        return dataStore.data.map { preferences ->
            preferences[KEY_SELECTED_PRESET_ID]
        }.firstOrNull()
    }

    /**
     * Set selected preset ID
     */
    suspend fun setSelectedPresetId(presetId: String) {
        dataStore.edit { preferences ->
            preferences[KEY_SELECTED_PRESET_ID] = presetId
        }
    }

    /**
     * Get current count
     */
    suspend fun getCurrentCount(): Int {
        return dataStore.data.map { preferences ->
            preferences[KEY_CURRENT_COUNT] ?: 0
        }.firstOrNull() ?: 0
    }

    /**
     * Set current count
     */
    suspend fun setCurrentCount(count: Int) {
        dataStore.edit { preferences ->
            preferences[KEY_CURRENT_COUNT] = count
        }
    }

    /**
     * Get target count
     */
    suspend fun getTargetCount(): Int {
        return dataStore.data.map { preferences ->
            preferences[KEY_TARGET_COUNT] ?: 33
        }.firstOrNull() ?: 33
    }

    /**
     * Set target count
     */
    suspend fun setTargetCount(target: Int) {
        dataStore.edit { preferences ->
            preferences[KEY_TARGET_COUNT] = target
        }
    }

    /**
     * Get Tasbih settings
     */
    suspend fun getSettings(): TasbihSettings {
        return dataStore.data.map { preferences ->
            TasbihSettings(
                hapticFeedback = preferences[KEY_HAPTIC_FEEDBACK] ?: true,
                soundFeedback = preferences[KEY_SOUND_FEEDBACK] ?: false,
                autoReset = preferences[KEY_AUTO_RESET] ?: false
            )
        }.firstOrNull() ?: TasbihSettings()
    }

    /**
     * Set Tasbih settings
     */
    suspend fun setSettings(settings: TasbihSettings) {
        dataStore.edit { preferences ->
            preferences[KEY_HAPTIC_FEEDBACK] = settings.hapticFeedback
            preferences[KEY_SOUND_FEEDBACK] = settings.soundFeedback
            preferences[KEY_AUTO_RESET] = settings.autoReset
        }
    }

    /**
     * Clear all preferences
     */
    suspend fun clearAll() {
        dataStore.edit { preferences ->
            preferences.remove(KEY_SELECTED_PRESET_ID)
            preferences.remove(KEY_CURRENT_COUNT)
            preferences.remove(KEY_TARGET_COUNT)
            preferences.remove(KEY_HAPTIC_FEEDBACK)
            preferences.remove(KEY_SOUND_FEEDBACK)
            preferences.remove(KEY_AUTO_RESET)
        }
    }
}
