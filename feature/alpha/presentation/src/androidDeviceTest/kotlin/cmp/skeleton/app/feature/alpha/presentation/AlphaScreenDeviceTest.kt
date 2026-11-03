package cmp.skeleton.app.feature.alpha.presentation

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import assertk.assertThat
import assertk.assertions.containsExactly
import cmp.skeleton.app.core.designsystem.theme.AppTheme
import cmp.skeleton.app.core.testing.text
import cmp.skeleton.app.feature.alpha.presentation.resources.Res
import cmp.skeleton.app.feature.alpha.presentation.resources.alpha_beta_message
import cmp.skeleton.app.feature.alpha.presentation.resources.alpha_open_beta
import kotlin.test.Test
import org.junit.Rule

class AlphaScreenDeviceTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val opened = mutableListOf<String>()

    private fun showAlpha() {
        val viewModel = AlphaViewModel(FakeGreetingRepository())
        composeRule.setContent {
            AppTheme {
                AlphaRoot(onOpenBetaClick = { opened += it }, viewModel = viewModel)
            }
        }
    }

    @Test
    fun mostra_a_saudacao_carregada() {
        showAlpha()

        composeRule.onNodeWithText("Hello").assertIsDisplayed()
    }

    @Test
    fun abrir_beta_envia_a_mensagem_com_a_saudacao() {
        showAlpha()

        composeRule.onNodeWithText(text(Res.string.alpha_open_beta)).performClick()
        composeRule.waitForIdle()

        assertThat(opened).containsExactly(text(Res.string.alpha_beta_message, "Hello"))
    }
}
