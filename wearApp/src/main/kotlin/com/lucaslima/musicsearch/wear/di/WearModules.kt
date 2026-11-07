package com.lucaslima.musicsearch.wear.di

import com.lucaslima.musicsearch.core.data.di.coreDataModule
import com.lucaslima.musicsearch.core.playback.di.platformPlaybackModule
import com.lucaslima.musicsearch.feature.library.data.di.libraryDataModule
import com.lucaslima.musicsearch.feature.library.presentation.di.libraryPresentationModule
import org.koin.core.module.Module

// A mesma API pública do iTunes que o :shared usa.
private const val API_BASE_URL = "https://itunes.apple.com/"

/**
 * Os mesmos módulos do celular, sem nada próprio: o relógio tem rede, banco e player locais e
 * usa os ViewModels da feature. Só a UI muda.
 */
fun wearModules(): List<Module> = listOf(
    coreDataModule(API_BASE_URL),
    platformPlaybackModule,
    libraryDataModule,
    libraryPresentationModule,
)
