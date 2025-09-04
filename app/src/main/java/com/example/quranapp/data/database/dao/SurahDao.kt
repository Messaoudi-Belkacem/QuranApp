package com.example.quranapp.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.quranapp.data.database.entities.Surah
import kotlinx.coroutines.flow.Flow

@Dao
interface SurahDao {
    @Query("SELECT * FROM surahs ORDER BY orderInQuran")
    fun getAllSurahs(): Flow<List<Surah>>

    @Query("SELECT * FROM surahs WHERE id = :id")
    fun getSurahById(id: Int): Flow<Surah>

    @Query("SELECT COUNT(*) FROM surahs")
    suspend fun getSurahCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSurah(surah: Surah)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSurahs(surahs: List<Surah>)
}