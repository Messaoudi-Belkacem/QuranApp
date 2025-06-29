package com.example.quranapp.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.quranapp.data.database.dao.AyahDao
import com.example.quranapp.data.database.dao.BookmarkDao
import com.example.quranapp.data.database.dao.SurahDao
import com.example.quranapp.data.database.entities.Ayah
import com.example.quranapp.data.database.entities.Bookmark
import com.example.quranapp.data.database.entities.Surah

@Database(
    entities = [Surah::class, Ayah::class, Bookmark::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class QuranDatabase : RoomDatabase() {
    abstract fun surahDao(): SurahDao
    abstract fun ayahDao(): AyahDao
    abstract fun bookmarkDao(): BookmarkDao
}