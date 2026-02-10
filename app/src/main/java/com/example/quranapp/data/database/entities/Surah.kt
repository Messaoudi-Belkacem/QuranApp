package com.example.quranapp.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "surahs")
data class Surah(
    @PrimaryKey val id: Int,
    val nameArabic: String,
    val nameEnglish: String,
    val totalVerses: Int,
    val startPage: Int = 1,
    val startJuzz: Int = 1
)