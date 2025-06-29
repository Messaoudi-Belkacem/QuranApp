package com.example.quranapp.domain.model

import com.example.quranapp.data.database.entities.Ayah
import com.example.quranapp.data.database.entities.Bookmark
import com.example.quranapp.data.database.entities.Surah

// domain/model/QuranData.kt
data class SurahWithAyahs(
    val surah: Surah,
    val ayahs: List<Ayah>
)

data class BookmarkWithDetails(
    val bookmark: Bookmark,
    val surah: Surah,
    val ayah: Ayah
)