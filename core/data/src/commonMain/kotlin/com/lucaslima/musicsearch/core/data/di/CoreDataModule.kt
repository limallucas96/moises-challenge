package com.lucaslima.musicsearch.core.data.di

import com.lucaslima.musicsearch.core.data.network.AuthTokenProvider
import com.lucaslima.musicsearch.core.data.network.HttpClientFactory
import com.lucaslima.musicsearch.core.data.network.NetworkConfig
import com.lucaslima.musicsearch.core.data.network.NoAuthTokenProvider
import com.lucaslima.musicsearch.core.data.network.appJson
import io.ktor.client.HttpClient
import kotlinx.serialization.json.Json
import org.koin.core.module.Module
import org.koin.dsl.module

fun coreDataModule(baseUrl: String): Module = module {
    includes(platformCoreDataModule)

    single { NetworkConfig(baseUrl = baseUrl) }
    single<Json> { appJson }
    single<HttpClient> {
        HttpClientFactory.create(
            engine = get(),
            config = get(),
            tokenProvider = getOrNull<AuthTokenProvider>() ?: NoAuthTokenProvider,
        )
    }
}
