package com.example.quranapp.di

import android.content.Context
import androidx.room.Room
import com.example.quranapp.data.database.QuranDatabase
import com.example.quranapp.data.database.dao.AyahDao
import com.example.quranapp.data.database.dao.BookmarkDao
import com.example.quranapp.data.database.dao.SurahDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideQuranDatabase(
        @ApplicationContext context: Context
    ): QuranDatabase {
        return Room.databaseBuilder(
            context,
            QuranDatabase::class.java,
            "quran_database"
        ).build()
    }

    @Provides
    fun provideSurahDao(database: QuranDatabase): SurahDao {
        return database.surahDao()
    }

    @Provides
    fun provideAyahDao(database: QuranDatabase): AyahDao {
        return database.ayahDao()
    }

    @Provides
    fun provideBookmarkDao(database: QuranDatabase): BookmarkDao {
        return database.bookmarkDao()
    }
}
