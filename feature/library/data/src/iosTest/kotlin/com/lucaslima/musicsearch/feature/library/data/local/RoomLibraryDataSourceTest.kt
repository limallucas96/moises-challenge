package com.lucaslima.musicsearch.feature.library.data.local

import androidx.paging.PagingSource
import androidx.room.Room
import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import com.lucaslima.musicsearch.feature.library.data.fakes.album
import com.lucaslima.musicsearch.feature.library.data.fakes.song
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.time.Instant
import kotlinx.coroutines.test.runTest

/**
 * O Room de verdade, em memória. Roda só no iOS: no Android o Room precisa de Context, e o
 * projeto não usa Robolectric. As consultas SQL são as mesmas nas duas plataformas.
 */
class RoomLibraryDataSourceTest {

    private val database = Room.inMemoryDatabaseBuilder<LibraryDatabase>(
        factory = { LibraryDatabaseConstructor.initialize() },
    ).buildLibraryDatabase()
    private val dataSource = RoomLibraryDataSource(database.songDao())
    private val now = Instant.fromEpochMilliseconds(1_000)

    @AfterTest
    fun tearDown() = database.close()

    private suspend fun searchIds(queryKey: String): List<Long> {
        val result = dataSource.searchPagingSource(queryKey)
            .load(PagingSource.LoadParams.Refresh(key = null, loadSize = 100, placeholdersEnabled = false))
        return (result as PagingSource.LoadResult.Page).data.map { it.id }
    }

    @Test
    fun paginas_da_busca_ficam_na_ordem_em_que_chegaram() = runTest {
        dataSource.saveSearchPage("q", listOf(song(3), song(1)), replace = true, nextPage = 1, updatedAt = now)
        dataSource.saveSearchPage("q", listOf(song(2)), replace = false, nextPage = null, updatedAt = now)

        assertThat(searchIds("q")).containsExactly(3L, 1L, 2L)
        assertThat(dataSource.searchPage("q")?.nextPage).isNull()
    }

    @Test
    fun refresh_troca_os_resultados_sem_afetar_outra_busca() = runTest {
        dataSource.saveSearchPage("a", listOf(song(1), song(2)), replace = true, nextPage = 1, updatedAt = now)
        dataSource.saveSearchPage("b", listOf(song(2)), replace = true, nextPage = null, updatedAt = now)

        dataSource.saveSearchPage("a", listOf(song(5)), replace = true, nextPage = null, updatedAt = now)

        assertThat(searchIds("a")).containsExactly(5L)
        assertThat(searchIds("b")).containsExactly(2L)
    }

    @Test
    fun recentes_ordenam_pelo_ultimo_play_e_respeitam_o_limite() = runTest {
        dataSource.markPlayed(song(1), Instant.fromEpochMilliseconds(1))
        dataSource.markPlayed(song(2), Instant.fromEpochMilliseconds(2))
        dataSource.markPlayed(song(3), Instant.fromEpochMilliseconds(3))
        dataSource.markPlayed(song(1), Instant.fromEpochMilliseconds(4))

        dataSource.observeRecentlyPlayed(limit = 2).test {
            assertThat(awaitItem().map { it.id }).containsExactly(1L, 3L)
        }
    }

    @Test
    fun atualizar_uma_musica_nao_apaga_ela_dos_recentes_nem_da_busca() = runTest {
        dataSource.saveSearchPage("q", listOf(song(1)), replace = true, nextPage = null, updatedAt = now)
        dataSource.markPlayed(song(1), now)

        dataSource.saveAlbum(album(id = 100, songs = listOf(song(1))), now)

        assertThat(searchIds("q")).containsExactly(1L)
        dataSource.observeRecentlyPlayed(limit = 10).test {
            assertThat(awaitItem().map { it.id }).containsExactly(1L)
        }
    }

    @Test
    fun album_aparece_so_depois_de_salvo_com_as_faixas_em_ordem() = runTest {
        dataSource.observeAlbum(100).test {
            assertThat(awaitItem()).isNull()

            val saved = album(id = 100, songs = listOf(song(2, trackNumber = 2), song(1, trackNumber = 1)))
            dataSource.saveAlbum(saved, now)

            assertThat(awaitItem()?.songs?.map { it.id }).isEqualTo(listOf(1L, 2L))
        }
    }
}
