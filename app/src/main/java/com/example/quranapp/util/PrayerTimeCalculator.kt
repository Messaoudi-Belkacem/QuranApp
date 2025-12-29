package com.example.quranapp.util

import android.util.Log
import com.example.quranapp.domain.model.AsrCalculationMethod
import com.example.quranapp.domain.model.HighLatitudeMethod
import com.example.quranapp.domain.model.PrayerCalculationMethod
import java.util.Calendar
import java.util.Date
import java.util.TimeZone
import kotlin.math.*

/**
 * Comprehensive Islamic Prayer Time Calculator
 *
 * Calculates prayer times using astronomical formulas based on sun position.
 * Supports multiple calculation methods, Asr juristic opinions, and high-latitude adjustments.
 *
 * Based on the algorithms from:
 * - Astronomical Algorithms by Jean Meeus
 * - PrayTimes.org methodology
 */
class PrayerTimeCalculator(
    private val latitude: Double,
    private val longitude: Double,
    private val timezone: Double, // Hours offset from UTC
    private val calculationMethod: PrayerCalculationMethod = PrayerCalculationMethod.MWL,
    private val asrMethod: AsrCalculationMethod = AsrCalculationMethod.SHAFII,
    private val highLatMethod: HighLatitudeMethod = HighLatitudeMethod.ANGLE_BASED
) {
    companion object {
        private const val TAG = "PrayerTimeCalculator"

        // Constants for calculations
        private const val INVALID_TIME = Double.NaN

        /**
         * Get automatic timezone offset from system for a date
         */
        fun getTimezoneOffset(date: Date): Double {
            val calendar = Calendar.getInstance().apply { time = date }
            val offsetMillis = calendar.get(Calendar.ZONE_OFFSET) + calendar.get(Calendar.DST_OFFSET)
            return offsetMillis / (1000.0 * 60 * 60) // Convert to hours
        }
    }

    /**
     * Calculate all prayer times for a given date
     * @return Map of prayer names to their times (24-hour format)
     */
    fun getPrayerTimes(date: Date): Map<String, Date?> {
        val calendar = Calendar.getInstance().apply {
            time = date
            timeZone = TimeZone.getDefault()
        }

        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1 // Calendar.MONTH is 0-based
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        Log.d(TAG, "Calculating prayer times for $year-$month-$day at ($latitude, $longitude)")

        // Calculate Julian date
        val julianDate = getJulianDate(year, month, day)

        // Calculate equation of time and sun declination
        val equationOfTime = calculateEquationOfTime(julianDate)
        val sunDeclination = calculateSunDeclination(julianDate)

        // Calculate prayer times
        val times = mutableMapOf<String, Double>()

        // Sunrise and Sunset (base calculations)
        val sunrise = calculateTime(90.833, sunDeclination, true)
        val sunset = calculateTime(90.833, sunDeclination, false)

        times["Sunrise"] = sunrise
        times["Sunset"] = sunset

        // Fajr (dawn)
        val fajrAngle = calculationMethod.fajrAngle ?: 18.0
        var fajr = calculateTime(180 - fajrAngle, sunDeclination, true)

        // Isha (night)
        var isha = if (calculationMethod.ishaInterval != null) {
            // Fixed interval after Maghrib
            sunset + calculationMethod.ishaInterval / 60.0
        } else {
            val ishaAngle = calculationMethod.ishaAngle ?: 17.0
            calculateTime(180 - ishaAngle, sunDeclination, false)
        }

        // Apply high latitude adjustment if needed
        if (fajr.isNaN() || isha.isNaN()) {
            Log.d(TAG, "Applying high latitude adjustment: $highLatMethod")
            val adjusted = adjustHighLatitudeTimes(sunrise, sunset, fajrAngle, calculationMethod.ishaAngle ?: 17.0)
            if (fajr.isNaN()) fajr = adjusted.first
            if (isha.isNaN()) isha = adjusted.second
        }

        times["Fajr"] = fajr

        // Dhuhr (noon) - Solar noon + small buffer
        val noon = calculateSolarNoon()
        times["Dhuhr"] = noon + 0.083 // Add 5 minutes buffer

        // Asr (afternoon)
        val asr = calculateAsrTime(sunDeclination, noon)
        times["Asr"] = asr

        // Maghrib (sunset) - Usually same as sunset, but can have interval
        val maghrib = if (calculationMethod.maghribInterval != null) {
            sunset + calculationMethod.maghribInterval / 60.0
        } else {
            sunset
        }
        times["Maghrib"] = maghrib

        times["Isha"] = isha

        // Convert to Date objects
        return times.mapValues { (name, time) ->
            if (time.isNaN()) {
                Log.w(TAG, "Could not calculate time for $name")
                null
            } else {
                convertToDate(date, time)
            }
        }
    }

    /**
     * Calculate time for a given sun angle
     * @param angle Sun angle below/above horizon
     * @param declination Sun declination
     * @param isRising True for sunrise-related times, false for sunset-related
     * @return Time in hours (decimal), or NaN if cannot be calculated
     */
    private fun calculateTime(angle: Double, declination: Double, isRising: Boolean): Double {
        val latRad = Math.toRadians(latitude)
        val decRad = Math.toRadians(declination)
        val angleRad = Math.toRadians(angle)

        val cosHourAngle = (cos(angleRad) - sin(latRad) * sin(decRad)) / (cos(latRad) * cos(decRad))

        // Check if the sun reaches this angle
        if (cosHourAngle < -1 || cosHourAngle > 1) {
            return INVALID_TIME
        }

        val hourAngle = Math.toDegrees(acos(cosHourAngle))
        val time = if (isRising) {
            12 - hourAngle / 15.0
        } else {
            12 + hourAngle / 15.0
        }

        return time
    }

    /**
     * Calculate Asr time based on shadow length
     */
    private fun calculateAsrTime(declination: Double, noon: Double): Double {
        val shadowFactor = asrMethod.shadowFactor.toDouble()

        val latRad = Math.toRadians(latitude)
        val decRad = Math.toRadians(declination)

        val shadowAngle = atan(shadowFactor + tan(abs(latRad - decRad)))
        val angle = 90 - Math.toDegrees(shadowAngle)

        val asrTime = calculateTime(angle, declination, false)

        // Ensure Asr is after noon
        return if (asrTime < noon) noon + 1 else asrTime
    }

    /**
     * Calculate solar noon (midday)
     */
    private fun calculateSolarNoon(): Double {
        return 12 - longitude / 15.0
    }

    /**
     * Adjust Fajr and Isha for high latitudes
     * @return Pair of (adjusted Fajr, adjusted Isha)
     */
    private fun adjustHighLatitudeTimes(
        sunrise: Double,
        sunset: Double,
        fajrAngle: Double,
        ishaAngle: Double
    ): Pair<Double, Double> {
        val nightLength = 24 - (sunset - sunrise)

        val (fajrPortion, ishaPortion) = when (highLatMethod) {
            HighLatitudeMethod.MIDDLE_OF_NIGHT -> {
                Pair(nightLength / 2, nightLength / 2)
            }
            HighLatitudeMethod.ONE_SEVENTH -> {
                Pair(nightLength / 7, nightLength / 7)
            }
            HighLatitudeMethod.ANGLE_BASED -> {
                Pair(fajrAngle / 60.0 * nightLength, ishaAngle / 60.0 * nightLength)
            }
            HighLatitudeMethod.NONE -> {
                return Pair(INVALID_TIME, INVALID_TIME)
            }
        }

        val adjustedFajr = sunrise - fajrPortion
        val adjustedIsha = sunset + ishaPortion

        return Pair(adjustedFajr, adjustedIsha)
    }

    /**
     * Calculate Julian date
     */
    private fun getJulianDate(year: Int, month: Int, day: Int): Double {
        var y = year
        var m = month

        if (m <= 2) {
            y -= 1
            m += 12
        }

        val a = floor(y / 100.0).toInt()
        val b = 2 - a + floor(a / 4.0).toInt()

        val jd = floor(365.25 * (y + 4716)) + floor(30.6001 * (m + 1)) + day + b - 1524.5

        return jd
    }

    /**
     * Calculate equation of time (correction for solar time)
     */
    private fun calculateEquationOfTime(julianDate: Double): Double {
        val t = (julianDate - 2451545.0) / 36525.0

        val epsilon = 23.439 - 0.0000004 * t
        val l0 = 280.466 + 36000.77 * t
        val e = 0.016708 - 0.000042 * t
        val m = 357.529 + 35999.05 * t

        val y = tan(Math.toRadians(epsilon / 2)).pow(2)

        val eqTime = y * sin(2 * Math.toRadians(l0)) -
                2 * e * sin(Math.toRadians(m)) +
                4 * e * y * sin(Math.toRadians(m)) * cos(2 * Math.toRadians(l0)) -
                0.5 * y.pow(2) * sin(4 * Math.toRadians(l0)) -
                1.25 * e.pow(2) * sin(2 * Math.toRadians(m))

        return Math.toDegrees(eqTime) * 4 // Convert to minutes
    }

    /**
     * Calculate sun declination
     */
    private fun calculateSunDeclination(julianDate: Double): Double {
        val t = (julianDate - 2451545.0) / 36525.0

        val epsilon = 23.439 - 0.0000004 * t
        val l0 = 280.466 + 36000.77 * t
        val m = 357.529 + 35999.05 * t

        val lambda = l0 + 1.915 * sin(Math.toRadians(m)) + 0.020 * sin(2 * Math.toRadians(m))

        val declination = asin(sin(Math.toRadians(epsilon)) * sin(Math.toRadians(lambda)))

        return Math.toDegrees(declination)
    }

    /**
     * Convert decimal hours to Date object
     */
    private fun convertToDate(baseDate: Date, hours: Double): Date {
        val calendar = Calendar.getInstance().apply {
            time = baseDate
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val totalMinutes = (hours * 60).toInt()
        calendar.add(Calendar.MINUTE, totalMinutes)

        return calendar.time
    }
}

