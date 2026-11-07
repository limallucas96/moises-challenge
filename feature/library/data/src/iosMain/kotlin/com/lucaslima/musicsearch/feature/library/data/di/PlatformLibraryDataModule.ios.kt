@file:OptIn(ExperimentalForeignApi::class)

package com.lucaslima.musicsearch.feature.library.data.di

import androidx.room.Room
import com.lucaslima.musicsearch.feature.library.data.local.LIBRARY_DATABASE_NAME
import com.lucaslima.musicsearch.feature.library.data.local.LibraryDatabase
import kotlinx.cinterop.ExperimentalForeignApi
import org.koin.core.module.Module
import org.koin.dsl.module
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

internal actual val platformLibraryDataModule: Module = module {
    single {
        LibraryDatabaseBuilder {
            val documents = NSFileManager.defaultManager.URLForDirectory(
                directory = NSDocumentDirectory,
                inDomain = NSUserDomainMask,
                appropriateForURL = null,
                create = false,
                error = null,
            )
            Room.databaseBuilder<LibraryDatabase>(
                name = requireNotNull(documents?.path) + "/" + LIBRARY_DATABASE_NAME,
            )
        }
    }
}
