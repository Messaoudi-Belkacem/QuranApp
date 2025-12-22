package com.example.quranapp.domain.usecase

import com.example.quranapp.data.database.entities.Surah
import com.example.quranapp.data.repository.QuranRepository
import jakarta.inject.Inject

class GetSurahsUseCase @Inject constructor(
    private val repository: QuranRepository
) {
    suspend operator fun invoke(): List<Surah> = repository.getAllSurahs()
}