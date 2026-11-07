package com.lucaslima.musicsearch.feature.library.presentation.nowplaying

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isNull
import assertk.assertions.isTrue
import com.lucaslima.musicsearch.core.testing.runViewModelTest
import com.lucaslima.musicsearch.feature.library.domain.PlayerState
import com.lucaslima.musicsearch.feature.library.domain.RepeatMode
import com.lucaslima.musicsearch.feature.library.presentation.fakes.FakePlayerController
import com.lucaslima.musicsearch.feature.library.presentation.fakes.song
import kotlin.test.Test
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.test.advanceTimeBy

class NowPlayingViewModelTest {

    private val player = FakePlayerController()

    private fun viewModel() = NowPlayingViewModel(playerController = player, seekSettleTimeout = SETTLE)

    @Test
    fun a_tela_mostra_o_que_o_player_esta_tocando() = runViewModelTest {
        val viewModel = viewModel()

        player.state.value = PlayerState(song = song(1), isPlaying = true, position = 5.seconds, duration = 30.seconds)

        assertThat(viewModel.state.value.song).isEqualTo(song(1))
        assertThat(viewModel.state.value.position).isEqualTo(5.seconds)
        assertThat(viewModel.state.value.player.isPlaying).isTrue()
    }

    @Test
    fun os_controles_vao_para_o_player() = runViewModelTest {
        val viewModel = viewModel()

        viewModel.onAction(NowPlayingAction.OnPlayPauseClick)
        viewModel.onAction(NowPlayingAction.OnNextClick)
        viewModel.onAction(NowPlayingAction.OnPreviousClick)
        viewModel.onAction(NowPlayingAction.OnRepeatClick)

        assertThat(player.commands).containsExactly("togglePlayPause", "next", "previous", "repeat")
    }

    @Test
    fun arrastar_o_slider_mostra_a_posicao_do_dedo_sem_buscar_ainda() = runViewModelTest {
        val viewModel = viewModel()
        player.state.value = PlayerState(song = song(1), position = 5.seconds, duration = 30.seconds)

        viewModel.onAction(NowPlayingAction.OnSeekChange(20.seconds))
        player.state.value = player.state.value.copy(position = 6.seconds)

        assertThat(viewModel.state.value.position).isEqualTo(20.seconds)
        assertThat(player.seeks).containsExactly()
    }

    @Test
    fun soltar_o_slider_busca_e_segura_a_posicao_ate_o_player_chegar() = runViewModelTest {
        val viewModel = viewModel()
        player.state.value = PlayerState(song = song(1), position = 5.seconds, duration = 30.seconds)
        viewModel.onAction(NowPlayingAction.OnSeekChange(20.seconds))

        viewModel.onAction(NowPlayingAction.OnSeekFinished)
        // O player ainda não chegou: a tela não volta para 5s.
        player.state.value = player.state.value.copy(position = 5.25.seconds)
        assertThat(viewModel.state.value.position).isEqualTo(20.seconds)

        player.state.value = player.state.value.copy(position = 20.2.seconds)
        assertThat(viewModel.state.value.scrubPosition).isNull()
        assertThat(viewModel.state.value.position).isEqualTo(20.2.seconds)
        assertThat(player.seeks).containsExactly(20.seconds)
    }

    @Test
    fun se_o_player_nao_confirmar_a_busca_a_tela_volta_a_segui_lo() = runViewModelTest {
        val viewModel = viewModel()
        player.state.value = PlayerState(song = song(1), position = 5.seconds, duration = 30.seconds)
        viewModel.onAction(NowPlayingAction.OnSeekChange(20.seconds))
        viewModel.onAction(NowPlayingAction.OnSeekFinished)

        advanceTimeBy(SETTLE + 1.milliseconds)

        assertThat(viewModel.state.value.scrubPosition).isNull()
        assertThat(viewModel.state.value.position).isEqualTo(5.seconds)
    }

    @Test
    fun soltar_sem_ter_arrastado_nao_busca() = runViewModelTest {
        val viewModel = viewModel()

        viewModel.onAction(NowPlayingAction.OnSeekFinished)

        assertThat(player.seeks).containsExactly()
    }

    @Test
    fun proxima_so_vale_com_proxima_na_fila_ou_repetindo_tudo() = runViewModelTest {
        val viewModel = viewModel()

        player.state.value = PlayerState(song = song(1), hasNext = false)
        assertThat(viewModel.state.value.canSkipNext).isFalse()

        player.state.value = PlayerState(song = song(1), hasNext = false, repeatMode = RepeatMode.ALL)
        assertThat(viewModel.state.value.canSkipNext).isTrue()

        player.state.value = PlayerState(song = song(1), hasNext = true)
        assertThat(viewModel.state.value.canSkipNext).isTrue()
    }

    @Test
    fun anterior_vale_sempre_que_ha_musica_porque_reinicia_a_atual() = runViewModelTest {
        val viewModel = viewModel()
        assertThat(viewModel.state.value.canSkipPrevious).isFalse()

        player.state.value = PlayerState(song = song(1), hasPrevious = false)

        assertThat(viewModel.state.value.canSkipPrevious).isTrue()
    }

    @Test
    fun mais_opcoes_abre_e_ver_album_fecha_o_sheet() = runViewModelTest {
        val viewModel = viewModel()

        viewModel.onAction(NowPlayingAction.OnMoreClick)
        assertThat(viewModel.state.value.isOptionsOpen).isTrue()

        viewModel.onAction(NowPlayingAction.OnViewAlbumClick(song(1)))
        assertThat(viewModel.state.value.isOptionsOpen).isFalse()
    }

    private companion object {
        val SETTLE = 1.seconds
    }
}
