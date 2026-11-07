package com.lucaslima.musicsearch.feature.library.data.di

import com.lucaslima.musicsearch.core.playback.MediaCatalog
import com.lucaslima.musicsearch.feature.library.data.catalog.LibraryMediaCatalog
import com.lucaslima.musicsearch.feature.library.data.local.LibraryDatabase
import com.lucaslima.musicsearch.feature.library.data.local.LibraryLocalDataSource
import com.lucaslima.musicsearch.feature.library.data.local.RoomLibraryDataSource
import com.lucaslima.musicsearch.feature.library.data.local.buildLibraryDatabase
import com.lucaslima.musicsearch.feature.library.data.player.KnownSongs
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

    singleOf(::KnownSongs)
    single<MediaCatalog> { LibraryMediaCatalog(remote = get(), local = get(), knownSongs = get(), clock = get()) }

    // Criado junto com o Koin, não na primeira tela: é ele que grava os recentes, e no Android
    // Auto a música toca sem nenhuma tela do app aberta.
    single<PlayerController>(createdAtStart = true) {
        MediaPlayerController(
            audioPlayer = get(),
            local = get(),
            clock = get(),
            knownSongs = get(),
            scope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
        )
    }
}
