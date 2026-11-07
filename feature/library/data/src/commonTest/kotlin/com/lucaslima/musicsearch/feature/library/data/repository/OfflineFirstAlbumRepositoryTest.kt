package com.lucaslima.musicsearch.feature.library.data.repository

import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import com.lucaslima.musicsearch.core.domain.DataError
import com.lucaslima.musicsearch.core.domain.Result
import com.lucaslima.musicsearch.feature.library.data.fakes.FakeCatalogRemoteDataSource
import com.lucaslima.musicsearch.feature.library.data.fakes.FakeClock
import com.lucaslima.musicsearch.feature.library.data.fakes.FakeLibraryLocalDataSource
import com.lucaslima.musicsearch.feature.library.data.fakes.album
import kotlin.test.Test
import kotlinx.coroutines.test.runTest

class OfflineFirstAlbumRepositoryTest {

    private val album = album(id = 100)
    private val remote = FakeCatalogRemoteDataSource(albums = mapOf(100L to album))
    private val local = FakeLibraryLocalDataSource()
    private val repository = OfflineFirstAlbumRepository(remote, local, FakeClock())

    @Test
    fun atualizar_grava_o_album_e_quem_observa_recebe() = runTest {
        repository.observeAlbum(100).test {
            assertThat(awaitItem()).isNull()

            assertThat(repository.refreshAlbum(100)).isEqualTo(Result.Success(Unit))

            assertThat(awaitItem()).isEqualTo(album)
        }
    }

    @Test
    fun sem_rede_devolve_o_erro_e_nao_apaga_o_cache() = runTest {
        repository.refreshAlbum(100)
        remote.error = DataError.Network.NO_INTERNET

        assertThat(repository.refreshAlbum(100)).isEqualTo(Result.Error(DataError.Network.NO_INTERNET))
        repository.observeAlbum(100).test {
            assertThat(awaitItem()).isEqualTo(album)
        }
    }

    @Test
    fun falha_ao_gravar_vira_erro_local() = runTest {
        local.failWrites = true

        assertThat(repository.refreshAlbum(100)).isEqualTo(Result.Error(DataError.Local.UNKNOWN))
    }
}
