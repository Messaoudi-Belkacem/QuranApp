package com.example.quranapp.presentation.screen.page

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Manages the immersive full-screen reading mode state.
 *
 * Controls visibility of top bars and overlays with tap-to-toggle
 * and automatic inactivity hiding.
 */
class ImmersiveModeController(
    private val coroutineScope: CoroutineScope,
    private val autoHideTimeoutMs: Long = 3500L
) {
    private val _isControlsVisible = MutableStateFlow(false)
    val isControlsVisible: StateFlow<Boolean> = _isControlsVisible.asStateFlow()

    private var autoHideJob: Job? = null

    /**
     * Toggles controls visibility. When showing, automatically schedules auto-hide.
     */
    fun toggle() {
        if (_isControlsVisible.value) {
            hide()
        } else {
            showTemporarily()
        }
    }

    /**
     * Shows controls and schedules an automatic hide after [autoHideTimeoutMs].
     */
    fun showTemporarily() {
        autoHideJob?.cancel()
        _isControlsVisible.value = true
        autoHideJob = coroutineScope.launch {
            delay(autoHideTimeoutMs)
            _isControlsVisible.value = false
        }
    }

    /**
     * Hides controls immediately and cancels any pending auto-hide job.
     */
    fun hide() {
        autoHideJob?.cancel()
        _isControlsVisible.value = false
    }
}
