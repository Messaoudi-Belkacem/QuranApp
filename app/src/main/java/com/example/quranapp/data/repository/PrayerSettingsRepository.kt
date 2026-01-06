package com.example.quranapp.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.quranapp.domain.model.AsrCalculationMethod
import com.example.quranapp.domain.model.HighLatitudeMethod
import com.example.quranapp.domain.model.PrayerCalculationMethod
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for prayer time calculation settings
 */
@Singleton
class PrayerSettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "prayer_settings"
        private const val KEY_CALCULATION_METHOD = "calculation_method"
        private const val KEY_ASR_METHOD = "asr_method"
        private const val KEY_HIGH_LAT_METHOD = "high_latitude_method"
        private const val KEY_COUNTRY_CODE = "country_code"
        private const val KEY_NOTIFICATION_ENABLED = "prayer_notification_enabled"
    }

    /**
     * Get selected calculation method, or auto-detect based on country
     */
    fun getCalculationMethod(): PrayerCalculationMethod {
        val methodId = prefs.getString(KEY_CALCULATION_METHOD, null)

        return if (methodId != null) {
            PrayerCalculationMethod.getMethodById(methodId) ?: getAutoDetectedMethod()
        } else {
            getAutoDetectedMethod()
        }
    }

    /**
     * Set calculation method
     */
    fun setCalculationMethod(method: PrayerCalculationMethod) {
        prefs.edit().putString(KEY_CALCULATION_METHOD, method.id).apply()
    }

    /**
     * Get Asr calculation method
     */
    fun getAsrMethod(): AsrCalculationMethod {
        val methodName = prefs.getString(KEY_ASR_METHOD, AsrCalculationMethod.SHAFII.name)
        return AsrCalculationMethod.fromString(methodName ?: AsrCalculationMethod.SHAFII.name)
    }

    /**
     * Set Asr calculation method
     */
    fun setAsrMethod(method: AsrCalculationMethod) {
        prefs.edit().putString(KEY_ASR_METHOD, method.name).apply()
    }

    /**
     * Get high latitude adjustment method
     */
    fun getHighLatitudeMethod(): HighLatitudeMethod {
        val methodName = prefs.getString(KEY_HIGH_LAT_METHOD, HighLatitudeMethod.ANGLE_BASED.name)
        return HighLatitudeMethod.fromString(methodName ?: HighLatitudeMethod.ANGLE_BASED.name)
    }

    /**
     * Set high latitude adjustment method
     */
    fun setHighLatitudeMethod(method: HighLatitudeMethod) {
        prefs.edit().putString(KEY_HIGH_LAT_METHOD, method.name).apply()
    }

    /**
     * Set country code for auto-detection
     */
    fun setCountryCode(countryCode: String) {
        prefs.edit().putString(KEY_COUNTRY_CODE, countryCode).apply()
    }

    /**
     * Get country code
     */
    fun getCountryCode(): String? {
        return prefs.getString(KEY_COUNTRY_CODE, null)
    }

    /**
     * Auto-detect method based on stored country code
     */
    private fun getAutoDetectedMethod(): PrayerCalculationMethod {
        val countryCode = getCountryCode() ?: detectCountryFromSystem()
        return PrayerCalculationMethod.getDefaultMethodForCountry(countryCode)
    }

    /**
     * Detect country from system locale
     */
    private fun detectCountryFromSystem(): String {
        return try {
            context.resources.configuration.locales[0].country
        } catch (e: Exception) {
            "US" // Default fallback
        }
    }

    /**
     * Reset all settings to defaults
     */
    fun resetToDefaults() {
        prefs.edit().clear().apply()
    }

    /**
     * Check if prayer countdown notification is enabled
     */
    fun isPrayerNotificationEnabled(): Boolean {
        return prefs.getBoolean(KEY_NOTIFICATION_ENABLED, false)
    }

    /**
     * Set prayer countdown notification enabled state
     */
    fun setPrayerNotificationEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_NOTIFICATION_ENABLED, enabled).apply()
    }
}
