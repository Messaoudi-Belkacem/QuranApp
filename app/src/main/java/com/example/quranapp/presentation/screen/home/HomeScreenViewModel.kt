package com.example.quranapp.presentation.screen.home

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quranapp.data.repository.QuranRepository
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
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadPrayerTimes()
    }

    private fun loadPrayerTimes() {
        viewModelScope.launch {
            try {
                // Get stored location from preferences
                val storedLocation = quranRepository.getCurrentLocation()

                if (storedLocation != null) {
                    Log.d("HomeScreenViewModel", "Location found: ${storedLocation.latitude}, ${storedLocation.longitude}")

                    // Calculate prayer times based on location
                    val prayerTimes = calculatePrayerTimes(
                        latitude = storedLocation.latitude.toDouble(),
                        longitude = storedLocation.longitude.toDouble()
                    )

                    _uiState.value = _uiState.value.copy(
                        prayerTimes = prayerTimes,
                        isLoading = false,
                        currentLocation = storedLocation
                    )
                } else {
                    Log.w("HomeScreenViewModel", "No location available")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Location not available. Please enable location services."
                    )
                }
            } catch (e: Exception) {
                Log.e("HomeScreenViewModel", "Error loading prayer times", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to load prayer times: ${e.message}"
                )
            }
        }
    }

    private suspend fun calculatePrayerTimes(latitude: Double, longitude: Double): List<PrayerTimeData> {
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
                listOf(
                    PrayerTimeData("Fajr", formatTime(prayerTimes.fajr)),
                    PrayerTimeData("Sunrise", formatTime(prayerTimes.sunrise)),
                    PrayerTimeData("Dhuhr", formatTime(prayerTimes.dhuhr)),
                    PrayerTimeData("Asr", formatTime(prayerTimes.asr)),
                    PrayerTimeData("Maghrib", formatTime(prayerTimes.maghrib)),
                    PrayerTimeData("Isha", formatTime(prayerTimes.isha))
                )
            } else {
                Log.w("HomeScreenViewModel", "Prayer times calculation returned null")
                generateMockPrayerTimes()
            }

        } catch (e: Exception) {
            Log.e("HomeScreenViewModel", "Error calculating prayer times", e)
            generateMockPrayerTimes()
        }
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
    val currentLocation: com.example.quranapp.data.model.Location? = null
)

data class PrayerTimeData(
    val name: String,
    val time: String
)
