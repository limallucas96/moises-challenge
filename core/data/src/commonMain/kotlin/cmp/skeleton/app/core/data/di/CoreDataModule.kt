package cmp.skeleton.app.core.data.di

import cmp.skeleton.app.core.data.network.AuthTokenProvider
import cmp.skeleton.app.core.data.network.HttpClientFactory
import cmp.skeleton.app.core.data.network.NetworkConfig
import cmp.skeleton.app.core.data.network.NoAuthTokenProvider
import cmp.skeleton.app.core.data.network.appJson
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
