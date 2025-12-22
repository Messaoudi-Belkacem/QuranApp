package com.example.quranapp

import android.app.Application
import android.os.Build
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.quranapp.data.repository.QuranRepository
import com.example.quranapp.util.LocationHelper
import com.example.quranapp.util.createFolder
import com.example.quranapp.util.createMediaStoreFolder
import com.example.quranapp.util.isFolderExists
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SharedViewModel @Inject constructor(
    private val quranRepository: QuranRepository,
    application: Application,
) : AndroidViewModel(application) {
    private val tag: String = "SharedViewModel.kt"
    private val context = getApplication<Application>()
    private val locationHelper = LocationHelper(context)

    init {
        Log.d(tag, "SharedViewModel init is called")
        viewModelScope.launch {
            val apiLevel = Build.VERSION.SDK_INT
            Log.d("API Level", "The API level of this device is: $apiLevel")


            // Handle location if permission is granted
            handleLocationSetup()

            if (quranRepository.isFirstLaunch()) {
                Log.d(tag, "This is first launch")
                quranRepository.setFirstLaunch(false)
            } else {
                Log.d(tag, "This is NOT first launch")
            }

            when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
                    if (createMediaStoreFolder(context, "Quran")) {
                        // Folder created successfully
                        Log.d(tag, "Folder created successfully")
                    } else {
                        // Folder creation failed
                        Log.d(tag, "Folder creation failed")
                    }
                }

                else -> {
                    val externalFolderPath =
                        context.getExternalFilesDir(null)?.absolutePath + "Quran"
                    if (isFolderExists(externalFolderPath)) {
                        Log.d(tag, "Folder exists and path is $externalFolderPath")
                    } else {
                        Log.d(tag, "Folder does not exist")
                        if (createFolder(externalFolderPath)) {
                            Log.d(tag, "Folder created")
                        } else {
                            Log.d(tag, "Folder creation failed")
                        }
                    }
                }
            }
        }
    }

    private suspend fun handleLocationSetup() {
        try {
            Log.d(tag, "=== handleLocationSetup started ===")

            if (locationHelper.hasLocationPermission()) {
                Log.d(tag, "✓ Location permission granted")

                if (locationHelper.isLocationEnabled()) {
                    Log.d(tag, "✓ Location services enabled, fetching location...")

                    val currentLocation = locationHelper.getCurrentLocation()
                    if (currentLocation != null) {
                        val (latitude, longitude) = currentLocation
                        quranRepository.setCurrentLocation(latitude, longitude)
                        Log.d(tag, "✓✓✓ Successfully stored location: $latitude, $longitude")
                    } else {
                        Log.w(tag, "⚠ Could not get current location (last known location not available)")
                        Log.d(tag, "This may happen on:")
                        Log.d(tag, "  - First app launch")
                        Log.d(tag, "  - Device hasn't used GPS recently")
                        Log.d(tag, "  - Location cache is cleared")
                        Log.d(tag, "Solution: HomeScreenViewModel will fetch fresh location when needed")
                    }
                } else {
                    Log.w(tag, "⚠ Location services are disabled in device settings")
                }
            } else {
                Log.d(tag, "Location permission not granted yet")
            }

            Log.d(tag, "=== handleLocationSetup completed ===")
        } catch (e: Exception) {
            Log.e(tag, "Error handling location setup", e)
        }
    }

    /**
     * Request fresh location update (can be called from UI when needed)
     */
    fun requestLocationUpdate() {
        viewModelScope.launch {
            try {
                if (locationHelper.hasLocationPermission()) {
                    val location = locationHelper.requestLocationUpdate()
                    if (location != null) {
                        val (latitude, longitude) = location
                        quranRepository.setCurrentLocation(latitude, longitude)
                        Log.d(tag, "Updated location: $latitude, $longitude")
                    }
                } else {
                    Log.w(tag, "Location permission not available for update")
                }
            } catch (e: Exception) {
                Log.e(tag, "Error requesting location update", e)
            }
        }
    }

    /**
     * Get stored location from preferences
     */
    suspend fun getStoredLocation(): com.example.quranapp.data.model.Location? {
        return try {
            quranRepository.getCurrentLocation()
        } catch (e: Exception) {
            Log.e(tag, "Error getting stored location", e)
            null
        }
    }
}