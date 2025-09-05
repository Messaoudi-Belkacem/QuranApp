package com.example.quranapp.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "surahs")
data class Surah(
    @PrimaryKey val id: Int,
    val name: String,
    val nameArabic: String,
    val nameEnglish: String,
    val ayahCount: Int,
    val revelationType: String // "Meccan" or "Medinan"
)