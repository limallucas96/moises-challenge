package com.lucaslima.musicsearch.feature.library.data.repository

import com.lucaslima.musicsearch.core.domain.DataError
import com.lucaslima.musicsearch.core.domain.EmptyResult
import com.lucaslima.musicsearch.core.domain.Result
import com.lucaslima.musicsearch.feature.library.data.local.LibraryLocalDataSource
import com.lucaslima.musicsearch.feature.library.data.remote.CatalogRemoteDataSource
import com.lucaslima.musicsearch.feature.library.domain.Album
import com.lucaslima.musicsearch.feature.library.domain.AlbumRepository
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Clock
import kotlinx.coroutines.flow.Flow

internal class OfflineFirstAlbumRepository(
    private val remote: CatalogRemoteDataSource,
    private val local: LibraryLocalDataSource,
    private val clock: Clock,
) : AlbumRepository {

    override fun observeAlbum(albumId: Long): Flow<Album?> = local.observeAlbum(albumId)

    override suspend fun refreshAlbum(albumId: Long): EmptyResult<DataError> =
        when (val result = remote.album(albumId)) {
            is Result.Error -> result

            is Result.Success -> try {
                local.saveAlbum(result.data, clock.now())
                Result.Success(Unit)
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                Result.Error(DataError.Local.UNKNOWN)
            }
        }
}
