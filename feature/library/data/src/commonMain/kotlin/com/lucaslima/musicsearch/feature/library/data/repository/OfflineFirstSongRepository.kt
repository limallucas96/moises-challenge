package com.lucaslima.musicsearch.feature.library.data.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.lucaslima.musicsearch.feature.library.data.local.LibraryLocalDataSource
import com.lucaslima.musicsearch.feature.library.data.local.toSong
import com.lucaslima.musicsearch.feature.library.data.remote.CatalogRemoteDataSource
import com.lucaslima.musicsearch.feature.library.domain.Song
import com.lucaslima.musicsearch.feature.library.domain.SongRepository
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

internal class OfflineFirstSongRepository(
    private val remote: CatalogRemoteDataSource,
    private val local: LibraryLocalDataSource,
    private val clock: Clock,
    private val pageSize: Int = DEFAULT_PAGE_SIZE,
    private val cacheTimeout: Duration = DEFAULT_CACHE_TIMEOUT,
) : SongRepository {

    @OptIn(ExperimentalPagingApi::class)
    override fun searchSongs(query: String): Flow<PagingData<Song>> {
        val term = query.trim()
        if (term.isEmpty()) return flowOf(PagingData.empty())
        val queryKey = term.toQueryKey()
        return Pager(
            config = PagingConfig(
                pageSize = pageSize,
                initialLoadSize = pageSize,
                prefetchDistance = pageSize / 2,
                enablePlaceholders = false,
            ),
            remoteMediator = SearchSongsRemoteMediator(
                term = term,
                queryKey = queryKey,
                remote = remote,
                local = local,
                clock = clock,
                pageSize = pageSize,
                cacheTimeout = cacheTimeout,
            ),
            pagingSourceFactory = { local.searchPagingSource(queryKey) },
        ).flow.map { page -> page.map { it.toSong() } }
    }

    override fun recentlyPlayed(limit: Int): Flow<List<Song>> = local.observeRecentlyPlayed(limit)

    companion object {
        const val DEFAULT_PAGE_SIZE = 25
        val DEFAULT_CACHE_TIMEOUT = 1.hours
    }
}

/** "Daft Punk " e "daft punk" são a mesma busca no cache. */
internal fun String.toQueryKey(): String = trim().lowercase().replace(Regex("\\s+"), " ")
