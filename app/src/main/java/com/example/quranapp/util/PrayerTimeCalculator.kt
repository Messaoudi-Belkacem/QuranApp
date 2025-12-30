package com.example.quranapp.util

import com.example.quranapp.domain.model.AsrCalculationMethod
import com.example.quranapp.domain.model.HighLatitudeMethod
import com.example.quranapp.domain.model.PrayerCalculationMethod
import java.util.Calendar
import java.util.Date
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.atan
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.tan

class PrayerTimeCalculator(
    private val latitude: Double,
    private val longitude: Double,
    private val timezone: Double,
    private val calculationMethod: PrayerCalculationMethod,
    private val asrMethod: AsrCalculationMethod,
    private val highLatMethod: HighLatitudeMethod,
) {

    private var jDate = 0.0

    fun getPrayerTimes(date: Date): Map<String, Date?> {
        val cal = Calendar.getInstance().apply { time = date }

        jDate = julianDate(
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH) + 1,
            cal.get(Calendar.DAY_OF_MONTH)
        )

        val times = computeTimes()

        // Apply timezone ONCE
        times.replaceAll { _, v -> fixHour(v + timezone) }

        adjustHighLatitudes(times)

        return times.mapValues { (_, hours) ->
            hoursToDate(date, hours)
        }
    }

    private fun computeTimes(): MutableMap<String, Double> {
        // Use POSITIVE altitudes; sunAngleTime handles "below horizon" internally.
        val fajr = sunAngleTime(calculationMethod.fajrAngle!!, beforeNoon = true)
        val sunrise = sunAngleTime(0.833, beforeNoon = true)
        val dhuhr = midDay()
        val asr = asrTime()
        val maghrib = sunAngleTime(0.833, beforeNoon = false)
        val isha = calculationMethod.ishaInterval?.let { intervalMinutes ->
            maghrib + intervalMinutes / 60.0
        } ?: sunAngleTime(calculationMethod.ishaAngle!!, beforeNoon = false)

        return mutableMapOf(
            "Fajr" to fajr,
            "Sunrise" to sunrise,
            "Dhuhr" to dhuhr,
            "Asr" to asr,
            "Maghrib" to maghrib,
            "Isha" to isha
        )
    }

    private fun midDay(): Double {
        val eqt = equationOfTime(jDate + 0.5)
        return fixHour(12.0 - eqt - longitude / 15.0)
    }

    /**
     * @param altitude Positive number in degrees:
     * \- for Fajr/Isha: angle below horizon (e.g. 18)
     * \- for Sunrise/Sunset: 0.833 (refraction)
     */
    private fun sunAngleTime(altitude: Double, beforeNoon: Boolean): Double {
        val decl = sunDeclination(jDate + 0.5)
        val noon = midDay()

        // Convert desired event altitude to actual solar altitude (negative for below-horizon events).
        val targetAlt = -altitude

        val cosH = (
                sin(deg2rad(targetAlt)) -
                        sin(deg2rad(latitude)) * sin(deg2rad(decl))
                ) / (
                cos(deg2rad(latitude)) * cos(deg2rad(decl))
                )

        val hourAngleHours = rad2deg(acos(cosH.coerceIn(-1.0, 1.0))) / 15.0
        return if (beforeNoon) noon - hourAngleHours else noon + hourAngleHours
    }

    private fun asrTime(): Double {
        val decl = sunDeclination(jDate + 0.5)
        val factor = asrMethod.shadowFactor

        // Asr altitude: atan(1 / (factor + tan(|lat - decl|)))
        val latDiff = abs(latitude - decl)
        val altitude = rad2deg(atan(1.0 / (factor + tan(deg2rad(latDiff)))))

        return sunAngleTime(altitude, beforeNoon = false)
    }

    private fun adjustHighLatitudes(times: MutableMap<String, Double>) {
        if (highLatMethod == HighLatitudeMethod.NONE) return

        val sunrise = times["Sunrise"]!!
        val sunset = times["Maghrib"]!!
        val night = timeDiff(sunset, sunrise)

        val fajrLimit = nightPortion(calculationMethod.fajrAngle!!) * night
        if (timeDiff(times["Fajr"]!!, sunrise) > fajrLimit) {
            times["Fajr"] = sunrise - fajrLimit
        }

        val ishaLimit = nightPortion(calculationMethod.ishaAngle!!) * night
        if (timeDiff(sunset, times["Isha"]!!) > ishaLimit) {
            times["Isha"] = sunset + ishaLimit
        }
    }

    private fun nightPortion(angle: Double): Double = when (highLatMethod) {
        HighLatitudeMethod.ANGLE_BASED -> angle / 60.0
        HighLatitudeMethod.MIDDLE_OF_NIGHT -> 0.5
        HighLatitudeMethod.ONE_SEVENTH -> 1.0 / 7.0
        HighLatitudeMethod.NONE -> 0.0
    }

    private fun sunDeclination(jd: Double): Double = sunPosition(jd).second

    private fun equationOfTime(jd: Double): Double = sunPosition(jd).first

    private fun sunPosition(jd: Double): Pair<Double, Double> {
        val d = jd - 2451545.0
        val g = fixAngle(357.529 + 0.98560028 * d)
        val q = fixAngle(280.459 + 0.98564736 * d)
        val l = fixAngle(q + 1.915 * sin(deg2rad(g)) + 0.020 * sin(deg2rad(2 * g)))

        val e = 23.439 - 0.00000036 * d
        val ra = atan2(
            cos(deg2rad(e)) * sin(deg2rad(l)),
            cos(deg2rad(l))
        ) * 180 / PI / 15.0

        val eqt = q / 15.0 - fixHour(ra)
        val decl = asin(sin(deg2rad(e)) * sin(deg2rad(l))) * 180 / PI

        return eqt to decl
    }

    private fun julianDate(y: Int, m: Int, d: Int): Double {
        var year = y
        var month = m
        if (month <= 2) {
            year--
            month += 12
        }
        val a = floor(year / 100.0)
        val b = 2 - a + floor(a / 4.0)
        return floor(365.25 * (year + 4716)) +
                floor(30.6001 * (month + 1)) +
                d + b - 1524.5
    }

    private fun hoursToDate(base: Date, h: Double): Date {
        val cal = Calendar.getInstance().apply {
            time = base
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        cal.add(Calendar.MINUTE, (h * 60).roundToInt())
        return cal.time
    }

    private fun timeDiff(a: Double, b: Double) = fixHour(b - a)

    private fun deg2rad(d: Double) = d * PI / 180.0
    private fun rad2deg(r: Double) = r * 180.0 / PI

    private fun fixHour(h: Double) = ((h % 24) + 24) % 24
    private fun fixAngle(a: Double) = ((a % 360) + 360) % 360
}