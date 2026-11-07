package com.lucaslima.musicsearch.wear.ui

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.wear.compose.material3.MaterialTheme
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import com.lucaslima.musicsearch.feature.library.domain.Song
import com.lucaslima.musicsearch.feature.library.presentation.songs.SongsAction
import com.lucaslima.musicsearch.feature.library.presentation.songs.SongsState
import com.lucaslima.musicsearch.wear.R
import kotlinx.coroutines.flow.flowOf
import org.junit.Rule
import org.junit.Test

class SongListScreenDeviceTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val actions = mutableListOf<SongsAction>()
    private var searchClicks = 0

    private fun show(
        state: SongsState,
        results: List<Song> = emptyList(),
    ) = composeRule.setContent {
        val loaded = LoadStates(
            refresh = LoadState.NotLoading(endOfPaginationReached = true),
            prepend = LoadState.NotLoading(endOfPaginationReached = true),
            append = LoadState.NotLoading(endOfPaginationReached = true),
        )
        val items = flowOf(PagingData.from(results, sourceLoadStates = loaded)).collectAsLazyPagingItems()
        MaterialTheme {
            SongListScreen(
                state = state,
                results = items,
                onSearchClick = { searchClicks++ },
                onAction = { actions += it },
            )
        }
    }

    /** A tela redonda é pequena: a lista só compõe o que está à vista. */
    private fun scrollTo(label: String) {
        composeRule.onNode(hasScrollAction()).performScrollToNode(hasText(label))
    }

    @Test
    fun sem_busca_tocar_um_recente_toca_os_recentes() {
        val recents = listOf(song(1, title = "One More Time"), song(2, title = "Aerodynamic"))
        show(SongsState(recentlyPlayed = recents, isRecentlyPlayedLoaded = true))

        scrollTo("Aerodynamic")
        composeRule.onNodeWithText("Aerodynamic").performClick()

        assertThat(actions).containsExactly(SongsAction.OnSongClick(recents[1], recents))
    }

    @Test
    fun recentes_vazios_mostram_o_aviso() {
        show(SongsState(isRecentlyPlayedLoaded = true))

        composeRule.onNodeWithText(text(R.string.recents_empty)).assertIsDisplayed()
    }

    @Test
    fun botao_de_busca_abre_a_entrada_do_sistema() {
        show(SongsState(isRecentlyPlayedLoaded = true))

        composeRule.onNodeWithText(text(R.string.search)).performClick()

        assertThat(searchClicks).isEqualTo(1)
    }

    @Test
    fun busca_mostra_o_termo_e_limpar_volta_aos_recentes() {
        show(SongsState(query = "daft", isRecentlyPlayedLoaded = true), results = listOf(song(3, title = "Around")))

        composeRule.onNodeWithText("daft").assertIsDisplayed()
        composeRule.onNodeWithText(text(R.string.clear_search)).performClick()

        assertThat(actions).containsExactly(SongsAction.OnClearQueryClick)
    }

    @Test
    fun busca_sem_resultado_avisa() {
        show(SongsState(query = "zzz", isRecentlyPlayedLoaded = true))

        scrollTo(text(R.string.no_results))
        composeRule.onNodeWithText(text(R.string.no_results)).assertIsDisplayed()
    }
}
