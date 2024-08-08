package com.example.quranapp

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quranapp.data.Repository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SharedViewModel @Inject constructor(private val repository: Repository) : ViewModel() {
    private val tag: String = "SharedViewModel.kt"

    init {
        Log.d(tag, "SharedViewModel init is called")
        viewModelScope.launch {
            if (repository.isFirstLaunch()) {
                Log.d(tag, "This is first launch")
                repository.setFirstLaunch(false)
            } else {
                Log.d(tag, "This is NOT first launch")
            }
        }
    }
}