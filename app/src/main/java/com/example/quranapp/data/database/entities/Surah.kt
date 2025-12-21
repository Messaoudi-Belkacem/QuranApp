package com.example.quranapp.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "surahs")
data class Surah(
    @PrimaryKey val id: Int,
    val name: String,
    val transliteration: String,
    val type: String,
    val totalVerses: Int,
)