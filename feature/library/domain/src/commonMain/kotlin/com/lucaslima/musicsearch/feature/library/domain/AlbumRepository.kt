package com.lucaslima.musicsearch.feature.library.domain

import com.lucaslima.musicsearch.core.domain.DataError
import com.lucaslima.musicsearch.core.domain.EmptyResult
import kotlinx.coroutines.flow.Flow

/** Álbuns, offline-first: [observeAlbum] lê do banco e [refreshAlbum] o atualiza pela rede. */
interface AlbumRepository {
    /** Emite null enquanto o álbum não estiver no banco. */
    fun observeAlbum(albumId: Long): Flow<Album?>

    suspend fun refreshAlbum(albumId: Long): EmptyResult<DataError>
}
