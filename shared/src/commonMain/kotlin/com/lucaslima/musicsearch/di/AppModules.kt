package com.lucaslima.musicsearch.di

import com.lucaslima.musicsearch.core.data.di.coreDataModule
import com.lucaslima.musicsearch.feature.library.data.di.libraryDataModule
import com.lucaslima.musicsearch.feature.library.presentation.di.libraryPresentationModule
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.KoinAppDeclaration
import org.koin.mp.KoinPlatformTools

// API pública do iTunes Search: sem chave e sem ambientes, então uma constante basta.
private const val API_BASE_URL = "https://itunes.apple.com/"

fun appModules(): List<Module> = listOf(
    coreDataModule(API_BASE_URL),
    libraryDataModule,
    libraryPresentationModule,
)

/** Idempotente: pode ser chamado de novo sem reiniciar o Koin. */
fun initKoin(config: KoinAppDeclaration? = null) {
    // GlobalContext só existe na JVM; o contexto padrão comum vem do KoinPlatformTools.
    if (KoinPlatformTools.defaultContext().getOrNull() != null) return
    startKoin {
        config?.invoke(this)
        modules(appModules())
    }
}
