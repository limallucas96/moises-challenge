package com.lucaslima.musicsearch.feature.library.data.local

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

@Database(
    entities = [
        SongEntity::class,
        SearchResultEntity::class,
        SearchPageEntity::class,
        RecentlyPlayedEntity::class,
        AlbumEntity::class,
    ],
    version = 1,
)
@ConstructedBy(LibraryDatabaseConstructor::class)
internal abstract class LibraryDatabase : RoomDatabase() {
    abstract fun songDao(): SongDao
}

// O Room gera o `actual` de cada plataforma.
@Suppress("KotlinNoActualForExpect")
internal expect object LibraryDatabaseConstructor : RoomDatabaseConstructor<LibraryDatabase> {
    override fun initialize(): LibraryDatabase
}

internal const val LIBRARY_DATABASE_NAME = "library.db"

/** Mesma configuração nas duas plataformas: SQLite embutido e consultas fora da main thread. */
internal fun RoomDatabase.Builder<LibraryDatabase>.buildLibraryDatabase(): LibraryDatabase =
    setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .build()
