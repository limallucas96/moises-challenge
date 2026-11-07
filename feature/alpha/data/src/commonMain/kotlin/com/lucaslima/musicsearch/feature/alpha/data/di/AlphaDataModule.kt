package com.lucaslima.musicsearch.feature.alpha.data.di

import com.lucaslima.musicsearch.feature.alpha.data.InMemoryGreetingRepository
import com.lucaslima.musicsearch.feature.alpha.domain.GreetingRepository
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val alphaDataModule = module {
    singleOf(::InMemoryGreetingRepository) { bind<GreetingRepository>() }
}
