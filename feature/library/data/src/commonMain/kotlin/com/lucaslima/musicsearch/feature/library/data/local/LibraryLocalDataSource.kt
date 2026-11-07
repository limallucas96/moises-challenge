package com.lucaslima.musicsearch.feature.library.data.local

import androidx.paging.PagingSource
import com.lucaslima.musicsearch.feature.library.domain.Album
import com.lucaslima.musicsearch.feature.library.domain.Song
import kotlin.time.Instant
import kotlinx.coroutines.flow.Flow

/**
 * O banco local, atrás de uma interface para o RemoteMediator e os repositórios serem testados
 * com um fake em memória, em qualquer plataforma.
 */
internal interface LibraryLocalDataSource {
    fun searchPagingSource(queryKey: String): PagingSource<Int, SongEntity>

    /** Null quando a busca nunca foi feita. */
    suspend fun searchPage(queryKey: String): SearchPageEntity?

    /**
     * Grava uma página da busca. Com [replace], apaga antes os resultados anteriores do termo
     * (refresh). [nextPage] null marca o fim da lista.
     */
    suspend fun saveSearchPage(
        queryKey: String,
        songs: List<Song>,
        replace: Boolean,
        nextPage: Int?,
        updatedAt: Instant,
    )

    fun observeRecentlyPlayed(limit: Int): Flow<List<Song>>

    suspend fun markPlayed(
        song: Song,
        playedAt: Instant,
    )

    fun observeAlbum(albumId: Long): Flow<Album?>

    suspend fun saveAlbum(
        album: Album,
        updatedAt: Instant,
    )
}
