package com.lucaslima.musicsearch.feature.library.data.catalog

import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.containsExactlyInAnyOrder
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import com.lucaslima.musicsearch.core.domain.DataError
import com.lucaslima.musicsearch.feature.library.data.fakes.FakeCatalogRemoteDataSource
import com.lucaslima.musicsearch.feature.library.data.fakes.FakeClock
import com.lucaslima.musicsearch.feature.library.data.fakes.FakeLibraryLocalDataSource
import com.lucaslima.musicsearch.feature.library.data.fakes.song
import com.lucaslima.musicsearch.feature.library.data.player.KnownSongs
import kotlin.test.Test
import kotlin.time.Duration.Companion.minutes
import kotlinx.coroutines.test.runTest

class LibraryMediaCatalogTest {

    private val remote = FakeCatalogRemoteDataSource()
    private val local = FakeLibraryLocalDataSource()
    private val knownSongs = KnownSongs()
    private val clock = FakeClock()
    private val catalog = LibraryMediaCatalog(remote, local, knownSongs, clock, limit = 3)

    @Test
    fun recentes_vem_do_banco_do_mais_novo_para_o_mais_antigo() = runTest {
        local.markPlayed(song(1), clock.now())
        clock.advanceBy(1.minutes)
        local.markPlayed(song(2), clock.now())

        val items = catalog.items(LibraryMediaCatalog.RECENT)

        assertThat(items.map { it.id }).containsExactly("2", "1")
    }

    @Test
    fun musicas_sem_previa_ficam_fora_das_secoes() = runTest {
        local.markPlayed(song(1, previewUrl = null), clock.now())
        local.markPlayed(song(2), clock.now())

        val items = catalog.items(LibraryMediaCatalog.RECENT)

        assertThat(items.map { it.id }).containsExactly("2")
    }

    @Test
    fun ultima_busca_mostra_o_resultado_salvo_mais_recente() = runTest {
        remote.catalog = listOf(song(1), song(2))
        catalog.search("daft")
        clock.advanceBy(1.minutes)
        remote.catalog = listOf(song(3))
        catalog.search("punk")

        val items = catalog.items(LibraryMediaCatalog.LAST_SEARCH)

        assertThat(items.map { it.id }).containsExactly("3")
    }

    @Test
    fun busca_pede_a_primeira_pagina_e_salva_no_banco() = runTest {
        remote.catalog = listOf(song(1), song(2), song(3), song(4))

        val items = catalog.search("  daft punk ")

        assertThat(items.map { it.id }).containsExactly("1", "2", "3")
        assertThat(remote.searchCalls).containsExactly("daft punk" to 0)
        assertThat(local.searchPages.values.single().nextPage).isEqualTo(1)
    }

    @Test
    fun busca_vazia_nao_chama_a_rede() = runTest {
        val items = catalog.search("   ")

        assertThat(items).isEmpty()
        assertThat(remote.searchCalls).isEmpty()
    }

    @Test
    fun busca_sem_rede_devolve_lista_vazia() = runTest {
        remote.error = DataError.Network.NO_INTERNET

        assertThat(catalog.search("daft")).isEmpty()
    }

    @Test
    fun falha_ao_salvar_nao_impede_o_resultado() = runTest {
        remote.catalog = listOf(song(1))
        local.failWrites = true

        assertThat(catalog.search("daft").map { it.id }).containsExactly("1")
    }

    @Test
    fun itens_entregues_ficam_conhecidos_pelo_player() = runTest {
        remote.catalog = listOf(song(1), song(2, previewUrl = null))

        catalog.search("daft")

        assertThat(knownSongs.songs.value.keys.toList()).containsExactly("1")
        assertThat(knownSongs.songs.value["1"]).isEqualTo(song(1))
    }

    @Test
    fun tocar_e_buscar_avisam_a_secao_que_mudou() = runTest {
        remote.catalog = listOf(song(1))

        catalog.sectionChanges().test {
            // Uma emissão inicial por seção, ao assinar.
            assertThat(listOf(awaitItem(), awaitItem())).containsExactlyInAnyOrder(
                LibraryMediaCatalog.RECENT,
                LibraryMediaCatalog.LAST_SEARCH,
            )

            local.markPlayed(song(1), clock.now())
            assertThat(awaitItem()).isEqualTo(LibraryMediaCatalog.RECENT)

            catalog.search("daft")
            assertThat(awaitItem()).isEqualTo(LibraryMediaCatalog.LAST_SEARCH)
        }
    }

    @Test
    fun secao_desconhecida_fica_vazia() = runTest {
        assertThat(catalog.items("nope")).isEmpty()
    }
}
