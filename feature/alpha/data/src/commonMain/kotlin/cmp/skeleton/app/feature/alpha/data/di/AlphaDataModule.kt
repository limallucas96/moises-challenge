package cmp.skeleton.app.feature.alpha.data.di

import cmp.skeleton.app.feature.alpha.data.InMemoryGreetingRepository
import cmp.skeleton.app.feature.alpha.domain.GreetingRepository
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val alphaDataModule = module {
    singleOf(::InMemoryGreetingRepository) { bind<GreetingRepository>() }
}
