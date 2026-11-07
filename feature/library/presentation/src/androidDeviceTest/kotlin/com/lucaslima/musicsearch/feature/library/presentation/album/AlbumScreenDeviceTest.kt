package com.lucaslima.musicsearch.feature.library.presentation.album

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import com.lucaslima.musicsearch.core.designsystem.theme.AppTheme
import com.lucaslima.musicsearch.core.domain.DataError
import com.lucaslima.musicsearch.core.testing.text
import com.lucaslima.musicsearch.feature.library.domain.Album
import com.lucaslima.musicsearch.feature.library.presentation.fakes.song
import com.lucaslima.musicsearch.feature.library.presentation.resources.Res
import com.lucaslima.musicsearch.feature.library.presentation.resources.back
import com.lucaslima.musicsearch.feature.library.presentation.resources.retry
import kotlin.test.Test
import org.junit.Rule

class AlbumScreenDeviceTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val actions = mutableListOf<AlbumAction>()
    private val album = Album(
        id = 100,
        title = "Discovery",
        artistName = "Daft Punk",
        artworkUrl = null,
        songs = listOf(song(1, title = "One More Time"), song(2, title = "Aerodynamic")),
    )

    private fun setScreen(state: AlbumState) {
        composeRule.setContent {
            AppTheme { AlbumScreen(state = state, onAction = { actions += it }) }
        }
        composeRule.waitForIdle()
    }

    @Test
    fun mostra_o_album_e_as_faixas() {
        setScreen(AlbumState(album = album, isLoading = false))

        // O título aparece na barra e no cabeçalho.
        assertThat(composeRule.onAllNodesWithText("Discovery").fetchSemanticsNodes().size).isEqualTo(2)
        composeRule.onNodeWithText("One More Time").assertIsDisplayed()
        composeRule.onNodeWithText("Aerodynamic").assertIsDisplayed()
    }

    @Test
    fun tocar_numa_faixa_e_voltar_viram_acoes() {
        setScreen(AlbumState(album = album, isLoading = false))

        composeRule.onNodeWithText("Aerodynamic").performClick()
        composeRule.onNodeWithContentDescription(text(Res.string.back)).performClick()

        assertThat(actions).containsExactly(AlbumAction.OnSongClick(album.songs[1]), AlbumAction.OnBackClick)
    }

    @Test
    fun erro_sem_album_salvo_permite_tentar_de_novo() {
        setScreen(AlbumState(isLoading = false, error = DataError.Network.NO_INTERNET))

        composeRule.onNodeWithText(text(Res.string.retry)).performClick()

        assertThat(actions).containsExactly(AlbumAction.OnRetryClick)
    }
}
