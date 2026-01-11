package com.example.quranapp.util

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.edit
import androidx.core.os.LocaleListCompat

object LanguageManager {
    private const val PREF_NAME = "app_preferences"
    private const val KEY_LANGUAGE = "selected_language"

    const val LANGUAGE_SYSTEM = "system"
    const val LANGUAGE_ENGLISH = "en"
    const val LANGUAGE_ARABIC = "ar"

    fun setLanguage(context: Context, languageCode: String) {
        // Save preference
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit {
            putString(KEY_LANGUAGE, languageCode)
        }

        // Apply language
        applyLanguage(languageCode)
    }

    fun getCurrentLanguage(context: Context): String {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_LANGUAGE, LANGUAGE_SYSTEM) ?: LANGUAGE_SYSTEM
    }

    private fun applyLanguage(languageCode: String) {
        val localeList = when (languageCode) {
            LANGUAGE_SYSTEM -> LocaleListCompat.getEmptyLocaleList()
            else -> LocaleListCompat.forLanguageTags(languageCode)
        }
        AppCompatDelegate.setApplicationLocales(localeList)
    }


    fun getLanguageDisplayName(languageCode: String): String {
        return when (languageCode) {
            LANGUAGE_SYSTEM -> "System Default"
            LANGUAGE_ENGLISH -> "English"
            LANGUAGE_ARABIC -> "العربية"
            else -> "System Default"
        }
    }
}

