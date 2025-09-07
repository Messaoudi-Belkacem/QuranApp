package com.example.quranapp.presentation.screen.home

import androidx.lifecycle.ViewModel
import com.example.quranapp.data.repository.QuranRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HomeScreenViewModel @Inject constructor(
    private val quranRepository: QuranRepository
) : ViewModel() {

}