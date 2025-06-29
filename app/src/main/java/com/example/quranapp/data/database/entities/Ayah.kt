package com.example.quranapp.data.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "ayahs",
    foreignKeys = [ForeignKey(
        entity = Surah::class,
        parentColumns = ["id"],
        childColumns = ["surahId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class Ayah(
    @PrimaryKey val id: Int,
    val surahId: Int,
    val ayahNumber: Int,
    val textArabic: String,
    val textTranslation: String,
    val juzNumber: Int,
    val hizbNumber: Int,
    val rukuNumber: Int
)