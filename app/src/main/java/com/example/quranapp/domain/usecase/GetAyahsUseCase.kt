package com.example.quranapp.domain.usecase

import com.example.quranapp.data.database.entities.Ayah
import com.example.quranapp.data.repository.QuranRepository
import jakarta.inject.Inject

class GetAyahsUseCase @Inject constructor(
    private val repository: QuranRepository
) {
    suspend operator fun invoke(surahId: Int): List<Ayah> =
        repository.getAyahsBySurah(surahId)
}