package cmp.skeleton.app.feature.beta.presentation

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import assertk.assertThat
import assertk.assertions.isEqualTo
import cmp.skeleton.app.core.designsystem.theme.AppTheme
import cmp.skeleton.app.core.testing.text
import cmp.skeleton.app.feature.beta.presentation.resources.Res
import cmp.skeleton.app.feature.beta.presentation.resources.beta_back
import cmp.skeleton.app.feature.beta.presentation.resources.beta_tap
import cmp.skeleton.app.feature.beta.presentation.resources.beta_taps
import kotlin.test.Test
import org.junit.Rule

class BetaScreenDeviceTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private var backCount = 0

    private fun showBeta() {
        val viewModel = BetaViewModel(message = "Sent from Alpha")
        composeRule.setContent {
            AppTheme {
                BetaRoot(message = "Sent from Alpha", onBackClick = { backCount++ }, viewModel = viewModel)
            }
        }
    }

    @Test
    fun mostra_a_mensagem_recebida() {
        showBeta()

        composeRule.onNodeWithText("Sent from Alpha").assertIsDisplayed()
    }

    @Test
    fun toque_atualiza_o_contador() {
        showBeta()

        composeRule.onNodeWithText(text(Res.string.beta_tap)).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithText(text(Res.string.beta_taps, 1)).assertIsDisplayed()
    }

    @Test
    fun voltar_chama_o_callback() {
        showBeta()

        composeRule.onNodeWithText(text(Res.string.beta_back)).performClick()
        composeRule.waitForIdle()

        assertThat(backCount).isEqualTo(1)
    }
}
