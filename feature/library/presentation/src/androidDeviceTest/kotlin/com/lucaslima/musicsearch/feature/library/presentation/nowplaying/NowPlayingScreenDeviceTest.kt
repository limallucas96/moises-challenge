package com.lucaslima.musicsearch.feature.library.presentation.nowplaying

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import assertk.assertThat
import assertk.assertions.containsExactly
import com.lucaslima.musicsearch.core.designsystem.theme.AppTheme
import com.lucaslima.musicsearch.core.testing.text
import com.lucaslima.musicsearch.feature.library.domain.PlayerState
import com.lucaslima.musicsearch.feature.library.domain.RepeatMode
import com.lucaslima.musicsearch.feature.library.presentation.fakes.song
import com.lucaslima.musicsearch.feature.library.presentation.resources.Res
import com.lucaslima.musicsearch.feature.library.presentation.resources.next
import com.lucaslima.musicsearch.feature.library.presentation.resources.now_playing_empty_title
import com.lucaslima.musicsearch.feature.library.presentation.resources.now_playing_error
import com.lucaslima.musicsearch.feature.library.presentation.resources.now_playing_more_options
import com.lucaslima.musicsearch.feature.library.presentation.resources.pause
import com.lucaslima.musicsearch.feature.library.presentation.resources.play
import com.lucaslima.musicsearch.feature.library.presentation.resources.previous
import com.lucaslima.musicsearch.feature.library.presentation.resources.repeat_one
import com.lucaslima.musicsearch.feature.library.presentation.resources.view_album
import kotlin.test.Test
import kotlin.time.Duration.Companion.seconds
import org.junit.Rule

class NowPlayingScreenDeviceTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val actions = mutableListOf<NowPlayingAction>()

    private fun setScreen(state: NowPlayingState) {
        composeRule.setContent {
            AppTheme { NowPlayingScreen(state = state, onAction = { actions += it }) }
        }
        composeRule.waitForIdle()
    }

    private fun playing(
        hasNext: Boolean = true,
        repeatMode: RepeatMode = RepeatMode.OFF,
        hasError: Boolean = false,
    ) = NowPlayingState(
        player = PlayerState(
            song = song(1, title = "Get Lucky"),
            isPlaying = true,
            position = 86.seconds,
            duration = 260.seconds,
            hasNext = hasNext,
            repeatMode = repeatMode,
            hasError = hasError,
        ),
    )

    @Test
    fun mostra_a_musica_e_o_tempo_tocado_e_o_que_falta() {
        setScreen(playing())

        composeRule.onNodeWithText("Get Lucky").assertIsDisplayed()
        composeRule.onNodeWithText("1:26").assertIsDisplayed()
        composeRule.onNodeWithText("-2:54").assertIsDisplayed()
    }

    @Test
    fun os_controles_viram_acoes() {
        setScreen(playing())

        composeRule.onNodeWithContentDescription(text(Res.string.pause)).performClick()
        composeRule.onNodeWithContentDescription(text(Res.string.next)).performClick()
        composeRule.onNodeWithContentDescription(text(Res.string.previous)).performClick()

        assertThat(actions).containsExactly(
            NowPlayingAction.OnPlayPauseClick,
            NowPlayingAction.OnNextClick,
            NowPlayingAction.OnPreviousClick,
        )
    }

    @Test
    fun na_ultima_musica_sem_repetir_proxima_fica_desligada() {
        setScreen(playing(hasNext = false))

        composeRule.onNodeWithContentDescription(text(Res.string.next)).assertIsNotEnabled()
    }

    @Test
    fun o_modo_de_repetir_e_anunciado() {
        setScreen(playing(repeatMode = RepeatMode.ONE))

        composeRule.onNodeWithContentDescription(text(Res.string.repeat_one)).assertIsEnabled()
    }

    @Test
    fun pausado_o_botao_principal_e_play() {
        val paused = playing().let { it.copy(player = it.player.copy(isPlaying = false)) }
        setScreen(paused)

        composeRule.onNodeWithContentDescription(text(Res.string.play)).assertIsDisplayed()
    }

    @Test
    fun erro_de_reproducao_aparece_na_tela() {
        setScreen(playing(hasError = true))

        composeRule.onNodeWithText(text(Res.string.now_playing_error)).assertIsDisplayed()
    }

    @Test
    fun sem_musica_mostra_que_nada_esta_tocando() {
        setScreen(NowPlayingState())

        composeRule.onNodeWithText(text(Res.string.now_playing_empty_title)).assertIsDisplayed()
    }

    @Test
    fun mais_opcoes_vira_acao_e_o_sheet_aberto_leva_ao_album() {
        setScreen(playing().copy(isOptionsOpen = true))

        composeRule.onNodeWithText(text(Res.string.view_album)).performClick()

        assertThat(actions).containsExactly(NowPlayingAction.OnViewAlbumClick(song(1, title = "Get Lucky")))
    }

    @Test
    fun o_botao_de_mais_opcoes_vira_acao() {
        setScreen(playing())

        composeRule.onNodeWithContentDescription(text(Res.string.now_playing_more_options)).performClick()

        assertThat(actions).containsExactly(NowPlayingAction.OnMoreClick)
    }
}
