package com.lucaslima.musicsearch.feature.library.data.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import com.lucaslima.musicsearch.core.domain.DataError
import com.lucaslima.musicsearch.core.domain.DataErrorException
import com.lucaslima.musicsearch.core.domain.Result
import com.lucaslima.musicsearch.feature.library.data.local.LibraryLocalDataSource
import com.lucaslima.musicsearch.feature.library.data.local.SongEntity
import com.lucaslima.musicsearch.feature.library.data.remote.CatalogRemoteDataSource
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Instant

/**
 * Busca a rede página a página e grava no banco; a tela lê só do banco. Uma busca já feita há
 * menos de [cacheTimeout] abre direto do cache, sem rede. Mais velha, abre do cache e atualiza
 * por cima. Sem rede, o cache continua na tela e o erro chega como `LoadState.Error`.
 */
@OptIn(ExperimentalPagingApi::class)
internal class SearchSongsRemoteMediator(
    private val term: String,
    private val queryKey: String,
    private val remote: CatalogRemoteDataSource,
    private val local: LibraryLocalDataSource,
    private val clock: Clock,
    private val pageSize: Int,
    private val cacheTimeout: Duration,
) : RemoteMediator<Int, SongEntity>() {

    override suspend fun initialize(): InitializeAction {
        val cached = local.searchPage(queryKey) ?: return InitializeAction.LAUNCH_INITIAL_REFRESH
        val age = clock.now() - Instant.fromEpochMilliseconds(cached.updatedAtMillis)
        return if (age <
            cacheTimeout
        ) {
            InitializeAction.SKIP_INITIAL_REFRESH
        } else {
            InitializeAction.LAUNCH_INITIAL_REFRESH
        }
    }

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, SongEntity>,
    ): MediatorResult {
        val page = when (loadType) {
            LoadType.REFRESH -> 0

            LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)

            LoadType.APPEND -> local.searchPage(queryKey)?.nextPage
                ?: return MediatorResult.Success(endOfPaginationReached = true)
        }

        return when (val result = remote.searchSongs(term, page, pageSize)) {
            is Result.Error -> MediatorResult.Error(DataErrorException(result.error))

            is Result.Success -> try {
                val songPage = result.data
                local.saveSearchPage(
                    queryKey = queryKey,
                    songs = songPage.songs,
                    replace = loadType == LoadType.REFRESH,
                    nextPage = if (songPage.endReached) null else page + 1,
                    updatedAt = clock.now(),
                )
                MediatorResult.Success(endOfPaginationReached = songPage.endReached)
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                MediatorResult.Error(DataErrorException(DataError.Local.UNKNOWN))
            }
        }
    }
}
