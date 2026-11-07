package com.lucaslima.musicsearch.feature.library.data.repository

import androidx.paging.testing.asSnapshot
import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import com.lucaslima.musicsearch.feature.library.data.fakes.FakeCatalogRemoteDataSource
import com.lucaslima.musicsearch.feature.library.data.fakes.FakeClock
import com.lucaslima.musicsearch.feature.library.data.fakes.FakeLibraryLocalDataSource
import com.lucaslima.musicsearch.feature.library.data.fakes.song
import kotlin.test.Test
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.test.runTest

class OfflineFirstSongRepositoryTest {

    private val remote = FakeCatalogRemoteDataSource(catalog = (1L..30L).map { song(it) })
    private val local = FakeLibraryLocalDataSource()
    private val clock = FakeClock()
    private val repository = OfflineFirstSongRepository(remote, local, clock, pageSize = 10)

    @Test
    fun busca_vai_a_rede_grava_no_banco_e_mostra_do_banco() = runTest {
        val songs = repository.searchSongs("  Daft Punk ").asSnapshot()

        assertThat(songs.map { it.id }.take(10)).isEqualTo((1L..10L).toList())
        assertThat(local.searchResults.keys).isEqualTo(setOf("daft punk"))
        assertThat(remote.searchCalls.first()).isEqualTo("Daft Punk" to 0)
    }

    @Test
    fun rolar_ate_o_fim_carrega_as_paginas_seguintes() = runTest {
        val songs = repository.searchSongs("daft punk").asSnapshot {
            scrollTo(index = 29)
        }

        assertThat(songs.map { it.id }).isEqualTo((1L..30L).toList())
    }

    @Test
    fun termo_em_branco_nao_busca() = runTest {
        val songs = repository.searchSongs("   ").asSnapshot()

        assertThat(songs).isEmpty()
        assertThat(remote.searchCalls).isEmpty()
    }

    @Test
    fun recentes_vem_do_banco_do_mais_novo_para_o_mais_velho() = runTest {
        local.markPlayed(song(1), clock.now())
        clock.advanceBy(1.seconds)
        local.markPlayed(song(2), clock.now())

        repository.recentlyPlayed(limit = 10).test {
            assertThat(awaitItem().map { it.id }).containsExactly(2L, 1L)
        }
    }

    @Test
    fun chave_de_cache_ignora_caixa_e_espacos() {
        assertThat("  Daft   PUNK ".toQueryKey()).isEqualTo("daft punk")
    }
}
