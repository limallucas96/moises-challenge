package cmp.skeleton.app.feature.alpha.presentation.di

import cmp.skeleton.app.feature.alpha.presentation.AlphaViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val alphaPresentationModule = module {
    viewModelOf(::AlphaViewModel)
}
