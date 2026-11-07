package com.lucaslima.musicsearch.feature.library.data.di

import android.content.Context
import androidx.room.Room
import com.lucaslima.musicsearch.feature.library.data.local.LIBRARY_DATABASE_NAME
import com.lucaslima.musicsearch.feature.library.data.local.LibraryDatabase
import org.koin.core.module.Module
import org.koin.dsl.module

internal actual val platformLibraryDataModule: Module = module {
    single {
        val context = get<Context>().applicationContext
        LibraryDatabaseBuilder {
            Room.databaseBuilder<LibraryDatabase>(
                context = context,
                name = context.getDatabasePath(LIBRARY_DATABASE_NAME).absolutePath,
            )
        }
    }
}
