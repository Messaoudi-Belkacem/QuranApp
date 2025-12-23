package com.example.quranapp.presentation.screen.tasbih

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TasbihPreset(
    val id: String,
    val name: String,
    val arabicText: String,
    val translation: String,
    val defaultTarget: Int
)

data class TasbihSettings(
    val hapticFeedback: Boolean = true,
    val soundFeedback: Boolean = false,
    val autoReset: Boolean = false
)

data class TasbihUiState(
    val currentCount: Int = 0,
    val targetCount: Int = 33,
    val selectedPreset: TasbihPreset = getDefaultPresets()[0],
    val presets: List<TasbihPreset> = getDefaultPresets(),
    val settings: TasbihSettings = TasbihSettings(),
    val isTargetReached: Boolean = false,
    val showResetDialog: Boolean = false,
    val showPresetsDialog: Boolean = false,
    val showSettingsDialog: Boolean = false
)

private fun getDefaultPresets(): List<TasbihPreset> = listOf(
    TasbihPreset(
        id = "subhanallah",
        name = "Subhanallah",
        arabicText = "سُبْحَانَ اللّٰهِ",
        translation = "Glory be to Allah",
        defaultTarget = 33
    ),
    TasbihPreset(
        id = "alhamdulillah",
        name = "Alhamdulillah",
        arabicText = "الْحَمْدُ لِلّٰهِ",
        translation = "All praise is due to Allah",
        defaultTarget = 33
    ),
    TasbihPreset(
        id = "allahu_akbar",
        name = "Allahu Akbar",
        arabicText = "اللّٰهُ أَكْبَرُ",
        translation = "Allah is the Greatest",
        defaultTarget = 34
    ),
    TasbihPreset(
        id = "la_ilaha_illallah",
        name = "La ilaha illallah",
        arabicText = "لَا إِلٰهَ إِلَّا اللّٰهُ",
        translation = "There is no god but Allah",
        defaultTarget = 100
    ),
    TasbihPreset(
        id = "astaghfirullah",
        name = "Astaghfirullah",
        arabicText = "أَسْتَغْفِرُ اللّٰهَ",
        translation = "I seek forgiveness from Allah",
        defaultTarget = 100
    )
)

@HiltViewModel
class TasbihViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(TasbihUiState())
    val uiState: StateFlow<TasbihUiState> = _uiState.asStateFlow()

    fun incrementCount() {
        viewModelScope.launch {
            val currentState = _uiState.value
            val newCount = currentState.currentCount + 1
            val targetReached = newCount == currentState.targetCount

            _uiState.value = currentState.copy(
                currentCount = if (currentState.settings.autoReset && targetReached) 0 else newCount,
                isTargetReached = targetReached
            )

            // Reset target reached flag after animation
            if (targetReached && !currentState.settings.autoReset) {
                kotlinx.coroutines.delay(1500)
                _uiState.value = _uiState.value.copy(isTargetReached = false)
            }
        }
    }

    fun decrementCount() {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState.currentCount > 0) {
                _uiState.value = currentState.copy(
                    currentCount = currentState.currentCount - 1
                )
            }
        }
    }

    fun resetCount() {
        _uiState.value = _uiState.value.copy(
            currentCount = 0,
            isTargetReached = false
        )
    }

    fun updateTarget(target: Int) {
        _uiState.value = _uiState.value.copy(targetCount = target)
    }

    fun selectPreset(preset: TasbihPreset) {
        _uiState.value = _uiState.value.copy(
            selectedPreset = preset,
            targetCount = preset.defaultTarget
        )
    }

    fun updateSettings(settings: TasbihSettings) {
        _uiState.value = _uiState.value.copy(settings = settings)
    }

    fun showResetDialog(show: Boolean) {
        _uiState.value = _uiState.value.copy(showResetDialog = show)
    }

    fun showPresetsDialog(show: Boolean) {
        _uiState.value = _uiState.value.copy(showPresetsDialog = show)
    }

    fun showSettingsDialog(show: Boolean) {
        _uiState.value = _uiState.value.copy(showSettingsDialog = show)
    }
}

