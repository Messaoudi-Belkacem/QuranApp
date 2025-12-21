package com.example.quranapp.presentation.screen.quran

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quranapp.data.database.entities.Surah
import com.example.quranapp.data.repository.QuranRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MushafUiState(
    val surahs: List<Surah> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class QuranViewModel @Inject constructor(
    private val quranRepository: QuranRepository
) : ViewModel() {

    private val tag = "QuranViewModel"
    private val _uiState = MutableStateFlow(MushafUiState())
    val uiState: StateFlow<MushafUiState> = _uiState.asStateFlow()

    init {
        Log.d(tag, "=== QuranViewModel initialized ===")
        loadSurahs()
    }

    private fun loadSurahs() {
        viewModelScope.launch {
            try {
                Log.d(tag, ">>> Starting to load Surahs...")
                _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
                Log.d(tag, ">>> isLoading set to true")

                // Check database count first
                var surahCount = quranRepository.getSurahCount()
                Log.d(tag, ">>> Database contains $surahCount Surahs")

                // If database is empty, wait for initialization (max 3 attempts)
                var attempts = 0
                while (surahCount == 0 && attempts < 3) {
                    attempts++
                    Log.w(tag, "⚠⚠⚠ Database is empty! Waiting for initialization... (Attempt $attempts/3)")
                    delay(2000) // Wait 2 seconds
                    surahCount = quranRepository.getSurahCount()
                    Log.d(tag, ">>> After waiting, database contains $surahCount Surahs")
                }

                if (surahCount == 0) {
                    Log.e(tag, "✗✗✗ Database is still empty after waiting. Triggering re-initialization...")
                    // Try to force initialization
                    quranRepository.initializeDatabase()
                    delay(1000)
                    surahCount = quranRepository.getSurahCount()
                    Log.d(tag, ">>> After re-initialization, database contains $surahCount Surahs")
                }

                quranRepository.getAllSurahs()
                    .catch { exception ->
                        Log.e(tag, "✗✗✗ Error loading Surahs from repository", exception)
                        Log.e(tag, "Error message: ${exception.message}")
                        Log.e(tag, "Error type: ${exception.javaClass.simpleName}")
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = exception.message ?: "Unknown error occurred"
                        )
                    }
                    .collect { surahs ->
                        Log.d(tag, ">>> Received ${surahs.size} Surahs from repository")
                        if (surahs.isEmpty()) {
                            Log.w(tag, "⚠ Surahs list is empty!")
                        } else {
                            Log.d(tag, "✓ First Surah: ${surahs.firstOrNull()?.name} (${surahs.firstOrNull()?.transliteration})")
                            Log.d(tag, "✓ Last Surah: ${surahs.lastOrNull()?.name} (${surahs.lastOrNull()?.transliteration})")
                        }
                        _uiState.value = _uiState.value.copy(
                            surahs = surahs,
                            isLoading = false,
                            errorMessage = null
                        )
                        Log.d(tag, ">>> UI State updated - isLoading: false, surahs count: ${surahs.size}")
                    }
            } catch (e: Exception) {
                Log.e(tag, "✗✗✗ Unexpected error in loadSurahs", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Failed to load Surahs"
                )
            }
        }
    }

    fun refreshSurahs() {
        Log.d(tag, ">>> Refresh requested")
        loadSurahs()
    }

    fun clearError() {
        Log.d(tag, ">>> Error cleared")
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
