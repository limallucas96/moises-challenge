package com.lucaslima.musicsearch.feature.library.presentation.album

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isNull
import com.lucaslima.musicsearch.core.domain.DataError
import com.lucaslima.musicsearch.core.domain.Result
import com.lucaslima.musicsearch.core.testing.runViewModelTest
import com.lucaslima.musicsearch.feature.library.domain.Album
import com.lucaslima.musicsearch.feature.library.domain.PlayerState
import com.lucaslima.musicsearch.feature.library.presentation.fakes.FakeAlbumRepository
import com.lucaslima.musicsearch.feature.library.presentation.fakes.FakePlayerController
import com.lucaslima.musicsearch.feature.library.presentation.fakes.song
import kotlin.test.Test

class AlbumViewModelTest {

    private val repository = FakeAlbumRepository()
    private val player = FakePlayerController()
    private val album = Album(
        id = 100,
        title = "Discovery",
        artistName = "Daft Punk",
        artworkUrl = null,
        songs = listOf(song(1), song(2), song(3)),
    )

    private fun viewModel() = AlbumViewModel(albumId = 100, albumRepository = repository, playerController = player)

    @Test
    fun o_album_salvo_aparece_e_a_rede_atualiza_por_tras() = runViewModelTest {
        repository.saved.value = album
        val viewModel = viewModel()

        assertThat(viewModel.state.value.album).isEqualTo(album)
        assertThat(viewModel.state.value.isLoading).isFalse()
        assertThat(repository.refreshCount).isEqualTo(1)
    }

    @Test
    fun sem_nada_salvo_o_album_chega_pela_rede() = runViewModelTest {
        repository.remoteAlbum = album
        val viewModel = viewModel()

        assertThat(viewModel.state.value.album).isEqualTo(album)
        assertThat(viewModel.state.value.error).isNull()
    }

    @Test
    fun falha_sem_nada_salvo_vira_erro_e_tentar_de_novo_busca_outra_vez() = runViewModelTest {
        repository.refreshResult = Result.Error(DataError.Network.NO_INTERNET)
        val viewModel = viewModel()
        assertThat(viewModel.state.value.error).isEqualTo(DataError.Network.NO_INTERNET)

        repository.refreshResult = Result.Success(Unit)
        repository.remoteAlbum = album
        viewModel.onAction(AlbumAction.OnRetryClick)

        assertThat(viewModel.state.value.error).isNull()
        assertThat(viewModel.state.value.album).isEqualTo(album)
        assertThat(repository.refreshCount).isEqualTo(2)
    }

    @Test
    fun tocar_uma_faixa_toca_o_album_a_partir_dela() = runViewModelTest {
        repository.saved.value = album
        val viewModel = viewModel()

        viewModel.onAction(AlbumAction.OnSongClick(song(2)))

        assertThat(player.lastQueue).isEqualTo(album.songs)
        assertThat(player.lastStartIndex).isEqualTo(1)
        assertThat(player.lastQueueTitle).isEqualTo("Discovery")
    }

    @Test
    fun a_faixa_que_esta_tocando_fica_marcada() = runViewModelTest {
        val viewModel = viewModel()

        player.state.value = PlayerState(song = song(3))

        assertThat(viewModel.state.value.playingSongId).isEqualTo(3L)
    }

    @Test
    fun tocar_a_faixa_que_ja_esta_no_player_nao_recomeca() = runViewModelTest {
        repository.saved.value = album
        val viewModel = viewModel()
        player.state.value = PlayerState(song = song(2), isPlaying = true)

        viewModel.onAction(AlbumAction.OnSongClick(song(2)))

        assertThat(player.lastQueue).isNull()
    }
}
