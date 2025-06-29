package com.example.quranapp.domain.usecase

import com.example.quranapp.data.database.entities.Surah
import com.example.quranapp.data.repository.QuranRepository
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow

class GetSurahsUseCase @Inject constructor(
    private val repository: QuranRepository
) {
    operator fun invoke(): Flow<List<Surah>> = repository.getAllSurahs()
}