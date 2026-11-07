package com.lucaslima.musicsearch.feature.library.data.remote

import com.lucaslima.musicsearch.core.data.network.safeCall
import com.lucaslima.musicsearch.core.domain.DataError
import com.lucaslima.musicsearch.core.domain.Result
import com.lucaslima.musicsearch.feature.library.domain.Album
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.parameter

/**
 * Catálogo pela [iTunes Search API](https://performance-partners.apple.com/search-api).
 *
 * A API não pagina de verdade: o parâmetro `offset` é ignorado e o `limit` vai até 200. Como um
 * `limit` maior devolve sempre a mesma lista com mais itens no fim, a página N pede
 * `limit = (N + 1) * pageSize` e descarta os itens das páginas anteriores. Esse detalhe fica
 * contido aqui: para o resto do app, é uma fonte paginada comum.
 */
internal class ItunesCatalogDataSource(private val httpClient: HttpClient) : CatalogRemoteDataSource {

    override suspend fun searchSongs(
        term: String,
        page: Int,
        pageSize: Int,
    ): Result<SongPage, DataError.Network> {
        val requested = ((page + 1) * pageSize).coerceAtMost(MAX_LIMIT)
        val alreadyLoaded = page * pageSize
        if (alreadyLoaded >= requested) return Result.Success(SongPage(emptyList(), endReached = true))

        val response = safeCall<ItunesResponseDto> {
            httpClient.get(SEARCH_PATH) {
                parameter("term", term)
                parameter("media", "music")
                parameter("entity", "song")
                parameter("limit", requested)
            }
        }
        return when (response) {
            is Result.Error -> response

            is Result.Success -> {
                val results = response.data.results
                Result.Success(
                    SongPage(
                        songs = results.drop(alreadyLoaded).mapNotNull { it.toSongOrNull() },
                        endReached = results.size < requested || requested == MAX_LIMIT,
                    ),
                )
            }
        }
    }

    override suspend fun album(albumId: Long): Result<Album, DataError.Network> {
        val response = safeCall<ItunesResponseDto> {
            httpClient.get(LOOKUP_PATH) {
                parameter("id", albumId)
                parameter("entity", "song")
            }
        }
        return when (response) {
            is Result.Error -> response

            is Result.Success -> response.data.toAlbumOrNull(albumId)
                ?.let { Result.Success(it) }
                ?: Result.Error(DataError.Network.NOT_FOUND)
        }
    }

    private companion object {
        const val SEARCH_PATH = "search"
        const val LOOKUP_PATH = "lookup"

        /** Teto do `limit` na API do iTunes. */
        const val MAX_LIMIT = 200
    }
}
