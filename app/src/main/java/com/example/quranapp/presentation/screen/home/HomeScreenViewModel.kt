package com.example.quranapp.presentation.screen.home

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quranapp.data.repository.PrayerSettingsRepository
import com.example.quranapp.data.repository.QuranRepository
import com.example.quranapp.domain.model.AsrCalculationMethod
import com.example.quranapp.domain.model.HighLatitudeMethod
import com.example.quranapp.domain.model.PrayerCalculationMethod
import com.example.quranapp.util.LocationHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
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
    private val prayerSettingsRepository: PrayerSettingsRepository,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    private val locationHelper = LocationHelper(context)
    private val tag = "HomeScreenViewModel"

    init {
        // Load user's prayer settings
        _uiState.value = _uiState.value.copy(
            calculationMethod = prayerSettingsRepository.getCalculationMethod(),
            asrMethod = prayerSettingsRepository.getAsrMethod(),
            highLatMethod = prayerSettingsRepository.getHighLatitudeMethod()
        )
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

    private fun calculatePrayerTimes(
        latitude: Double,
        longitude: Double,
    ): List<PrayerTimeData> {
        return try {
            // Get user's prayer calculation preferences from UI state
            val calculationMethod = _uiState.value.calculationMethod
            val asrMethod = _uiState.value.asrMethod
            val highLatMethod = _uiState.value.highLatMethod

            Log.d(
                tag,
                "Using Adhan library with method: ${calculationMethod.displayName}, Asr: ${asrMethod.displayName}"
            )

            // Create Adhan calculator with user preferences
            val calculator = com.example.quranapp.util.AdhanPrayerTimeCalculator(
                latitude = latitude,
                longitude = longitude,
                calculationMethod = calculationMethod,
                asrMethod = asrMethod,
                highLatMethod = highLatMethod
            )

            // Calculate prayer times using Adhan library
            val now = Date()
            val prayerTimesMap = calculator.getPrayerTimes(now)

            // Convert to list format
            val prayers = listOf(
                PrayerTimeData("Fajr", formatTime(prayerTimesMap["Fajr"]), prayerTimesMap["Fajr"]),
                PrayerTimeData(
                    "Sunrise",
                    formatTime(prayerTimesMap["Sunrise"]),
                    prayerTimesMap["Sunrise"]
                ),
                PrayerTimeData(
                    "Dhuhr",
                    formatTime(prayerTimesMap["Dhuhr"]),
                    prayerTimesMap["Dhuhr"]
                ),
                PrayerTimeData("Asr", formatTime(prayerTimesMap["Asr"]), prayerTimesMap["Asr"]),
                PrayerTimeData(
                    "Maghrib",
                    formatTime(prayerTimesMap["Maghrib"]),
                    prayerTimesMap["Maghrib"]
                ),
                PrayerTimeData("Isha", formatTime(prayerTimesMap["Isha"]), prayerTimesMap["Isha"])
            )

            // Calculate next and previous prayers
            calculateNextAndPreviousPrayers(prayers)

            Log.d(
                tag,
                "Prayer times calculated successfully using Adhan library with ${calculationMethod.displayName}"
            )
            prayers

        } catch (e: Exception) {
            Log.e(tag, "Error calculating prayer times with Adhan library", e)
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

    private fun formatTime(date: Date?): String {
        if (date == null) return "--:--"
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

    fun refreshPrayerTimes(showLoading: Boolean = true) {
        if (showLoading) {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        }
        loadPrayerTimes()
    }

    fun showPrayerSettings(show: Boolean) {
        _uiState.value = _uiState.value.copy(showPrayerSettings = show)
    }

    fun updateCalculationMethod(method: PrayerCalculationMethod) {
        prayerSettingsRepository.setCalculationMethod(method)
        _uiState.value = _uiState.value.copy(calculationMethod = method)
        // Recalculate immediately without showing loading spinner
        recalculatePrayerTimes()
    }

    fun updateAsrMethod(method: AsrCalculationMethod) {
        prayerSettingsRepository.setAsrMethod(method)
        _uiState.value = _uiState.value.copy(asrMethod = method)
        // Recalculate immediately without showing loading spinner
        recalculatePrayerTimes()
    }

    fun updateHighLatMethod(method: HighLatitudeMethod) {
        prayerSettingsRepository.setHighLatitudeMethod(method)
        _uiState.value = _uiState.value.copy(highLatMethod = method)
        // Recalculate immediately without showing loading spinner
        recalculatePrayerTimes()
    }

    /**
     * Recalculate prayer times with current location and updated settings
     * Does not show loading state for instant updates
     */
    private fun recalculatePrayerTimes() {
        viewModelScope.launch {
            try {
                val currentLocation = _uiState.value.currentLocation
                if (currentLocation != null) {
                    Log.d(tag, "Recalculating prayer times with updated settings...")

                    val prayerTimes = calculatePrayerTimes(
                        latitude = currentLocation.latitude.toDouble(),
                        longitude = currentLocation.longitude.toDouble()
                    )

                    _uiState.value = _uiState.value.copy(
                        prayerTimes = prayerTimes,
                        error = null
                    )

                    Log.d(tag, "Prayer times recalculated successfully")
                } else {
                    Log.w(tag, "No location available to recalculate prayer times")
                }
            } catch (e: Exception) {
                Log.e(tag, "Error recalculating prayer times", e)
                _uiState.value = _uiState.value.copy(
                    error = "Failed to update prayer times: ${e.message}"
                )
            }
        }
    }

    /**
     * Update home screen widget with latest prayer times
     */
    private fun updateWidget() {
        try {
            com.example.quranapp.widget.PrayerTimesWidgetProvider.requestWidgetUpdate(context)
        } catch (e: Exception) {
            Log.e(tag, "Error updating widget", e)
        }
    }

    /**
     * Get fresh location directly from GPS without using stored location
     * @return Pair of (latitude, longitude) or null if location cannot be obtained
     */
    suspend fun getFreshLocation(): Pair<Double, Double>? {
        return try {
            Log.d(tag, "=== Getting fresh location (bypassing storage) ===")

            // Check location permission
            if (!locationHelper.hasLocationPermission()) {
                Log.e(tag, "Location permission not granted")
                _uiState.value = _uiState.value.copy(
                    error = "Location permission is required. Please grant permission in settings."
                )
                return null
            }

            // Check if location services are enabled
            if (!locationHelper.isLocationEnabled()) {
                Log.e(tag, "Location services are disabled")
                _uiState.value = _uiState.value.copy(
                    error = "Location services are disabled. Please enable GPS in your device settings."
                )
                return null
            }

            // Get fresh location from GPS
            val freshLocation = locationHelper.getCurrentLocation()

            if (freshLocation != null) {
                val (latitude, longitude) = freshLocation
                Log.d(tag, "✓ Fresh location obtained: $latitude, $longitude")
                Pair(latitude.toDouble(), longitude.toDouble())
            } else {
                Log.e(tag, "Failed to get fresh location from GPS")
                _uiState.value = _uiState.value.copy(
                    error = "Unable to get your location. Please check:\n" +
                            "1. GPS is enabled\n" +
                            "2. Location permission is granted\n" +
                            "3. Try moving to an open area"
                )
                null
            }
        } catch (e: Exception) {
            Log.e(tag, "Error getting fresh location", e)
            _uiState.value = _uiState.value.copy(
                error = "Failed to get location: ${e.message}"
            )
            null
        }
    }

    /**
     * Update location with fresh GPS data and recalculate prayer times
     * Sets the location in both storage and UI state
     */
    fun refreshLocationAndPrayerTimes() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            val freshLocation = getFreshLocation()

            if (freshLocation != null) {
                val (latitude, longitude) = freshLocation

                // Store the new location in repository (convert Double to Float)
                quranRepository.setCurrentLocation(latitude.toFloat(), longitude.toFloat())

                // Get the stored location object from repository
                val storedLocation = quranRepository.getCurrentLocation()

                // Get address (convert Double to Float)
                val address = try {
                    locationHelper.getCityName(latitude.toFloat(), longitude.toFloat())
                        ?: locationHelper.getAddressFromLocation(latitude.toFloat(), longitude.toFloat())
                } catch (e: Exception) {
                    Log.w(tag, "Failed to get address: ${e.message}")
                    null
                }

                // Calculate prayer times with fresh location
                val prayerTimes = calculatePrayerTimes(latitude, longitude)

                // Update UI state with new location, address, and prayer times
                _uiState.value = _uiState.value.copy(
                    prayerTimes = prayerTimes,
                    isLoading = false,
                    currentLocation = storedLocation,
                    locationAddress = address,
                    error = null
                )

                // Update widget with new prayer times
                updateWidget()

                Log.d(tag, "✓ Location and prayer times refreshed successfully")
                Log.d(tag, "✓ Location stored: ${storedLocation?.latitude}, ${storedLocation?.longitude}")
                Log.d(tag, "✓ Address: $address")
            } else {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    /**
     * Expose prayer settings repository for notification toggle
     */
    fun getPrayerSettingsRepository(): PrayerSettingsRepository {
        return prayerSettingsRepository
    }
}

data class HomeUiState(
    val prayerTimes: List<PrayerTimeData> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val currentLocation: com.example.quranapp.data.model.Location? = null,
    val nextPrayer: PrayerTimeData? = null,
    val lastPrayer: PrayerTimeData? = null,
    val locationAddress: String? = null,
    val showPrayerSettings: Boolean = false,
    val calculationMethod: PrayerCalculationMethod = PrayerCalculationMethod.MWL,
    val asrMethod: AsrCalculationMethod = AsrCalculationMethod.SHAFII,
    val highLatMethod: HighLatitudeMethod = HighLatitudeMethod.ANGLE_BASED,
)

data class PrayerTimeData(
    val name: String,
    val time: String,
    val dateTime: Date? = null,
)
