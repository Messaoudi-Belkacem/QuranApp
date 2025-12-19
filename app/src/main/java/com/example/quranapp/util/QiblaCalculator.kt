package com.example.quranapp.util

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan

object QiblaCalculator {
    // Kaaba coordinates
    private const val KAABA_LATITUDE = 21.4225
    private const val KAABA_LONGITUDE = 39.8262

    /**
     * Calculate the bearing (direction) to Qibla from the user's location
     * Returns angle in degrees (0-360°) relative to true north
     */
    fun calculateQiblaBearing(
        userLat: Double,
        userLon: Double,
    ): Double {
        val kaabaLat = Math.toRadians(KAABA_LATITUDE)
        val kaabaLon = Math.toRadians(KAABA_LONGITUDE)

        val lat = Math.toRadians(userLat)
        val lon = Math.toRadians(userLon)

        val dLon = kaabaLon - lon

        val y = sin(dLon)
        val x = cos(lat) * tan(kaabaLat) - sin(lat) * cos(dLon)

        return (Math.toDegrees(atan2(y, x)) + 360) % 360
    }

    /**
     * Calculate the distance to Kaaba in kilometers
     */
    fun calculateDistanceToKaaba(
        userLat: Double,
        userLon: Double,
    ): Double {
        val earthRadiusKm = 6371.0

        val dLat = Math.toRadians(KAABA_LATITUDE - userLat)
        val dLon = Math.toRadians(KAABA_LONGITUDE - userLon)

        val lat1 = Math.toRadians(userLat)
        val lat2 = Math.toRadians(KAABA_LATITUDE)

        val a = sin(dLat / 2) * sin(dLat / 2) +
                sin(dLon / 2) * sin(dLon / 2) * cos(lat1) * cos(lat2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return earthRadiusKm * c
    }
}

