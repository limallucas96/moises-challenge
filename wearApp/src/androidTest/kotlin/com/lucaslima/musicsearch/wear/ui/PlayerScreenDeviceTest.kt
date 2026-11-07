package com.lucaslima.musicsearch.wear.ui

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.wear.compose.material3.MaterialTheme
import assertk.assertThat
import assertk.assertions.containsExactly
import com.lucaslima.musicsearch.feature.library.domain.PlayerState
import com.lucaslima.musicsearch.feature.library.domain.RepeatMode
import com.lucaslima.musicsearch.feature.library.presentation.nowplaying.NowPlayingAction
import com.lucaslima.musicsearch.feature.library.presentation.nowplaying.NowPlayingState
import com.lucaslima.musicsearch.wear.R
import org.junit.Rule
import org.junit.Test

class PlayerScreenDeviceTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val actions = mutableListOf<NowPlayingAction>()

    private fun show(state: NowPlayingState) = composeRule.setContent {
        MaterialTheme { PlayerScreen(state = state, onAction = { actions += it }) }
    }

    @Test
    fun mostra_a_musica_tocando() {
        show(NowPlayingState(player = PlayerState(song = song(1, title = "One More Time"), isPlaying = true)))

        composeRule.onNodeWithText("One More Time").assertIsDisplayed()
        composeRule.onNodeWithText("Daft Punk").assertIsDisplayed()
        composeRule.onNodeWithContentDescription(text(R.string.pause)).assertIsDisplayed()
    }

    @Test
    fun controles_mandam_as_acoes_do_player() {
        show(NowPlayingState(player = PlayerState(song = song(1), hasNext = true)))

        composeRule.onNodeWithContentDescription(text(R.string.play)).performClick()
        composeRule.onNodeWithContentDescription(text(R.string.next)).performClick()
        composeRule.onNodeWithContentDescription(text(R.string.previous)).performClick()
        composeRule.onNodeWithContentDescription(text(R.string.repeat_off)).performClick()

        assertThat(actions).containsExactly(
            NowPlayingAction.OnPlayPauseClick,
            NowPlayingAction.OnNextClick,
            NowPlayingAction.OnPreviousClick,
            NowPlayingAction.OnRepeatClick,
        )
    }

    @Test
    fun ultima_musica_sem_repetir_desliga_o_proximo() {
        show(NowPlayingState(player = PlayerState(song = song(1), hasNext = false, repeatMode = RepeatMode.OFF)))

        composeRule.onNodeWithContentDescription(text(R.string.next)).assertIsNotEnabled()
    }

    @Test
    fun repetir_uma_aparece_para_o_leitor_de_tela() {
        show(NowPlayingState(player = PlayerState(song = song(1), repeatMode = RepeatMode.ONE)))

        composeRule.onNodeWithContentDescription(text(R.string.repeat_one)).assertIsDisplayed()
    }

    @Test
    fun sem_musica_mostra_o_aviso() {
        show(NowPlayingState())

        composeRule.onNodeWithText(text(R.string.nothing_playing)).assertIsDisplayed()
    }
}
