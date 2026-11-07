package com.lucaslima.musicsearch.feature.library.presentation.fakes

import com.lucaslima.musicsearch.core.domain.DataError
import com.lucaslima.musicsearch.core.domain.EmptyResult
import com.lucaslima.musicsearch.core.domain.Result
import com.lucaslima.musicsearch.feature.library.domain.Album
import com.lucaslima.musicsearch.feature.library.domain.AlbumRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/** O "banco" é [saved]; [refreshResult] decide como a rede responde. */
class FakeAlbumRepository : AlbumRepository {
    val saved = MutableStateFlow<Album?>(null)
    var refreshResult: EmptyResult<DataError> = Result.Success(Unit)
    var refreshCount = 0

    /** O que a rede "devolve" quando dá certo: vai para o banco, como no repositório real. */
    var remoteAlbum: Album? = null

    override fun observeAlbum(albumId: Long): Flow<Album?> = saved

    override suspend fun refreshAlbum(albumId: Long): EmptyResult<DataError> {
        refreshCount++
        if (refreshResult is Result.Success) remoteAlbum?.let { saved.value = it }
        return refreshResult
    }
}
