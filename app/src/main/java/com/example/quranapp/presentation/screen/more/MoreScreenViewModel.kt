package com.example.quranapp.presentation.screen.more

import android.content.Context
import androidx.lifecycle.ViewModel
import com.example.quranapp.data.repository.QuranRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

@HiltViewModel
class MoreScreenViewModel @Inject constructor(
    private val quranRepository: QuranRepository,
    @ApplicationContext private val context: Context,
) : ViewModel() {

}