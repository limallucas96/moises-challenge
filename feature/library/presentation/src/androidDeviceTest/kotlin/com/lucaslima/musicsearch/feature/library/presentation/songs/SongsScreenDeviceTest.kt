package com.lucaslima.musicsearch.feature.library.presentation.songs

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.lucaslima.musicsearch.core.designsystem.theme.AppTheme
import com.lucaslima.musicsearch.core.testing.text
import com.lucaslima.musicsearch.feature.library.presentation.resources.Res
import com.lucaslima.musicsearch.feature.library.presentation.resources.songs_title
import kotlin.test.Test
import org.junit.Rule

class SongsScreenDeviceTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun mostra_o_titulo_da_tela() {
        composeRule.setContent {
            AppTheme { SongsScreen(state = SongsState(), onAction = {}) }
        }

        composeRule.onNodeWithText(text(Res.string.songs_title)).assertIsDisplayed()
    }
}
