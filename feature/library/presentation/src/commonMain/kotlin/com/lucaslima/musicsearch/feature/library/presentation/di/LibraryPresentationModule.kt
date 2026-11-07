package com.lucaslima.musicsearch.feature.library.presentation.di

import com.lucaslima.musicsearch.feature.library.presentation.songs.SongsViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val libraryPresentationModule = module {
    viewModelOf(::SongsViewModel)
}
