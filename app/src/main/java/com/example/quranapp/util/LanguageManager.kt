package com.example.quranapp.util

import android.app.Activity
import android.content.Context
import androidx.core.content.edit
import java.util.Locale

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

        // Restart activity to apply language change
        if (context is Activity) {
            val intent = context.intent
            context.finish()
            context.startActivity(intent)
        }
    }

    fun getCurrentLanguage(context: Context): String {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_LANGUAGE, LANGUAGE_SYSTEM) ?: LANGUAGE_SYSTEM
    }

    fun applyLanguage(context: Context) {
        val languageCode = getCurrentLanguage(context)

        if (languageCode == LANGUAGE_SYSTEM) {
            // Use system default
            return
        }

        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        val config = context.resources.configuration
        config.setLocale(locale)
        config.setLayoutDirection(locale)

        @Suppress("DEPRECATION")
        context.resources.updateConfiguration(config, context.resources.displayMetrics)
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

