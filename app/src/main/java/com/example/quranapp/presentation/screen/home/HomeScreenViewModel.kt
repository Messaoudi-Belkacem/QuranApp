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

/**
 * ViewModel for Home Screen
 * Manages prayer times, location, and user preferences
 */
@HiltViewModel
class HomeScreenViewModel @Inject constructor(
    private val quranRepository: QuranRepository,
    private val prayerSettingsRepository: PrayerSettingsRepository,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val locationHelper = LocationHelper(context)

    companion object {
        private const val TAG = "HomeScreenViewModel"
        private const val TIME_FORMAT = "hh:mm a"
    }

    init {
        loadUserPreferences()
        loadPrayerTimes()
    }

    /**
     * Load user's prayer calculation preferences
     */
    private fun loadUserPreferences() {
        _uiState.value = _uiState.value.copy(
            calculationMethod = prayerSettingsRepository.getCalculationMethod(),
            asrMethod = prayerSettingsRepository.getAsrMethod(),
            highLatMethod = prayerSettingsRepository.getHighLatitudeMethod()
        )
    }

    /**
     * Load prayer times using current or fresh location
     */
    private fun loadPrayerTimes() {
        viewModelScope.launch {
            try {
                Log.d(TAG, "=== Starting loadPrayerTimes ===")

                if (!validateLocationServices()) {
                    return@launch
                }

                val location = getOrFetchLocation() ?: return@launch

                calculateAndUpdatePrayerTimes(location)

            } catch (e: Exception) {
                Log.e(TAG, "Error in loadPrayerTimes", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to load prayer times: ${e.message}"
                )
            }
        }
    }

    /**
     * Validates location permission and services
     * @return true if valid, false otherwise
     */
    private fun validateLocationServices(): Boolean {
        if (!locationHelper.hasLocationPermission()) {
            Log.e(TAG, "Location permission not granted")
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                error = "Location permission is required. Please grant permission in settings."
            )
            return false
        }
        Log.d(TAG, "✓ Location permission granted")

        if (!locationHelper.isLocationEnabled()) {
            Log.e(TAG, "Location services are disabled")
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                error = "Location services are disabled. Please enable GPS in your device settings."
            )
            return false
        }
        Log.d(TAG, "✓ Location services enabled")

        return true
    }

    /**
     * Gets stored location or fetches fresh one if not available
     * @return Location object or null if unable to obtain
     */
    private suspend fun getOrFetchLocation(): com.example.quranapp.data.model.Location? {
        var storedLocation = quranRepository.getCurrentLocation()
        Log.d(TAG, "Stored location: $storedLocation")

        if (storedLocation == null) {
            Log.d(TAG, "No stored location found, fetching fresh location...")
            val freshLocation = locationHelper.getCurrentLocation()

            if (freshLocation != null) {
                val (latitude, longitude) = freshLocation
                Log.d(TAG, "✓ Fresh location obtained: $latitude, $longitude")

                quranRepository.setCurrentLocation(latitude, longitude)
                storedLocation = quranRepository.getCurrentLocation()
                Log.d(TAG, "✓ Location stored successfully")
            } else {
                Log.e(TAG, "Failed to get fresh location")
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Unable to get your location. Please check:\n" +
                            "1. GPS is enabled\n" +
                            "2. Location permission is granted\n" +
                            "3. Try moving to an open area"
                )
                return null
            }
        }

        return storedLocation
    }

    /**
     * Calculates prayer times and updates UI state with location info
     */
    private suspend fun calculateAndUpdatePrayerTimes(location: com.example.quranapp.data.model.Location) {
        Log.d(TAG, "✓ Using location: ${location.latitude}, ${location.longitude}")

        val address = getLocationAddress(location)
        val prayerTimes = calculatePrayerTimes(
            latitude = location.latitude.toDouble(),
            longitude = location.longitude.toDouble()
        )

        _uiState.value = _uiState.value.copy(
            prayerTimes = prayerTimes,
            isLoading = false,
            currentLocation = location,
            locationAddress = address,
            error = null
        )
        Log.d(TAG, "✓ Prayer times calculated successfully, Address: $address")
    }

    /**
     * Gets human-readable address from location coordinates
     */
    private suspend fun getLocationAddress(location: com.example.quranapp.data.model.Location): String? {
        return try {
            locationHelper.getCityName(location.latitude, location.longitude)
                ?: locationHelper.getAddressFromLocation(location.latitude, location.longitude)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to get address: ${e.message}")
            null
        }
    }

    /**
     * Calculate prayer times using Adhan library with user's preferences
     * @param latitude Location latitude
     * @param longitude Location longitude
     * @return List of prayer times or mock data if calculation fails
     */
    private fun calculatePrayerTimes(
        latitude: Double,
        longitude: Double,
    ): List<PrayerTimeData> {
        return try {
            val calculationMethod = _uiState.value.calculationMethod
            val asrMethod = _uiState.value.asrMethod
            val highLatMethod = _uiState.value.highLatMethod

            Log.d(TAG, "Using Adhan library with method: ${calculationMethod.displayName}, Asr: ${asrMethod.displayName}")

            val calculator = com.example.quranapp.util.AdhanPrayerTimeCalculator(
                latitude = latitude,
                longitude = longitude,
                calculationMethod = calculationMethod,
                asrMethod = asrMethod,
                highLatMethod = highLatMethod
            )

            val prayerTimesMap = calculator.getPrayerTimes(Date())

            val prayers = listOf(
                PrayerTimeData("Fajr", formatTime(prayerTimesMap["Fajr"]), prayerTimesMap["Fajr"]),
                PrayerTimeData("Sunrise", formatTime(prayerTimesMap["Sunrise"]), prayerTimesMap["Sunrise"]),
                PrayerTimeData("Dhuhr", formatTime(prayerTimesMap["Dhuhr"]), prayerTimesMap["Dhuhr"]),
                PrayerTimeData("Asr", formatTime(prayerTimesMap["Asr"]), prayerTimesMap["Asr"]),
                PrayerTimeData("Maghrib", formatTime(prayerTimesMap["Maghrib"]), prayerTimesMap["Maghrib"]),
                PrayerTimeData("Isha", formatTime(prayerTimesMap["Isha"]), prayerTimesMap["Isha"])
            )

            calculateNextAndPreviousPrayers(prayers)

            Log.d(TAG, "Prayer times calculated successfully using Adhan library with ${calculationMethod.displayName}")
            prayers

        } catch (e: Exception) {
            Log.e(TAG, "Error calculating prayer times with Adhan library", e)
            generateMockPrayerTimes()
        }
    }

    /**
     * Determines next and previous prayer times relative to current time
     * Updates UI state with calculated values
     */
    private fun calculateNextAndPreviousPrayers(prayers: List<PrayerTimeData>) {
        val now = Date()
        var nextPrayer: PrayerTimeData? = null
        var lastPrayer: PrayerTimeData? = null

        // Find first prayer after current time
        for (prayer in prayers) {
            if (prayer.dateTime != null && prayer.dateTime.after(now)) {
                nextPrayer = prayer
                break
            }
        }

        // Find last prayer before current time
        for (i in prayers.indices.reversed()) {
            val prayer = prayers[i]
            if (prayer.dateTime != null && prayer.dateTime.before(now)) {
                lastPrayer = prayer
                break
            }
        }

        _uiState.value = _uiState.value.copy(
            nextPrayer = nextPrayer,
            lastPrayer = lastPrayer
        )
    }

    /**
     * Formats date to time string
     */
    private fun formatTime(date: Date?): String {
        if (date == null) return "--:--"
        val timeFormat = SimpleDateFormat(TIME_FORMAT, Locale.getDefault())
        return timeFormat.format(date)
    }

    /**
     * Generates mock prayer times as fallback
     */
    private fun generateMockPrayerTimes(): List<PrayerTimeData> {
        return listOf(
            PrayerTimeData("Fajr", "05:30 AM"),
            PrayerTimeData("Dhuhr", "12:15 PM"),
            PrayerTimeData("Asr", "03:45 PM"),
            PrayerTimeData("Maghrib", "06:20 PM"),
            PrayerTimeData("Isha", "07:45 PM")
        )
    }

    // Public API methods

    /**
     * Refresh prayer times with current location
     */
    fun refreshPrayerTimes(showLoading: Boolean = true) {
        if (showLoading) {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        }
        loadPrayerTimes()
    }

    /**
     * Toggle prayer settings dialog visibility
     */
    fun showPrayerSettings(show: Boolean) {
        _uiState.value = _uiState.value.copy(showPrayerSettings = show)
    }

    /**
     * Update prayer calculation method and recalculate
     */
    fun updateCalculationMethod(method: PrayerCalculationMethod) {
        prayerSettingsRepository.setCalculationMethod(method)
        _uiState.value = _uiState.value.copy(calculationMethod = method)
        recalculatePrayerTimes()
    }

    /**
     * Update Asr calculation method and recalculate
     */
    fun updateAsrMethod(method: AsrCalculationMethod) {
        prayerSettingsRepository.setAsrMethod(method)
        _uiState.value = _uiState.value.copy(asrMethod = method)
        recalculatePrayerTimes()
    }

    /**
     * Update high latitude adjustment method and recalculate
     */
    fun updateHighLatMethod(method: HighLatitudeMethod) {
        prayerSettingsRepository.setHighLatitudeMethod(method)
        _uiState.value = _uiState.value.copy(highLatMethod = method)
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
                    Log.d(TAG, "Recalculating prayer times with updated settings...")

                    val prayerTimes = calculatePrayerTimes(
                        latitude = currentLocation.latitude.toDouble(),
                        longitude = currentLocation.longitude.toDouble()
                    )

                    _uiState.value = _uiState.value.copy(
                        prayerTimes = prayerTimes,
                        error = null
                    )

                    Log.d(TAG, "Prayer times recalculated successfully")
                } else {
                    Log.w(TAG, "No location available to recalculate prayer times")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error recalculating prayer times", e)
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
            Log.e(TAG, "Error updating widget", e)
        }
    }

    /**
     * Get fresh location directly from GPS
     * @return Pair of (latitude, longitude) or null if location cannot be obtained
     */
    private suspend fun getFreshLocation(): Pair<Double, Double>? {
        return try {
            Log.d(TAG, "=== Getting fresh location from GPS ===")

            if (!validateLocationServices()) {
                return null
            }

            val freshLocation = locationHelper.getCurrentLocation()

            if (freshLocation != null) {
                val (latitude, longitude) = freshLocation
                Log.d(TAG, "✓ Fresh location obtained: $latitude, $longitude")
                Pair(latitude.toDouble(), longitude.toDouble())
            } else {
                Log.e(TAG, "Failed to get fresh location from GPS")
                _uiState.value = _uiState.value.copy(
                    error = "Unable to get your location. Please check:\n" +
                            "1. GPS is enabled\n" +
                            "2. Location permission is granted\n" +
                            "3. Try moving to an open area"
                )
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting fresh location", e)
            _uiState.value = _uiState.value.copy(
                error = "Failed to get location: ${e.message}"
            )
            null
        }
    }

    /**
     * Refresh location with fresh GPS data and recalculate prayer times
     */
    fun refreshLocationAndPrayerTimes() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            val freshLocation = getFreshLocation()

            if (freshLocation != null) {
                val (latitude, longitude) = freshLocation

                // Store the new location
                quranRepository.setCurrentLocation(latitude.toFloat(), longitude.toFloat())
                val storedLocation = quranRepository.getCurrentLocation()

                // Get address
                val address = try {
                    locationHelper.getCityName(latitude.toFloat(), longitude.toFloat())
                        ?: locationHelper.getAddressFromLocation(latitude.toFloat(), longitude.toFloat())
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to get address: ${e.message}")
                    null
                }

                // Calculate prayer times
                val prayerTimes = calculatePrayerTimes(latitude, longitude)

                // Update UI state
                _uiState.value = _uiState.value.copy(
                    prayerTimes = prayerTimes,
                    isLoading = false,
                    currentLocation = storedLocation,
                    locationAddress = address,
                    error = null
                )

                // Update widget
                updateWidget()

                Log.d(TAG, "✓ Location and prayer times refreshed successfully")
                Log.d(TAG, "✓ Location stored: ${storedLocation?.latitude}, ${storedLocation?.longitude}")
                Log.d(TAG, "✓ Address: $address")
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

/**
 * UI State for Home Screen
 */
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

/**
 * Prayer time data model
 */
data class PrayerTimeData(
    val name: String,
    val time: String,
    val dateTime: Date? = null,
)
