package com.example.quranapp.util

import android.util.Log
import com.example.quranapp.domain.model.AsrCalculationMethod
import com.example.quranapp.domain.model.HighLatitudeMethod
import com.example.quranapp.domain.model.PrayerCalculationMethod
import java.util.Calendar
import java.util.Date
import java.util.TimeZone
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.atan
import kotlin.math.atan2
import kotlin.math.floor

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
    private val timezone: Double,
    private val calculationMethod: PrayerCalculationMethod = PrayerCalculationMethod.MWL,
    private val asrMethod: AsrCalculationMethod = AsrCalculationMethod.SHAFII,
    private val highLatMethod: HighLatitudeMethod = HighLatitudeMethod.ANGLE_BASED,
) {
    companion object {
        private const val TAG = "PrayerTimeCalculator"

        /**
         * Get automatic timezone offset from system for a date
         */
        fun getTimezoneOffset(date: Date): Double {
            val calendar = Calendar.getInstance().apply { time = date }
            val offsetMillis =
                calendar.get(Calendar.ZONE_OFFSET) + calendar.get(Calendar.DST_OFFSET)
            return offsetMillis / (1000.0 * 60 * 60)
        }
    }

    // Julian date for current calculation
    private var jDate: Double = 0.0

    /**
     * Calculate all prayer times for a given date
     */
    fun getPrayerTimes(date: Date): Map<String, Date?> {
        val calendar = Calendar.getInstance().apply {
            time = date
            timeZone = TimeZone.getDefault()
        }

        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        Log.d(TAG, "Calculating for $year-$month-$day at ($latitude, $longitude), tz=$timezone")
        Log.d(TAG, "Method: ${calculationMethod.displayName}, Asr: ${asrMethod.displayName}")

        // Calculate Julian date at noon
        jDate = julianDate(year, month, day) - longitude / (15.0 * 24.0)

        // Compute prayer times
        val times = computePrayerTimes()

        // Apply timezone
        for (key in times.keys) {
            times[key] = times[key]!! + timezone
        }

        // Adjust times to proper range (0-24)
        for (key in times.keys) {
            times[key] = fixHour(times[key]!!)
        }

        // Apply high latitude adjustments
        adjustHighLatitudes(times)

        Log.d(TAG, "Calculated times: $times")

        // Convert to Date objects
        return times.mapValues { (name, hours) ->
            if (hours.isNaN()) {
                Log.w(TAG, "Could not calculate time for $name")
                null
            } else {
                hoursToDate(date, hours)
            }
        }
    }

    /**
     * Compute prayer times for current Julian date
     */
    private fun computePrayerTimes(): MutableMap<String, Double> {
        val fajrAngle = calculationMethod.fajrAngle ?: 18.0
        val ishaAngle = calculationMethod.ishaAngle ?: 17.0

        // Calculate each prayer time
        val fajr = sunAngleTime(fajrAngle, true)
        val sunrise = sunAngleTime(0.833, true)
        val dhuhr = midDay() + 1.0 / 60.0 // Add 1 minute safety
        val asr = asrTime()
        val sunset = sunAngleTime(0.833, false)
        val isha = if (calculationMethod.ishaInterval != null) {
            sunset + calculationMethod.ishaInterval / 60.0
        } else {
            sunAngleTime(ishaAngle, false)
        }

        Log.d(
            TAG,
            "Raw times - Fajr: $fajr, Sunrise: $sunrise, Dhuhr: $dhuhr, Asr: $asr, Maghrib: $sunset, Isha: $isha"
        )

        return mutableMapOf(
            "Fajr" to fajr,
            "Sunrise" to sunrise,
            "Dhuhr" to dhuhr,
            "Asr" to asr,
            "Maghrib" to sunset,
            "Isha" to isha
        )
    }

    /**
     * Compute mid-day (Dhuhr) time
     */
    private fun midDay(): Double {
        val t = equationOfTime(jDate + 0.5)
        return fixHour(12.0 - t)
    }

    /**
     * Compute time when sun reaches a specific angle below horizon
     * @param angle Angle below horizon (positive value)
     * @param isCcw True for times before noon (Fajr, Sunrise), false for after noon
     */
    private fun sunAngleTime(angle: Double, isCcw: Boolean): Double {
        val decl = sunDeclination(jDate + 0.5)
        val noon = midDay()

        // Hour angle formula: cos(H) = (sin(a) - sin(lat)*sin(dec)) / (cos(lat)*cos(dec))
        // where a = -angle for below horizon
        val angleRad = degToRad(-angle) // negative because below horizon
        val latRad = degToRad(latitude)
        val declRad = degToRad(decl)

        val cosH = (sin(angleRad) - sin(latRad) * sin(declRad)) / (cos(latRad) * cos(declRad))

        // Clamp to valid range
        val clampedCosH = cosH.coerceIn(-1.0, 1.0)
        val hourAngle = radToDeg(acos(clampedCosH))
        val t = hourAngle / 15.0

        return if (isCcw) noon - t else noon + t
    }

    /**
     * Compute Asr time
     * The shadow length at Asr = shadow at noon + shadow factor (1 for Shafi'i, 2 for Hanafi)
     */
    private fun asrTime(): Double {
        val decl = sunDeclination(jDate + 0.5)
        val factor = asrMethod.shadowFactor.toDouble()

        // Calculate the angle when shadow = factor * object_length + noon_shadow
        // angle = arccot(factor + tan(|latitude - declination|))
        val latDiff = abs(latitude - decl)
        val angle = radToDeg(atan(1.0 / (factor + tan(degToRad(latDiff)))))

        Log.d(TAG, "Asr calculation: decl=$decl, factor=$factor, latDiff=$latDiff, angle=$angle")

        return sunAngleTime(angle, false)
    }

    /**
     * Adjust times for high latitudes
     */
    private fun adjustHighLatitudes(times: MutableMap<String, Double>) {
        val sunrise = times["Sunrise"] ?: return
        val sunset = times["Sunset"] ?: times["Maghrib"] ?: return

        val nightTime = timeDiff(sunset, sunrise)

        // Adjust Fajr
        val fajrDiff = nightPortion(calculationMethod.fajrAngle ?: 18.0) * nightTime
        if (times["Fajr"]?.isNaN() == true || timeDiff(times["Fajr"]!!, sunrise) > fajrDiff) {
            times["Fajr"] = sunrise - fajrDiff
        }

        // Adjust Isha
        val ishaAngle = calculationMethod.ishaAngle ?: 17.0
        val ishaDiff = nightPortion(ishaAngle) * nightTime
        if (times["Isha"]?.isNaN() == true || timeDiff(sunset, times["Isha"]!!) > ishaDiff) {
            times["Isha"] = sunset + ishaDiff
        }
    }

    /**
     * Get night portion for high latitude adjustment
     */
    private fun nightPortion(angle: Double): Double {
        return when (highLatMethod) {
            HighLatitudeMethod.ANGLE_BASED -> angle / 60.0
            HighLatitudeMethod.MIDDLE_OF_NIGHT -> 0.5
            HighLatitudeMethod.ONE_SEVENTH -> 1.0 / 7.0
            HighLatitudeMethod.NONE -> 0.0
        }
    }

    /**
     * Compute sun declination
     */
    private fun sunDeclination(jd: Double): Double {
        return sunPosition(jd).second
    }

    /**
     * Compute equation of time
     */
    private fun equationOfTime(jd: Double): Double {
        return sunPosition(jd).first
    }

    /**
     * Compute sun position (equation of time and declination)
     */
    private fun sunPosition(jd: Double): Pair<Double, Double> {
        val d = jd - 2451545.0
        val g = fixAngle(357.529 + 0.98560028 * d)
        val q = fixAngle(280.459 + 0.98564736 * d)
        val l = fixAngle(q + 1.915 * sin(degToRad(g)) + 0.020 * sin(degToRad(2 * g)))

        val e = 23.439 - 0.00000036 * d
        val ra = radToDeg(atan2(cos(degToRad(e)) * sin(degToRad(l)), cos(degToRad(l)))) / 15.0
        val eqt = q / 15.0 - fixHour(ra)
        val decl = radToDeg(asin(sin(degToRad(e)) * sin(degToRad(l))))

        return Pair(eqt, decl)
    }

    /**
     * Calculate Julian date from Gregorian date
     */
    private fun julianDate(year: Int, month: Int, day: Int): Double {
        var y = year
        var m = month
        if (m <= 2) {
            y -= 1
            m += 12
        }
        val a = floor(y / 100.0)
        val b = 2 - a + floor(a / 4.0)
        return floor(365.25 * (y + 4716)) + floor(30.6001 * (m + 1)) + day + b - 1524.5
    }

    /**
     * Convert decimal hours to Date object
     */
    private fun hoursToDate(baseDate: Date, hours: Double): Date {
        val calendar = Calendar.getInstance().apply {
            time = baseDate
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val h = floor(hours).toInt()
        val m = floor((hours - h) * 60).toInt()
        val s = floor(((hours - h) * 60 - m) * 60).toInt()

        calendar.set(Calendar.HOUR_OF_DAY, h)
        calendar.set(Calendar.MINUTE, m)
        calendar.set(Calendar.SECOND, s)

        return calendar.time
    }

    // -------------------- Math helpers --------------------

    private fun degToRad(d: Double): Double = d * PI / 180.0
    private fun radToDeg(r: Double): Double = r * 180.0 / PI

    private fun sin(d: Double): Double = kotlin.math.sin(d)
    private fun cos(d: Double): Double = kotlin.math.cos(d)
    private fun tan(d: Double): Double = kotlin.math.tan(d)

    private fun arcsin(x: Double): Double = radToDeg(asin(x))
    private fun arccos(x: Double): Double = radToDeg(acos(x.coerceIn(-1.0, 1.0)))
    private fun arctan(x: Double): Double = radToDeg(atan(x))
    private fun arccot(x: Double): Double = radToDeg(atan(1.0 / x))

    private fun fixAngle(a: Double): Double {
        var result = a - 360.0 * floor(a / 360.0)
        if (result < 0) result += 360.0
        return result
    }

    private fun fixHour(a: Double): Double {
        var result = a - 24.0 * floor(a / 24.0)
        if (result < 0) result += 24.0
        return result
    }

    private fun timeDiff(time1: Double, time2: Double): Double {
        return fixHour(time2 - time1)
    }
}

