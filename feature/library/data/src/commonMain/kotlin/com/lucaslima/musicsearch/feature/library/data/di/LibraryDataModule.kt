package com.lucaslima.musicsearch.feature.library.data.di

import com.lucaslima.musicsearch.feature.library.data.local.LibraryDatabase
import com.lucaslima.musicsearch.feature.library.data.local.LibraryLocalDataSource
import com.lucaslima.musicsearch.feature.library.data.local.RoomLibraryDataSource
import com.lucaslima.musicsearch.feature.library.data.local.buildLibraryDatabase
import com.lucaslima.musicsearch.feature.library.data.player.MediaPlayerController
import com.lucaslima.musicsearch.feature.library.data.remote.CatalogRemoteDataSource
import com.lucaslima.musicsearch.feature.library.data.remote.ItunesCatalogDataSource
import com.lucaslima.musicsearch.feature.library.data.repository.OfflineFirstAlbumRepository
import com.lucaslima.musicsearch.feature.library.data.repository.OfflineFirstSongRepository
import com.lucaslima.musicsearch.feature.library.domain.AlbumRepository
import com.lucaslima.musicsearch.feature.library.domain.PlayerController
import com.lucaslima.musicsearch.feature.library.domain.SongRepository
import kotlin.time.Clock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val libraryDataModule = module {
    includes(platformLibraryDataModule)

    single<Clock> { Clock.System }
    single { get<LibraryDatabaseBuilder>().builder().buildLibraryDatabase() }
    single { get<LibraryDatabase>().songDao() }

    singleOf(::RoomLibraryDataSource) { bind<LibraryLocalDataSource>() }
    singleOf(::ItunesCatalogDataSource) { bind<CatalogRemoteDataSource>() }

    single<SongRepository> { OfflineFirstSongRepository(remote = get(), local = get(), clock = get()) }
    singleOf(::OfflineFirstAlbumRepository) { bind<AlbumRepository>() }

    single<PlayerController> {
        MediaPlayerController(
            audioPlayer = get(),
            local = get(),
            clock = get(),
            scope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
        )
    }
}
