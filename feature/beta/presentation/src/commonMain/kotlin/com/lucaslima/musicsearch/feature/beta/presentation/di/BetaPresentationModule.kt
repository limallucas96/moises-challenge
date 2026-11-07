package com.lucaslima.musicsearch.feature.beta.presentation.di

import com.lucaslima.musicsearch.feature.beta.presentation.BetaViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val betaPresentationModule = module {
    // O argumento da NavKey chega por parâmetro do Koin (koinViewModel { parametersOf(...) } no Root).
    viewModel { params -> BetaViewModel(message = params.get()) }
}
