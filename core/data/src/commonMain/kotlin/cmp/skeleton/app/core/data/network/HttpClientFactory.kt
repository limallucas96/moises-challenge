package cmp.skeleton.app.core.data.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

data class NetworkConfig(
    val baseUrl: String,
    val enableLogging: Boolean = true,
)

val appJson: Json = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
    explicitNulls = false
}

/**
 * A engine vem de fora (skill android-data-layer): OkHttp/Darwin em produção,
 * MockEngine nos testes.
 */
object HttpClientFactory {
    fun create(
        engine: HttpClientEngine,
        config: NetworkConfig,
        tokenProvider: AuthTokenProvider,
    ): HttpClient = HttpClient(engine) {
        install(ContentNegotiation) { json(appJson) }

        if (config.enableLogging) {
            install(Logging) { level = LogLevel.INFO }
        }

        install(Auth) {
            bearer {
                loadTokens { tokenProvider.accessToken()?.let { BearerTokens(it, "") } }
                refreshTokens { tokenProvider.refreshAccessToken()?.let { BearerTokens(it, "") } }
            }
        }

        defaultRequest {
            url(config.baseUrl)
            contentType(ContentType.Application.Json)
        }
    }
}
