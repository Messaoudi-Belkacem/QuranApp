package com.example.quranapp.data.database.dao

import androidx.room.Dao
import androidx.room.Query
import com.example.quranapp.data.database.entities.Surah
import kotlinx.coroutines.flow.Flow

@Dao
interface SurahDao {
    @Query("SELECT * FROM surahs ORDER BY orderInQuran")
    fun getAllSurahs(): Flow<List<Surah>>

    @Query("SELECT * FROM surahs WHERE id = :id")
    suspend fun getSurahById(id: Int): Surah?
}