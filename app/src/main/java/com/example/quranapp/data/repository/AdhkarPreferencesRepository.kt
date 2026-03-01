package com.example.quranapp.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for Adhkar preferences using DataStore
 * Stores total score and progress for each category
 */
@Singleton
class AdhkarPreferencesRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    companion object {
        private val KEY_TOTAL_SCORE = intPreferencesKey("adhkar_total_score")

        // Helper function to create progress key for a specific category and dhikr index
        private fun progressKey(categoryFileName: String, dhikrIndex: Int): Preferences.Key<Int> {
            return intPreferencesKey("adhkar_progress_${categoryFileName}_$dhikrIndex")
        }

        // Helper function to create completion status key for a specific category
        private fun categoryCompletionKey(categoryFileName: String): Preferences.Key<String> {
            return stringPreferencesKey("adhkar_completion_$categoryFileName")
        }
    }

    /**
     * Get total score as Flow
     */
    fun getTotalScore(): Flow<Int> {
        return dataStore.data.map { preferences ->
            preferences[KEY_TOTAL_SCORE] ?: 0
        }
    }

    /**
     * Get total score (one-time read)
     */
    suspend fun getTotalScoreOnce(): Int {
        return dataStore.data.map { preferences ->
            preferences[KEY_TOTAL_SCORE] ?: 0
        }.firstOrNull() ?: 0
    }

    /**
     * Add score to the total
     */
    suspend fun addScore(points: Int) {
        dataStore.edit { preferences ->
            val currentScore = preferences[KEY_TOTAL_SCORE] ?: 0
            preferences[KEY_TOTAL_SCORE] = currentScore + points
        }
    }

    /**
     * Set total score
     */
    suspend fun setTotalScore(score: Int) {
        dataStore.edit { preferences ->
            preferences[KEY_TOTAL_SCORE] = score
        }
    }

    /**
     * Get progress for a specific dhikr in a category
     */
    suspend fun getDhikrProgress(categoryFileName: String, dhikrIndex: Int): Int {
        return dataStore.data.map { preferences ->
            preferences[progressKey(categoryFileName, dhikrIndex)] ?: 0
        }.firstOrNull() ?: 0
    }

    /**
     * Set progress for a specific dhikr in a category
     */
    suspend fun setDhikrProgress(categoryFileName: String, dhikrIndex: Int, progress: Int) {
        dataStore.edit { preferences ->
            preferences[progressKey(categoryFileName, dhikrIndex)] = progress
        }
    }

    /**
     * Get all progress for a category
     */
    suspend fun getCategoryProgress(categoryFileName: String, dhikrCount: Int): Map<Int, Int> {
        return dataStore.data.map { preferences ->
            (0 until dhikrCount).associateWith { index ->
                preferences[progressKey(categoryFileName, index)] ?: 0
            }
        }.firstOrNull() ?: emptyMap()
    }

    /**
     * Reset all progress for a specific category
     */
    suspend fun resetCategoryProgress(categoryFileName: String, dhikrCount: Int) {
        dataStore.edit { preferences ->
            (0 until dhikrCount).forEach { index ->
                preferences.remove(progressKey(categoryFileName, index))
            }
        }
    }

    /**
     * Check if category is completed
     */
    suspend fun isCategoryCompleted(categoryFileName: String): Boolean {
        return dataStore.data.map { preferences ->
            preferences[categoryCompletionKey(categoryFileName)] == "completed"
        }.firstOrNull() ?: false
    }

    /**
     * Mark category as completed
     */
    suspend fun markCategoryCompleted(categoryFileName: String) {
        dataStore.edit { preferences ->
            preferences[categoryCompletionKey(categoryFileName)] = "completed"
        }
    }

    /**
     * Clear category completion status
     */
    suspend fun clearCategoryCompletion(categoryFileName: String) {
        dataStore.edit { preferences ->
            preferences.remove(categoryCompletionKey(categoryFileName))
        }
    }

    /**
     * Reset all adhkar data (for testing or user reset)
     */
    suspend fun resetAllData() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
