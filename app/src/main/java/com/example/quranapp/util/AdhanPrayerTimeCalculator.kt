@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.example.quranapp.util

import android.util.Log
import com.batoulapps.adhan2.CalculationMethod
import com.batoulapps.adhan2.CalculationParameters
import com.batoulapps.adhan2.Coordinates
import com.batoulapps.adhan2.HighLatitudeRule
import com.batoulapps.adhan2.Madhab
import com.batoulapps.adhan2.PrayerTimes
import com.batoulapps.adhan2.data.DateComponents
import com.example.quranapp.domain.model.AsrCalculationMethod
import com.example.quranapp.domain.model.HighLatitudeMethod
import com.example.quranapp.domain.model.PrayerCalculationMethod
import kotlin.time.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toLocalDateTime
import java.util.Calendar
import java.util.Date

/**
 * Prayer Time Calculator using the Adhan library by Batoul Apps
 *
 * This implementation uses high-precision astronomical equations from Jean Meeus'
 * "Astronomical Algorithms" book for accurate prayer time calculations.
 *
 * The Adhan library is the most reliable and tested solution for Islamic prayer times.
 */
class AdhanPrayerTimeCalculator(
    private val latitude: Double,
    private val longitude: Double,
    private val calculationMethod: PrayerCalculationMethod,
    private val asrMethod: AsrCalculationMethod,
    private val highLatMethod: HighLatitudeMethod,
) {
    companion object {
        private const val TAG = "AdhanPrayerCalculator"
    }

    /**
     * Calculate prayer times for a given date
     * @param date The date to calculate prayer times for
     * @return Map of prayer names to their Date objects
     */
    fun getPrayerTimes(date: Date): Map<String, Date?> {
        return try {
            // Create coordinates
            val coordinates = Coordinates(latitude, longitude)

            // Get date components
            val calendar = Calendar.getInstance().apply { time = date }
            val dateComponents = DateComponents(
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH) + 1, // Calendar.MONTH is 0-based
                calendar.get(Calendar.DAY_OF_MONTH)
            )

            // Get calculation parameters based on selected method
            val params = getCalculationParameters()

            // Calculate prayer times using Adhan library
            val prayerTimes = PrayerTimes(coordinates, dateComponents, params)

            // Get system timezone
            val systemTimeZone = TimeZone.currentSystemDefault()

            // Convert Instant to Date for each prayer time
            val times = mutableMapOf<String, Date?>()

            times["Fajr"] = instantToDate(prayerTimes.fajr, systemTimeZone)
            times["Sunrise"] = instantToDate(prayerTimes.sunrise, systemTimeZone)
            times["Dhuhr"] = instantToDate(prayerTimes.dhuhr, systemTimeZone)
            times["Asr"] = instantToDate(prayerTimes.asr, systemTimeZone)
            times["Maghrib"] = instantToDate(prayerTimes.maghrib, systemTimeZone)
            times["Isha"] = instantToDate(prayerTimes.isha, systemTimeZone)

            Log.d(
                TAG,
                "Prayer times calculated successfully using ${calculationMethod.displayName}"
            )
            Log.d(TAG, "Times: $times")

            times

        } catch (e: Exception) {
            Log.e(TAG, "Error calculating prayer times with Adhan library", e)
            emptyMap()
        }
    }

    /**
     * Convert Adhan library Instant to Java Date
     */
    private fun instantToDate(instant: Instant, timeZone: TimeZone): Date {
        val localDateTime = instant.toLocalDateTime(timeZone)
        val javaLocalDateTime = localDateTime.toJavaLocalDateTime()

        return Calendar.getInstance().apply {
            set(Calendar.YEAR, javaLocalDateTime.year)
            set(Calendar.MONTH, javaLocalDateTime.monthValue - 1)
            set(Calendar.DAY_OF_MONTH, javaLocalDateTime.dayOfMonth)
            set(Calendar.HOUR_OF_DAY, javaLocalDateTime.hour)
            set(Calendar.MINUTE, javaLocalDateTime.minute)
            set(Calendar.SECOND, javaLocalDateTime.second)
            set(Calendar.MILLISECOND, 0)
        }.time
    }

    /**
     * Get CalculationParameters based on selected calculation method
     */
    private fun getCalculationParameters(): CalculationParameters {
        // Map our calculation method to Adhan's CalculationMethod
        val adhanMethod = when (calculationMethod.id) {
            "mwl" -> CalculationMethod.MUSLIM_WORLD_LEAGUE
            "egyptian" -> CalculationMethod.EGYPTIAN
            "makkah" -> CalculationMethod.UMM_AL_QURA
            "isna" -> CalculationMethod.NORTH_AMERICA
            "karachi" -> CalculationMethod.KARACHI
            "moonsighting" -> CalculationMethod.MOON_SIGHTING_COMMITTEE
            "tehran", "russia" -> CalculationMethod.OTHER
            else -> CalculationMethod.MUSLIM_WORLD_LEAGUE
        }

        // Get parameters from the method and customize them
        val madhab = when (asrMethod) {
            AsrCalculationMethod.HANAFI -> Madhab.HANAFI
            AsrCalculationMethod.SHAFII -> Madhab.SHAFI
        }

        val highLatRule = when (highLatMethod) {
            HighLatitudeMethod.MIDDLE_OF_NIGHT -> HighLatitudeRule.MIDDLE_OF_THE_NIGHT
            HighLatitudeMethod.ONE_SEVENTH -> HighLatitudeRule.SEVENTH_OF_THE_NIGHT
            HighLatitudeMethod.ANGLE_BASED -> HighLatitudeRule.TWILIGHT_ANGLE
            HighLatitudeMethod.NONE -> HighLatitudeRule.MIDDLE_OF_THE_NIGHT // Fallback
        }

        val params = adhanMethod.parameters.copy(
            madhab = madhab,
            highLatitudeRule = highLatRule
        ).let { baseParams ->
            // Apply custom angles for methods not natively supported by the Adhan library
            when (calculationMethod.id) {
                "tehran" -> baseParams.copy(
                    fajrAngle = 17.7,
                    ishaAngle = 14.0
                )
                "russia" -> baseParams.copy(
                    fajrAngle = 16.0,
                    ishaAngle = 15.0
                )
                else -> baseParams
            }
        }

        Log.d(
            TAG,
            "Using method: ${adhanMethod.name}, Madhab: $madhab, HighLatRule: $highLatRule"
        )

        return params
    }
}