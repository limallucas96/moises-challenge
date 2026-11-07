package com.lucaslima.musicsearch.feature.library.presentation.di

import com.lucaslima.musicsearch.feature.library.presentation.songs.SongsViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val libraryPresentationModule = module {
    viewModel { SongsViewModel(songRepository = get(), playerController = get(), savedStateHandle = get()) }
}
