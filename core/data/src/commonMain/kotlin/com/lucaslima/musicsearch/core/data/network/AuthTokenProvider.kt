package com.lucaslima.musicsearch.core.data.network

/**
 * Porta: :core:data precisa de um token, mas não conhece a feature de auth.
 * :feature:auth:data implementa e registra no Koin.
 */
interface AuthTokenProvider {
    suspend fun accessToken(): String?
    suspend fun refreshAccessToken(): String?
}

object NoAuthTokenProvider : AuthTokenProvider {
    override suspend fun accessToken(): String? = null
    override suspend fun refreshAccessToken(): String? = null
}
