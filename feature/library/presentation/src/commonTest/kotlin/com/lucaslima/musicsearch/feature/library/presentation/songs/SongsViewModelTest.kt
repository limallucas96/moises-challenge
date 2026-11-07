package com.lucaslima.musicsearch.feature.library.presentation.songs

import androidx.lifecycle.SavedStateHandle
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isNull
import assertk.assertions.isTrue
import com.lucaslima.musicsearch.core.testing.runViewModelTest
import com.lucaslima.musicsearch.feature.library.domain.PlayerState
import com.lucaslima.musicsearch.feature.library.presentation.fakes.FakePlayerController
import com.lucaslima.musicsearch.feature.library.presentation.fakes.FakeSongRepository
import com.lucaslima.musicsearch.feature.library.presentation.fakes.song
import kotlin.test.Test
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent

class SongsViewModelTest {

    private val repository = FakeSongRepository()
    private val player = FakePlayerController()

    private fun viewModel(savedStateHandle: SavedStateHandle = SavedStateHandle()) = SongsViewModel(
        songRepository = repository,
        playerController = player,
        savedStateHandle = savedStateHandle,
        searchDebounce = DEBOUNCE,
    )

    /** O fluxo de resultados só busca enquanto alguém coleta, como a tela faz. */
    private fun TestScope.collectResults(viewModel: SongsViewModel) {
        backgroundScope.launch { viewModel.searchResults.collect() }
    }

    @Test
    fun o_termo_volta_do_saved_state_handle_depois_da_morte_do_processo() = runViewModelTest {
        val viewModel = viewModel(SavedStateHandle(mapOf("query" to "daft")))

        assertThat(viewModel.state.value.query).isEqualTo("daft")
        assertThat(viewModel.state.value.isSearching).isTrue()
    }

    @Test
    fun digitar_atualiza_o_estado_e_guarda_o_termo() = runViewModelTest {
        val handle = SavedStateHandle()
        val viewModel = viewModel(handle)

        viewModel.onAction(SongsAction.OnQueryChange("daft"))

        assertThat(viewModel.state.value.query).isEqualTo("daft")
        assertThat(handle.get<String>("query")).isEqualTo("daft")
    }

    @Test
    fun a_busca_so_dispara_quando_o_usuario_para_de_digitar() = runViewModelTest {
        val viewModel = viewModel()
        collectResults(viewModel)

        viewModel.onAction(SongsAction.OnQueryChange("d"))
        advanceTimeBy(DEBOUNCE / 2)
        viewModel.onAction(SongsAction.OnQueryChange("da"))
        advanceTimeBy(DEBOUNCE / 2)
        viewModel.onAction(SongsAction.OnQueryChange("daft"))
        advanceTimeBy(DEBOUNCE + 1.milliseconds)

        assertThat(repository.searchedTerms).containsExactly("", "daft")
    }

    @Test
    fun espacos_nas_pontas_nao_disparam_outra_busca() = runViewModelTest {
        val viewModel = viewModel()
        collectResults(viewModel)

        viewModel.onAction(SongsAction.OnQueryChange("daft"))
        advanceTimeBy(DEBOUNCE + 1.milliseconds)
        viewModel.onAction(SongsAction.OnQueryChange(" daft "))
        advanceTimeBy(DEBOUNCE + 1.milliseconds)

        assertThat(repository.searchedTerms).containsExactly("", "daft")
    }

    @Test
    fun limpar_a_busca_volta_aos_recentes_sem_esperar() = runViewModelTest {
        val viewModel = viewModel()
        collectResults(viewModel)
        viewModel.onAction(SongsAction.OnQueryChange("daft"))
        advanceTimeBy(DEBOUNCE + 1.milliseconds)

        viewModel.onAction(SongsAction.OnClearQueryClick)
        runCurrent()

        assertThat(viewModel.state.value.query).isEqualTo("")
        assertThat(viewModel.state.value.isSearching).isFalse()
        assertThat(repository.searchedTerms).containsExactly("", "daft", "")
    }

    @Test
    fun os_recentes_so_contam_como_carregados_depois_da_primeira_leitura() = runViewModelTest {
        val viewModel = viewModel()
        assertThat(viewModel.state.value.isRecentlyPlayedLoaded).isFalse()

        repository.recents.emit(emptyList())
        assertThat(viewModel.state.value.isRecentlyPlayedLoaded).isTrue()
        assertThat(viewModel.state.value.recentlyPlayed).isEmpty()

        repository.recents.emit(listOf(song(1), song(2)))
        assertThat(viewModel.state.value.recentlyPlayed).containsExactly(song(1), song(2))
    }

    @Test
    fun tocar_usa_a_lista_visivel_como_fila_comecando_na_musica_tocada() = runViewModelTest {
        val viewModel = viewModel()
        val queue = listOf(song(1), song(2), song(3))

        viewModel.onAction(SongsAction.OnSongClick(song(2), queue))

        assertThat(player.lastQueue).isEqualTo(queue)
        assertThat(player.lastStartIndex).isEqualTo(1)
    }

    @Test
    fun musica_fora_da_lista_toca_sozinha() = runViewModelTest {
        val viewModel = viewModel()

        viewModel.onAction(SongsAction.OnSongClick(song(9), listOf(song(1))))

        assertThat(player.lastQueue).isEqualTo(listOf(song(9)))
        assertThat(player.lastStartIndex).isEqualTo(0)
    }

    @Test
    fun a_musica_que_esta_tocando_fica_marcada() = runViewModelTest {
        val viewModel = viewModel()

        player.state.value = PlayerState(song = song(3), isPlaying = true)
        assertThat(viewModel.state.value.playingSongId).isEqualTo(3L)

        player.state.value = PlayerState()
        assertThat(viewModel.state.value.playingSongId).isNull()
    }

    @Test
    fun mais_opcoes_abre_o_sheet_e_fechar_limpa() = runViewModelTest {
        val viewModel = viewModel()

        viewModel.onAction(SongsAction.OnMoreClick(song(1)))
        assertThat(viewModel.state.value.optionsSong).isEqualTo(song(1))

        viewModel.onAction(SongsAction.OnOptionsDismiss)
        assertThat(viewModel.state.value.optionsSong).isNull()
    }

    @Test
    fun ver_album_fecha_o_sheet() = runViewModelTest {
        val viewModel = viewModel()
        viewModel.onAction(SongsAction.OnMoreClick(song(1)))

        viewModel.onAction(SongsAction.OnViewAlbumClick(song(1)))

        assertThat(viewModel.state.value.optionsSong).isNull()
    }

    private companion object {
        val DEBOUNCE = 300.milliseconds
    }
}
