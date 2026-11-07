package com.lucaslima.musicsearch.wear.ui

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.wear.compose.material3.MaterialTheme
import assertk.assertThat
import assertk.assertions.containsExactly
import com.lucaslima.musicsearch.core.domain.DataError
import com.lucaslima.musicsearch.feature.library.domain.Album
import com.lucaslima.musicsearch.feature.library.presentation.album.AlbumAction
import com.lucaslima.musicsearch.feature.library.presentation.album.AlbumState
import com.lucaslima.musicsearch.wear.R
import org.junit.Rule
import org.junit.Test

class AlbumScreensDeviceTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun lista_de_albuns_abre_o_album_tocado() {
        val opened = mutableListOf<Long>()
        composeRule.setContent {
            MaterialTheme {
                AlbumListScreen(
                    albums = listOf(song(1, albumId = 7, albumTitle = "Discovery")),
                    isLoaded = true,
                    onAlbumClick = { opened += it },
                )
            }
        }

        composeRule.onNodeWithText("Discovery").performClick()

        assertThat(opened).containsExactly(7L)
    }

    @Test
    fun sem_recentes_a_lista_de_albuns_avisa() {
        composeRule.setContent {
            MaterialTheme { AlbumListScreen(albums = emptyList(), isLoaded = true, onAlbumClick = {}) }
        }

        composeRule.onNodeWithText(text(R.string.albums_empty)).assertIsDisplayed()
    }

    @Test
    fun faixa_tocada_manda_a_musica() {
        val actions = mutableListOf<AlbumAction>()
        val track = song(2, title = "Digital Love")
        val album =
            Album(id = 100, title = "Discovery", artistName = "Daft Punk", artworkUrl = null, songs = listOf(track))
        composeRule.setContent {
            MaterialTheme {
                TrackListScreen(state = AlbumState(album = album, isLoading = false), onAction = {
                    actions +=
                        it
                })
            }
        }

        composeRule.onNodeWithText("Digital Love").performClick()

        assertThat(actions).containsExactly(AlbumAction.OnSongClick(track))
    }

    @Test
    fun erro_sem_album_salvo_oferece_tentar_de_novo() {
        val actions = mutableListOf<AlbumAction>()
        composeRule.setContent {
            MaterialTheme {
                TrackListScreen(
                    state = AlbumState(isLoading = false, error = DataError.Network.NO_INTERNET),
                    onAction = { actions += it },
                )
            }
        }

        composeRule.onNodeWithText(text(R.string.retry)).performClick()

        assertThat(actions).containsExactly(AlbumAction.OnRetryClick)
    }
}
