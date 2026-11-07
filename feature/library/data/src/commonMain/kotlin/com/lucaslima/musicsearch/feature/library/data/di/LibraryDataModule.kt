package com.lucaslima.musicsearch.feature.library.data.di

import com.lucaslima.musicsearch.feature.library.data.local.LibraryDatabase
import com.lucaslima.musicsearch.feature.library.data.local.buildLibraryDatabase
import org.koin.dsl.module

val libraryDataModule = module {
    includes(platformLibraryDataModule)

    single { get<LibraryDatabaseBuilder>().builder().buildLibraryDatabase() }
    single { get<LibraryDatabase>().songDao() }
}
