package com.lucaslima.musicsearch.feature.alpha.presentation.di

import com.lucaslima.musicsearch.feature.alpha.presentation.AlphaViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val alphaPresentationModule = module {
    viewModelOf(::AlphaViewModel)
}
