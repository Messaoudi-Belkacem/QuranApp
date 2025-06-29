package com.example.quranapp.data.database.dao

import androidx.room.Dao
import androidx.room.Query
import com.example.quranapp.data.database.entities.Ayah
import kotlinx.coroutines.flow.Flow

@Dao
interface AyahDao {
    @Query("SELECT * FROM ayahs WHERE surahId = :surahId ORDER BY ayahNumber")
    fun getAyahsBySurah(surahId: Int): Flow<List<Ayah>>

    @Query("SELECT * FROM ayahs WHERE id = :id")
    suspend fun getAyahById(id: Int): Ayah?
}