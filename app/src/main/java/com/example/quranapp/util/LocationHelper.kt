package com.example.quranapp.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.util.Log
import androidx.core.content.ContextCompat
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class LocationHelper(private val context: Context) {
    private val tag = "LocationHelper"
    private val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

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
                Log.d(tag, "  Provider: ${location.provider}, Accuracy: ${location.accuracy}m, Age: ${(System.currentTimeMillis() - location.time) / 1000}s")
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
            Log.d(tag, "Provider: $provider, Location: ${if (location != null) "${location.latitude},${location.longitude}" else "null"}")

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
    suspend fun requestLocationUpdate(): Pair<Float, Float>? = suspendCancellableCoroutine { continuation ->
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
                continuation.resume(Pair(location.latitude.toFloat(), location.longitude.toFloat()))
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
}
