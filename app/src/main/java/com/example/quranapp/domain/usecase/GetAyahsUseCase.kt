package com.example.quranapp.domain.usecase

import com.example.quranapp.data.database.entities.Ayah
import com.example.quranapp.data.repository.QuranRepository
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow

class GetAyahsUseCase @Inject constructor(
    private val repository: QuranRepository
) {
    operator fun invoke(surahId: Int): Flow<List<Ayah>> =
        repository.getAyahsBySurah(surahId)
}