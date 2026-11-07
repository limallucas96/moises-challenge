package com.lucaslima.musicsearch.feature.library.presentation.di

import com.lucaslima.musicsearch.feature.library.presentation.album.AlbumViewModel
import com.lucaslima.musicsearch.feature.library.presentation.nowplaying.NowPlayingViewModel
import com.lucaslima.musicsearch.feature.library.presentation.songs.SongsViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val libraryPresentationModule = module {
    viewModel { SongsViewModel(songRepository = get(), playerController = get(), savedStateHandle = get()) }
    viewModel { NowPlayingViewModel(playerController = get()) }
    viewModel { params -> AlbumViewModel(albumId = params.get(), albumRepository = get(), playerController = get()) }
}
