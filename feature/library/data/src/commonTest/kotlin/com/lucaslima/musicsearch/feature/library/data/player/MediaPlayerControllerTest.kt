package com.lucaslima.musicsearch.feature.library.data.player

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import com.lucaslima.musicsearch.feature.library.data.fakes.FakeAudioPlayer
import com.lucaslima.musicsearch.feature.library.data.fakes.FakeClock
import com.lucaslima.musicsearch.feature.library.data.fakes.FakeLibraryLocalDataSource
import com.lucaslima.musicsearch.feature.library.data.fakes.song
import com.lucaslima.musicsearch.feature.library.domain.RepeatMode
import kotlin.test.Test
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest

class MediaPlayerControllerTest {

    private val audioPlayer = FakeAudioPlayer()
    private val local = FakeLibraryLocalDataSource()

    private fun TestScope.controller() = MediaPlayerController(
        audioPlayer = audioPlayer,
        local = local,
        clock = FakeClock(),
        scope = backgroundScope,
    )

    @Test
    fun tocar_entrega_a_fila_ao_player_e_expoe_a_musica_atual() = runTest(UnconfinedTestDispatcher()) {
        val controller = controller()
        val queue = listOf(song(1), song(2), song(3))

        controller.play(queue, startIndex = 1)

        assertThat(audioPlayer.queue.map { it.id }).containsExactly("1", "2", "3")
        assertThat(controller.state.value.song).isEqualTo(song(2))
        assertThat(controller.state.value.isPlaying).isTrue()
    }

    @Test
    fun musicas_sem_previa_ficam_fora_da_fila_e_o_indice_acompanha() = runTest(UnconfinedTestDispatcher()) {
        val controller = controller()
        val queue = listOf(song(1, previewUrl = null), song(2), song(3, previewUrl = null), song(4))

        controller.play(queue, startIndex = 3)

        assertThat(audioPlayer.queue.map { it.id }).containsExactly("2", "4")
        assertThat(controller.state.value.song).isEqualTo(song(4))
    }

    @Test
    fun musica_sem_previa_nao_toca() = runTest(UnconfinedTestDispatcher()) {
        val controller = controller()

        controller.play(listOf(song(1, previewUrl = null)), startIndex = 0)
        controller.play(listOf(song(1)), startIndex = 5)

        assertThat(audioPlayer.queue).isEmpty()
    }

    @Test
    fun cada_musica_que_comeca_entra_nos_recentes_uma_vez() = runTest(UnconfinedTestDispatcher()) {
        val controller = controller()
        controller.play(listOf(song(1), song(2)), startIndex = 0)

        controller.togglePlayPause()
        controller.skipToNext()

        assertThat(local.playedSongIds).containsExactly(1L, 2L)
    }

    @Test
    fun falha_ao_gravar_recentes_nao_para_o_player() = runTest(UnconfinedTestDispatcher()) {
        local.failWrites = true
        val controller = controller()

        controller.play(listOf(song(1), song(2)), startIndex = 0)
        controller.skipToNext()

        assertThat(controller.state.value.song).isEqualTo(song(2))
    }

    @Test
    fun repetir_passa_pelos_modos_do_player() = runTest(UnconfinedTestDispatcher()) {
        val controller = controller()

        controller.toggleRepeat()

        assertThat(controller.state.value.repeatMode).isEqualTo(RepeatMode.ONE)
    }

    @Test
    fun erro_do_player_chega_ao_estado() = runTest(UnconfinedTestDispatcher()) {
        val controller = controller()
        controller.play(listOf(song(1)), startIndex = 0)

        audioPlayer.fail()

        assertThat(controller.state.value.hasError).isTrue()
        assertThat(controller.state.value.isPlaying).isFalse()
    }
}
