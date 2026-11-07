package com.lucaslima.musicsearch.feature.library.data.remote

import com.lucaslima.musicsearch.core.domain.DataError
import com.lucaslima.musicsearch.core.domain.Result
import com.lucaslima.musicsearch.feature.library.domain.Album
import com.lucaslima.musicsearch.feature.library.domain.Song

/**
 * Fonte remota do catálogo. Fala em modelos do domain, não em DTO: trocar a API (iTunes por
 * outra) é escrever outra implementação desta interface, sem tocar no repositório nem nas telas.
 */
internal interface CatalogRemoteDataSource {
    /** Página [page] (a partir de 0) da busca, com [pageSize] itens no máximo. */
    suspend fun searchSongs(
        term: String,
        page: Int,
        pageSize: Int,
    ): Result<SongPage, DataError.Network>

    suspend fun album(albumId: Long): Result<Album, DataError.Network>
}

internal data class SongPage(
    val songs: List<Song>,
    /** true quando não existe página seguinte. */
    val endReached: Boolean,
)
