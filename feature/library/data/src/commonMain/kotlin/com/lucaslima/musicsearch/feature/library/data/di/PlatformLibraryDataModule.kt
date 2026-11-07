package com.lucaslima.musicsearch.feature.library.data.di

import androidx.room.RoomDatabase
import com.lucaslima.musicsearch.feature.library.data.local.LibraryDatabase
import org.koin.core.module.Module

/** Caminho do arquivo do banco, por plataforma. */
internal expect val platformLibraryDataModule: Module

internal fun interface LibraryDatabaseBuilder {
    fun builder(): RoomDatabase.Builder<LibraryDatabase>
}
