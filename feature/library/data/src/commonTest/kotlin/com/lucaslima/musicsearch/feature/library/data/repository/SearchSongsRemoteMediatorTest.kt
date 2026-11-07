package com.lucaslima.musicsearch.feature.library.data.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingConfig
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isInstanceOf
import assertk.assertions.isNull
import assertk.assertions.isTrue
import assertk.assertions.prop
import com.lucaslima.musicsearch.core.domain.DataError
import com.lucaslima.musicsearch.core.domain.DataErrorException
import com.lucaslima.musicsearch.feature.library.data.fakes.FakeCatalogRemoteDataSource
import com.lucaslima.musicsearch.feature.library.data.fakes.FakeClock
import com.lucaslima.musicsearch.feature.library.data.fakes.FakeLibraryLocalDataSource
import com.lucaslima.musicsearch.feature.library.data.fakes.song
import com.lucaslima.musicsearch.feature.library.data.local.SongEntity
import kotlin.test.Test
import kotlin.time.Duration.Companion.minutes
import kotlinx.coroutines.test.runTest

@OptIn(ExperimentalPagingApi::class)
class SearchSongsRemoteMediatorTest {

    private val remote = FakeCatalogRemoteDataSource(catalog = (1L..5L).map { song(it) })
    private val local = FakeLibraryLocalDataSource()
    private val clock = FakeClock()

    private val mediator = SearchSongsRemoteMediator(
        term = "Daft Punk",
        queryKey = "daft punk",
        remote = remote,
        local = local,
        clock = clock,
        pageSize = 2,
        cacheTimeout = 60.minutes,
    )

    private val emptyState = PagingState<Int, SongEntity>(
        pages = emptyList(),
        anchorPosition = null,
        config = PagingConfig(pageSize = 2),
        leadingPlaceholderCount = 0,
    )

    private suspend fun load(type: LoadType) = mediator.load(type, emptyState)

    private fun savedIds() = local.searchResults["daft punk"].orEmpty().map { it.id }

    @Test
    fun busca_sem_cache_atualiza_ao_abrir() = runTest {
        assertThat(mediator.initialize()).isEqualTo(RemoteMediator.InitializeAction.LAUNCH_INITIAL_REFRESH)
    }

    @Test
    fun cache_recente_abre_sem_rede() = runTest {
        load(LoadType.REFRESH)
        clock.advanceBy(59.minutes)

        assertThat(mediator.initialize()).isEqualTo(RemoteMediator.InitializeAction.SKIP_INITIAL_REFRESH)
    }

    @Test
    fun cache_velho_atualiza_ao_abrir() = runTest {
        load(LoadType.REFRESH)
        clock.advanceBy(61.minutes)

        assertThat(mediator.initialize()).isEqualTo(RemoteMediator.InitializeAction.LAUNCH_INITIAL_REFRESH)
    }

    @Test
    fun refresh_grava_a_primeira_pagina_com_o_termo_original() = runTest {
        val result = load(LoadType.REFRESH)

        assertThat(result).isInstanceOf<RemoteMediator.MediatorResult.Success>()
            .prop(RemoteMediator.MediatorResult.Success::endOfPaginationReached)
            .isFalse()
        assertThat(savedIds()).containsExactly(1L, 2L)
        assertThat(local.searchPages.getValue("daft punk").nextPage).isEqualTo(1)
        assertThat(remote.searchCalls).containsExactly("Daft Punk" to 0)
    }

    @Test
    fun append_continua_de_onde_parou_ate_o_fim() = runTest {
        load(LoadType.REFRESH)
        load(LoadType.APPEND)
        val last = load(LoadType.APPEND)

        assertThat(savedIds()).containsExactly(1L, 2L, 3L, 4L, 5L)
        assertThat(last).isInstanceOf<RemoteMediator.MediatorResult.Success>()
            .prop(RemoteMediator.MediatorResult.Success::endOfPaginationReached)
            .isTrue()
        assertThat(local.searchPages.getValue("daft punk").nextPage).isNull()
    }

    @Test
    fun append_depois_do_fim_nao_chama_a_rede() = runTest {
        repeat(3) { load(if (it == 0) LoadType.REFRESH else LoadType.APPEND) }
        remote.searchCalls.clear()

        val result = load(LoadType.APPEND)

        assertThat(result).isInstanceOf<RemoteMediator.MediatorResult.Success>()
            .prop(RemoteMediator.MediatorResult.Success::endOfPaginationReached)
            .isTrue()
        assertThat(remote.searchCalls).isEmpty()
    }

    @Test
    fun refresh_substitui_os_resultados_anteriores() = runTest {
        load(LoadType.REFRESH)
        load(LoadType.APPEND)
        remote.catalog = listOf(song(9), song(8), song(7))

        load(LoadType.REFRESH)

        assertThat(savedIds()).containsExactly(9L, 8L)
    }

    @Test
    fun erro_de_rede_vira_erro_tipado_e_mantem_o_cache() = runTest {
        load(LoadType.REFRESH)
        remote.error = DataError.Network.NO_INTERNET

        val result = load(LoadType.REFRESH)

        assertThat(result).isInstanceOf<RemoteMediator.MediatorResult.Error>()
            .prop(RemoteMediator.MediatorResult.Error::throwable)
            .isInstanceOf<DataErrorException>()
            .prop(DataErrorException::error)
            .isEqualTo(DataError.Network.NO_INTERNET)
        assertThat(savedIds()).containsExactly(1L, 2L)
    }

    @Test
    fun falha_ao_gravar_vira_erro_local() = runTest {
        local.failWrites = true

        val result = load(LoadType.REFRESH)

        assertThat(result).isInstanceOf<RemoteMediator.MediatorResult.Error>()
            .prop(RemoteMediator.MediatorResult.Error::throwable)
            .isInstanceOf<DataErrorException>()
            .prop(DataErrorException::error)
            .isEqualTo(DataError.Local.UNKNOWN)
    }

    @Test
    fun prepend_nunca_busca() = runTest {
        val result = load(LoadType.PREPEND)

        assertThat(result).isInstanceOf<RemoteMediator.MediatorResult.Success>()
            .prop(RemoteMediator.MediatorResult.Success::endOfPaginationReached)
            .isTrue()
        assertThat(remote.searchCalls).isEmpty()
    }
}
