package com.example.quranapp.data

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class Repository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {

    private val tag = "repository"

    // Shared preferences
    object PreferencesKeys {
        val FIRST_LAUNCH = booleanPreferencesKey("first_launch")
    }

    suspend fun isFirstLaunch(): Boolean {
        Log.d(tag, "isFirstLaunch is called")
        val preferencesFlow = dataStore.data.first()
        return preferencesFlow[PreferencesKeys.FIRST_LAUNCH] ?: true
    }

    suspend fun setFirstLaunch(isFirstLaunch: Boolean) {
        Log.d(tag, "setFirstLaunch is called")
        dataStore.edit {preferences ->
            preferences[PreferencesKeys.FIRST_LAUNCH] = isFirstLaunch
        }
    }
}