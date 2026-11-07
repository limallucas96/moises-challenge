package com.lucaslima.musicsearch.feature.library.presentation.songs

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import com.lucaslima.musicsearch.core.designsystem.theme.AppTheme
import com.lucaslima.musicsearch.core.domain.DataError
import com.lucaslima.musicsearch.core.domain.DataErrorException
import com.lucaslima.musicsearch.core.testing.text
import com.lucaslima.musicsearch.feature.library.domain.Song
import com.lucaslima.musicsearch.feature.library.presentation.fakes.song
import com.lucaslima.musicsearch.feature.library.presentation.resources.Res
import com.lucaslima.musicsearch.feature.library.presentation.resources.recently_played
import com.lucaslima.musicsearch.feature.library.presentation.resources.recents_empty_title
import com.lucaslima.musicsearch.feature.library.presentation.resources.retry
import com.lucaslima.musicsearch.feature.library.presentation.resources.search_clear
import com.lucaslima.musicsearch.feature.library.presentation.resources.search_no_results_title
import com.lucaslima.musicsearch.feature.library.presentation.resources.search_offline_banner
import com.lucaslima.musicsearch.feature.library.presentation.resources.search_placeholder
import com.lucaslima.musicsearch.feature.library.presentation.resources.song_more_options
import com.lucaslima.musicsearch.feature.library.presentation.resources.songs_title
import com.lucaslima.musicsearch.feature.library.presentation.resources.view_album
import kotlin.test.Test
import kotlinx.coroutines.flow.flowOf
import org.junit.Rule

class SongsScreenDeviceTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val actions = mutableListOf<SongsAction>()

    private fun setScreen(
        state: SongsState,
        results: PagingData<Song> = PagingData.empty(),
    ) {
        composeRule.setContent {
            AppTheme { Screen(state, results) }
        }
        composeRule.waitForIdle()
    }

    @Composable
    private fun Screen(
        state: SongsState,
        results: PagingData<Song>,
    ) {
        SongsScreen(
            state = state,
            songs = flowOf(results).collectAsLazyPagingItems(),
            onAction = { actions += it },
        )
    }

    @Test
    fun sem_recentes_mostra_o_convite_para_buscar() {
        setScreen(SongsState(isRecentlyPlayedLoaded = true))

        composeRule.onNodeWithText(text(Res.string.songs_title)).assertIsDisplayed()
        composeRule.onNodeWithText(text(Res.string.recents_empty_title)).assertIsDisplayed()
    }

    @Test
    fun mostra_os_recentes_e_tocar_um_usa_os_recentes_como_fila() {
        val recents = listOf(song(1, title = "One More Time"), song(2, title = "Aerodynamic"))
        setScreen(SongsState(recentlyPlayed = recents, isRecentlyPlayedLoaded = true))

        composeRule.onNodeWithText(text(Res.string.recently_played)).assertIsDisplayed()
        composeRule.onNodeWithText("Aerodynamic").performClick()

        assertThat(actions).containsExactly(SongsAction.OnSongClick(recents[1], recents))
    }

    @Test
    fun digitar_no_campo_vira_acao_de_busca() {
        setScreen(SongsState(isRecentlyPlayedLoaded = true))

        composeRule.onNodeWithContentDescription(text(Res.string.search_placeholder)).performTextInput("daft")

        assertThat(actions).containsExactly(SongsAction.OnQueryChange("daft"))
    }

    @Test
    fun com_termo_o_botao_de_limpar_aparece_e_limpa() {
        setScreen(SongsState(query = "daft", isRecentlyPlayedLoaded = true))

        composeRule.onNodeWithContentDescription(text(Res.string.search_clear)).performClick()

        assertThat(actions).containsExactly(SongsAction.OnClearQueryClick)
    }

    @Test
    fun mostra_os_resultados_da_busca() {
        val results = listOf(song(1, title = "Get Lucky"), song(2, title = "Lose Yourself to Dance"))
        setScreen(SongsState(query = "daft", isRecentlyPlayedLoaded = true), PagingData.from(results))

        composeRule.onNodeWithText("Get Lucky").assertIsDisplayed()
        composeRule.onNodeWithText("Lose Yourself to Dance").performClick()

        assertThat(actions).containsExactly(SongsAction.OnSongClick(results[1], results))
    }

    @Test
    fun busca_sem_resultado_diz_que_nao_achou() {
        setScreen(
            SongsState(query = "zzzz", isRecentlyPlayedLoaded = true),
            PagingData.from(emptyList(), sourceLoadStates = loaded()),
        )

        composeRule.onNodeWithText(text(Res.string.search_no_results_title, "zzzz")).assertIsDisplayed()
    }

    @Test
    fun erro_sem_nada_salvo_mostra_o_erro_com_tentar_de_novo() {
        setScreen(
            SongsState(query = "daft", isRecentlyPlayedLoaded = true),
            PagingData.from(emptyList(), sourceLoadStates = refreshError()),
        )

        composeRule.onNodeWithText(text(Res.string.retry)).assertIsDisplayed()
    }

    @Test
    fun erro_com_resultados_salvos_mostra_os_salvos_e_o_aviso() {
        setScreen(
            SongsState(query = "daft", isRecentlyPlayedLoaded = true),
            PagingData.from(listOf(song(1, title = "Get Lucky")), sourceLoadStates = refreshError()),
        )

        composeRule.onNodeWithText("Get Lucky").assertIsDisplayed()
        composeRule.onNodeWithText(text(Res.string.search_offline_banner)).assertIsDisplayed()
    }

    @Test
    fun mais_opcoes_abre_o_sheet_com_ver_album() {
        val song = song(1, title = "Get Lucky")
        setScreen(SongsState(recentlyPlayed = listOf(song), isRecentlyPlayedLoaded = true))

        composeRule.onNodeWithContentDescription(text(Res.string.song_more_options, "Get Lucky")).performClick()
        assertThat(actions).containsExactly(SongsAction.OnMoreClick(song))
    }

    @Test
    fun o_sheet_aberto_leva_ao_album() {
        val song = song(1, title = "Get Lucky")
        setScreen(SongsState(isRecentlyPlayedLoaded = true, optionsSong = song))

        composeRule.onNodeWithText(text(Res.string.view_album)).performClick()

        assertThat(actions.last()).isEqualTo(SongsAction.OnViewAlbumClick(song))
    }

    /** Busca terminada, como o Pager informa depois da primeira página. */
    private fun loaded() = LoadStates(
        refresh = LoadState.NotLoading(endOfPaginationReached = false),
        prepend = LoadState.NotLoading(endOfPaginationReached = true),
        append = LoadState.NotLoading(endOfPaginationReached = true),
    )

    private fun refreshError() = LoadStates(
        refresh = LoadState.Error(DataErrorException(DataError.Network.NO_INTERNET)),
        prepend = LoadState.NotLoading(endOfPaginationReached = true),
        append = LoadState.NotLoading(endOfPaginationReached = true),
    )
}
