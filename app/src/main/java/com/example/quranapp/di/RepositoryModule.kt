package com.example.quranapp.di

import com.example.quranapp.data.repository.QuranRepository
import com.example.quranapp.data.repository.QuranRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun bindQuranRepository(
        quranRepositoryImpl: QuranRepositoryImpl
    ): QuranRepository
}
