package com.lucaslima.musicsearch.feature.library.domain

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow

/**
 * Músicas do catálogo, offline-first: o banco local é a fonte de verdade e a rede só o atualiza.
 * Falha de rede numa página chega como `LoadState.Error` com uma `DataErrorException`.
 */
interface SongRepository {
    /** Resultados paginados da busca. Termo em branco devolve uma lista vazia. */
    fun searchSongs(query: String): Flow<PagingData<Song>>

    /** As últimas músicas tocadas, da mais recente para a mais antiga. */
    fun recentlyPlayed(limit: Int = DEFAULT_RECENT_LIMIT): Flow<List<Song>>

    companion object {
        const val DEFAULT_RECENT_LIMIT = 20
    }
}
