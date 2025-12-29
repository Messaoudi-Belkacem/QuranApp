package com.example.quranapp.presentation.screen.home

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quranapp.data.repository.QuranRepository
import com.example.quranapp.util.LocationHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.kosrat.muslimdata.models.AsrMethod
import dev.kosrat.muslimdata.models.CalculationMethod
import dev.kosrat.muslimdata.models.HigherLatitudeMethod
import dev.kosrat.muslimdata.models.PrayerAttribute
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class HomeScreenViewModel @Inject constructor(
    private val quranRepository: QuranRepository,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    private val locationHelper = LocationHelper(context)
    private val tag = "HomeScreenViewModel"

    init {
        loadPrayerTimes()
    }

    private fun loadPrayerTimes() {
        viewModelScope.launch {
            try {
                Log.d(tag, "=== Starting loadPrayerTimes ===")

                // Step 1: Check location permission
                if (!locationHelper.hasLocationPermission()) {
                    Log.e(tag, "Location permission not granted")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Location permission is required. Please grant permission in settings."
                    )
                    return@launch
                }
                Log.d(tag, "✓ Location permission granted")

                // Step 2: Check if location services are enabled
                if (!locationHelper.isLocationEnabled()) {
                    Log.e(tag, "Location services are disabled")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Location services are disabled. Please enable GPS in your device settings."
                    )
                    return@launch
                }
                Log.d(tag, "✓ Location services enabled")

                // Step 3: Try to get stored location first
                var storedLocation = quranRepository.getCurrentLocation()
                Log.d(tag, "Stored location: $storedLocation")

                // Step 4: If no stored location, fetch fresh location
                if (storedLocation == null) {
                    Log.d(tag, "No stored location found, fetching fresh location...")
                    val freshLocation = locationHelper.getCurrentLocation()

                    if (freshLocation != null) {
                        val (latitude, longitude) = freshLocation
                        Log.d(tag, "✓ Fresh location obtained: $latitude, $longitude")

                        // Store it for future use
                        quranRepository.setCurrentLocation(latitude, longitude)
                        storedLocation = quranRepository.getCurrentLocation()
                        Log.d(tag, "✓ Location stored successfully")
                    } else {
                        Log.e(tag, "Failed to get fresh location")
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = "Unable to get your location. Please check:\n" +
                                    "1. GPS is enabled\n" +
                                    "2. Location permission is granted\n" +
                                    "3. Try moving to an open area"
                        )
                        return@launch
                    }
                }

                // Step 5: Calculate prayer times with the location
                if (storedLocation != null) {
                    Log.d(
                        tag,
                        "✓ Using location: ${storedLocation.latitude}, ${storedLocation.longitude}"
                    )

                    // Try to get address/city name
                    val address = try {
                        locationHelper.getCityName(
                            storedLocation.latitude,
                            storedLocation.longitude
                        )
                            ?: locationHelper.getAddressFromLocation(
                                storedLocation.latitude,
                                storedLocation.longitude
                            )
                    } catch (e: Exception) {
                        Log.w(tag, "Failed to get address: ${e.message}")
                        null
                    }

                    val prayerTimes = calculatePrayerTimes(
                        latitude = storedLocation.latitude.toDouble(),
                        longitude = storedLocation.longitude.toDouble()
                    )

                    _uiState.value = _uiState.value.copy(
                        prayerTimes = prayerTimes,
                        isLoading = false,
                        currentLocation = storedLocation,
                        locationAddress = address,
                        error = null
                    )
                    Log.d(tag, "✓ Prayer times calculated successfully, Address: $address")
                } else {
                    Log.e(tag, "Location is still null after all attempts")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Failed to obtain location data"
                    )
                }
            } catch (e: Exception) {
                Log.e(tag, "Error in loadPrayerTimes", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to load prayer times: ${e.message}"
                )
            }
        }
    }

    private suspend fun calculatePrayerTimes(
        latitude: Double,
        longitude: Double,
    ): List<PrayerTimeData> {
        return try {
            // Create location object for the Muslim Data library
            val prayerLocation = dev.kosrat.muslimdata.models.Location(
                id = 1,
                name = "Current Location",
                latitude = latitude,
                longitude = longitude,
                countryCode = "US",
                countryName = "United States",
                hasFixedPrayerTime = false,
                prayerDependentId = null
            )

            val prayerAttribute = PrayerAttribute(
                calculationMethod = CalculationMethod.MAKKAH,
                asrMethod = AsrMethod.SHAFII,
                higherLatitudeMethod = HigherLatitudeMethod.ANGLE_BASED
            )

            // Initialize Muslim Repository and get prayer times
            val muslimRepository = dev.kosrat.muslimdata.repository.MuslimRepository(context)
            val prayerTimes = muslimRepository.getPrayerTimes(
                location = prayerLocation,
                date = Date(),
                attribute = prayerAttribute
            )

            // Convert to our data format with null safety
            if (prayerTimes != null) {
                val prayers = listOf(
                    PrayerTimeData("Fajr", formatTime(prayerTimes.fajr), prayerTimes.fajr),
                    PrayerTimeData("Sunrise", formatTime(prayerTimes.sunrise), prayerTimes.sunrise),
                    PrayerTimeData("Dhuhr", formatTime(prayerTimes.dhuhr), prayerTimes.dhuhr),
                    PrayerTimeData("Asr", formatTime(prayerTimes.asr), prayerTimes.asr),
                    PrayerTimeData("Maghrib", formatTime(prayerTimes.maghrib), prayerTimes.maghrib),
                    PrayerTimeData("Isha", formatTime(prayerTimes.isha), prayerTimes.isha)
                )

                // Calculate next and previous prayers
                calculateNextAndPreviousPrayers(prayers)

                prayers
            } else {
                Log.w(tag, "Prayer times calculation returned null")
                generateMockPrayerTimes()
            }

        } catch (e: Exception) {
            Log.e(tag, "Error calculating prayer times", e)
            generateMockPrayerTimes()
        }
    }

    private fun calculateNextAndPreviousPrayers(prayers: List<PrayerTimeData>) {
        val now = Date()
        var nextPrayer: PrayerTimeData? = null
        var lastPrayer: PrayerTimeData? = null

        // Find next prayer (first prayer after current time)
        for (prayer in prayers) {
            if (prayer.dateTime != null && prayer.dateTime.after(now)) {
                nextPrayer = prayer
                break
            }
        }

        // Find last prayer (last prayer before current time)
        for (i in prayers.indices.reversed()) {
            val prayer = prayers[i]
            if (prayer.dateTime != null && prayer.dateTime.before(now)) {
                lastPrayer = prayer
                break
            }
        }

        // Update UI state with next and previous prayers
        _uiState.value = _uiState.value.copy(
            nextPrayer = nextPrayer,
            lastPrayer = lastPrayer
        )
    }

    private fun formatTime(date: Date): String {
        val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
        return timeFormat.format(date)
    }

    private fun generateMockPrayerTimes(): List<PrayerTimeData> {
        return listOf(
            PrayerTimeData("Fajr", "05:30 AM"),
            PrayerTimeData("Dhuhr", "12:15 PM"),
            PrayerTimeData("Asr", "03:45 PM"),
            PrayerTimeData("Maghrib", "06:20 PM"),
            PrayerTimeData("Isha", "07:45 PM")
        )
    }

    fun refreshPrayerTimes() {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        loadPrayerTimes()
    }
}

data class HomeUiState(
    val prayerTimes: List<PrayerTimeData> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val currentLocation: com.example.quranapp.data.model.Location? = null,
    val nextPrayer: PrayerTimeData? = null,
    val lastPrayer: PrayerTimeData? = null,
)

data class PrayerTimeData(
    val name: String,
    val time: String,
    val dateTime: Date? = null,
)
