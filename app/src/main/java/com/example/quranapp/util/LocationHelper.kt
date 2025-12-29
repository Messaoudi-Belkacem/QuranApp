package com.example.quranapp.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.util.Log
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Helper class for location-related operations
 *
 * Provides methods to:
 * - Check location permissions and services
 * - Get current device location (last known or fresh update)
 * - Reverse geocode coordinates to addresses and city names
 *
 * Usage example:
 * ```kotlin
 * val locationHelper = LocationHelper(context)
 *
 * // Get current location
 * val location = locationHelper.getCurrentLocation()
 * location?.let { (lat, lon) ->
 *     // Get full address
 *     val address = locationHelper.getAddressFromLocation(lat, lon)
 *
 *     // Or just city name
 *     val city = locationHelper.getCityName(lat, lon)
 * }
 * ```
 *
 * Note: Requires ACCESS_FINE_LOCATION or ACCESS_COARSE_LOCATION permissions
 */
class LocationHelper(private val context: Context) {
    private val tag = "LocationHelper"
    private val locationManager =
        context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    /**
     * Check if location permissions are granted
     */
    fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Check if location services are enabled
     */
    fun isLocationEnabled(): Boolean {
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    /**
     * Get the current location using the best available provider
     */
    suspend fun getCurrentLocation(): Pair<Float, Float>? {
        Log.d(tag, "=== getCurrentLocation called ===")

        if (!hasLocationPermission()) {
            Log.w(tag, "Location permission not granted")
            return null
        }
        Log.d(tag, "✓ Location permission granted")

        if (!isLocationEnabled()) {
            Log.w(tag, "Location services are disabled")
            return null
        }
        Log.d(tag, "✓ Location services enabled")

        return try {
            val location = getLastKnownLocation()
            if (location != null) {
                Log.d(tag, "✓ Got location: ${location.latitude}, ${location.longitude}")
                Log.d(
                    tag,
                    "  Provider: ${location.provider}, Accuracy: ${location.accuracy}m, Age: ${(System.currentTimeMillis() - location.time) / 1000}s"
                )
                Pair(location.latitude.toFloat(), location.longitude.toFloat())
            } else {
                Log.w(tag, "⚠ No last known location available")
                Log.d(tag, "Attempting to request fresh location update...")
                // Try to request a fresh location update
                requestLocationUpdate()
            }
        } catch (e: SecurityException) {
            Log.e(tag, "Security exception getting location", e)
            null
        } catch (e: Exception) {
            Log.e(tag, "Error getting location", e)
            null
        }
    }

    /**
     * Get the last known location from the best available provider
     */
    @Suppress("MissingPermission")
    private fun getLastKnownLocation(): Location? {
        if (!hasLocationPermission()) {
            Log.w(tag, "Cannot get last known location - no permission")
            return null
        }

        val providers = locationManager.getProviders(true)
        Log.d(tag, "Available location providers: $providers")

        if (providers.isEmpty()) {
            Log.w(tag, "No location providers are enabled")
            return null
        }

        var bestLocation: Location? = null

        for (provider in providers) {
            val location = locationManager.getLastKnownLocation(provider)
            Log.d(
                tag,
                "Provider: $provider, Location: ${if (location != null) "${location.latitude},${location.longitude}" else "null"}"
            )

            if (location != null) {
                if (bestLocation == null || location.accuracy < bestLocation.accuracy) {
                    bestLocation = location
                    Log.d(tag, "  → New best location from $provider")
                }
            }
        }

        if (bestLocation == null) {
            Log.w(tag, "No last known location found from any provider")
        } else {
            Log.d(tag, "Best location selected from provider: ${bestLocation.provider}")
        }

        return bestLocation
    }

    /**
     * Request a fresh location update (more accurate but takes time)
     */
    @Suppress("MissingPermission")
    suspend fun requestLocationUpdate(): Pair<Float, Float>? =
        suspendCancellableCoroutine { continuation ->
            if (!hasLocationPermission()) {
                continuation.resume(null)
                return@suspendCancellableCoroutine
            }

            if (!isLocationEnabled()) {
                continuation.resume(null)
                return@suspendCancellableCoroutine
            }

            val provider = when {
                locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) -> LocationManager.GPS_PROVIDER
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) -> LocationManager.NETWORK_PROVIDER
                else -> {
                    continuation.resume(null)
                    return@suspendCancellableCoroutine
                }
            }

            val locationListener = object : android.location.LocationListener {
                override fun onLocationChanged(location: Location) {
                    locationManager.removeUpdates(this)
                    continuation.resume(
                        Pair(
                            location.latitude.toFloat(),
                            location.longitude.toFloat()
                        )
                    )
                }

                override fun onProviderEnabled(provider: String) {}
                override fun onProviderDisabled(provider: String) {
                    locationManager.removeUpdates(this)
                    continuation.resume(null)
                }
            }

            try {
                locationManager.requestLocationUpdates(
                    provider,
                    0L,
                    0f,
                    locationListener
                )

                continuation.invokeOnCancellation {
                    locationManager.removeUpdates(locationListener)
                }
            } catch (e: SecurityException) {
                continuation.resumeWithException(e)
            }
        }

    /**
     * Get address/city name from coordinates using Geocoder
     *
     * @param lat Latitude coordinate
     * @param lon Longitude coordinate
     * @return Formatted address string with city/area information, or null if geocoding fails
     *
     * Note: Requires network connection and may be slow. Call from a coroutine.
     */
    suspend fun getAddressFromLocation(lat: Float, lon: Float): String? {
        return withContext(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(context, Locale.getDefault())
                val results = try {
                    geocoder.getFromLocation(lat.toDouble(), lon.toDouble(), 1)
                } catch (e: IOException) {
                    Log.e(tag, "Geocoder IO error", e)
                    null
                }

                if (results.isNullOrEmpty()) {
                    Log.w(tag, "No address found for $lat,$lon")
                    return@withContext null
                }

                val address = results[0]
                // Prefer city/locality, fall back to admin area or full formatted address
                val city = address.locality ?: address.subAdminArea ?: address.adminArea
                val parts = listOfNotNull(
                    address.featureName,
                    address.thoroughfare,
                    address.subLocality,
                    city,
                    address.postalCode,
                    address.countryName
                )
                val formatted = parts.joinToString(", ")
                formatted.takeIf { it.isNotBlank() }
            } catch (e: Exception) {
                Log.e(tag, "Error reverse-geocoding", e)
                null
            }
        }
    }

    /**
     * Get only the city name from coordinates
     *
     * @param lat Latitude coordinate
     * @param lon Longitude coordinate
     * @return City name, or null if not found
     */
    suspend fun getCityName(lat: Float, lon: Float): String? {
        return withContext(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(context, Locale.getDefault())
                val results = try {
                    geocoder.getFromLocation(lat.toDouble(), lon.toDouble(), 1)
                } catch (e: IOException) {
                    Log.e(tag, "Geocoder IO error", e)
                    null
                }

                if (results.isNullOrEmpty()) {
                    Log.w(tag, "No address found for $lat,$lon")
                    return@withContext null
                }

                val address = results[0]
                // Return the most specific location name available
                address.locality ?: address.subAdminArea ?: address.adminArea
            } catch (e: Exception) {
                Log.e(tag, "Error reverse-geocoding", e)
                null
            }
        }
    }
}