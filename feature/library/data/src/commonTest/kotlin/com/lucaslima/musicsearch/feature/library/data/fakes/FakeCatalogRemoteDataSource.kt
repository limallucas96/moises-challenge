package com.lucaslima.musicsearch.feature.library.data.fakes

import com.lucaslima.musicsearch.core.domain.DataError
import com.lucaslima.musicsearch.core.domain.Result
import com.lucaslima.musicsearch.feature.library.data.remote.CatalogRemoteDataSource
import com.lucaslima.musicsearch.feature.library.data.remote.SongPage
import com.lucaslima.musicsearch.feature.library.domain.Album
import com.lucaslima.musicsearch.feature.library.domain.Song

/** Catálogo em memória: [catalog] é a lista completa que a "API" pagina. */
internal class FakeCatalogRemoteDataSource(
    var catalog: List<Song> = emptyList(),
    var albums: Map<Long, Album> = emptyMap(),
) : CatalogRemoteDataSource {

    /** Quando não é null, toda chamada falha com este erro. */
    var error: DataError.Network? = null

    val searchCalls = mutableListOf<Pair<String, Int>>()
    var albumCalls = 0
        private set

    override suspend fun searchSongs(
        term: String,
        page: Int,
        pageSize: Int,
    ): Result<SongPage, DataError.Network> {
        searchCalls += term to page
        error?.let { return Result.Error(it) }
        val from = page * pageSize
        val songs = catalog.drop(from).take(pageSize)
        return Result.Success(SongPage(songs, endReached = from + pageSize >= catalog.size))
    }

    override suspend fun album(albumId: Long): Result<Album, DataError.Network> {
        albumCalls++
        error?.let { return Result.Error(it) }
        return albums[albumId]?.let { Result.Success(it) } ?: Result.Error(DataError.Network.NOT_FOUND)
    }
}
