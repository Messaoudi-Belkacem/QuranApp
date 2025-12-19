package com.example.quranapp.presentation.screen.qibla

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.quranapp.util.CompassSensorManager
import com.example.quranapp.util.LocationHelper
import com.example.quranapp.util.QiblaCalculator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class QiblaUiState(
    val qiblaBearing: Float = 0f,
    val deviceAzimuth: Float = 0f,
    val userLatitude: Double = 0.0,
    val userLongitude: Double = 0.0,
    val distanceToKaaba: Double = 0.0,
    val isLoadingLocation: Boolean = true,
    val locationError: String? = null,
    val hasSensors: Boolean = true,
    val isCalibrated: Boolean = false,
) {
    val rotationAngle: Float
        get() = qiblaBearing - deviceAzimuth
}

@HiltViewModel
class QiblaViewModel @Inject constructor(
    application: Application,
) : AndroidViewModel(application) {

    private val tag = "QiblaViewModel"
    private val locationHelper = LocationHelper(application)
    private val compassSensorManager = CompassSensorManager(application)

    private val _uiState =
        MutableStateFlow(QiblaUiState(hasSensors = compassSensorManager.hasSensors()))
    val uiState: StateFlow<QiblaUiState> = _uiState.asStateFlow()

    init {
        Log.d(tag, "QiblaViewModel initialized")
        startCompassUpdates()
        loadLocation()
    }

    /**
     * Start receiving compass sensor updates
     */
    private fun startCompassUpdates() {
        viewModelScope.launch {
            compassSensorManager.getAzimuthFlow()
                .catch { e ->
                    Log.e(tag, "Error receiving compass updates", e)
                }
                .collect { azimuth ->
                    _uiState.update { state ->
                        state.copy(
                            deviceAzimuth = azimuth,
                            isCalibrated = true
                        )
                    }
                }
        }
    }

    /**
     * Load user location and calculate Qibla bearing
     */
    fun loadLocation() {
        viewModelScope.launch {
            Log.d(tag, "Loading location...")
            _uiState.update { it.copy(isLoadingLocation = true, locationError = null) }

            try {
                val location = locationHelper.getCurrentLocation()

                if (location != null) {
                    val (lat, lon) = location
                    Log.d(tag, "Location loaded: $lat, $lon")

                    val qiblaBearing = QiblaCalculator.calculateQiblaBearing(
                        lat.toDouble(),
                        lon.toDouble()
                    ).toFloat()

                    val distance = QiblaCalculator.calculateDistanceToKaaba(
                        lat.toDouble(),
                        lon.toDouble()
                    )

                    _uiState.update { state ->
                        state.copy(
                            qiblaBearing = qiblaBearing,
                            userLatitude = lat.toDouble(),
                            userLongitude = lon.toDouble(),
                            distanceToKaaba = distance,
                            isLoadingLocation = false,
                            locationError = null
                        )
                    }

                    Log.d(
                        tag,
                        "Qibla bearing: $qiblaBearing°, Distance: ${
                            String.format(
                                "%.2f",
                                distance
                            )
                        } km"
                    )
                } else {
                    Log.w(tag, "Failed to get location")
                    _uiState.update { state ->
                        state.copy(
                            isLoadingLocation = false,
                            locationError = "Unable to get location. Please check permissions and GPS."
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e(tag, "Error loading location", e)
                _uiState.update { state ->
                    state.copy(
                        isLoadingLocation = false,
                        locationError = "Error: ${e.localizedMessage}"
                    )
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        Log.d(tag, "QiblaViewModel cleared")
    }
}

