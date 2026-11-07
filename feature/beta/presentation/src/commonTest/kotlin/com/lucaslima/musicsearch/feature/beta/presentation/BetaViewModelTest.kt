package com.lucaslima.musicsearch.feature.beta.presentation

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.lucaslima.musicsearch.core.testing.runViewModelTest
import kotlin.test.Test

class BetaViewModelTest {

    @Test
    fun comeca_com_a_mensagem_recebida_e_sem_toques() = runViewModelTest {
        val state = BetaViewModel(message = "hi").state.value

        assertThat(state.message).isEqualTo("hi")
        assertThat(state.tapCount).isEqualTo(0)
    }

    @Test
    fun cada_toque_soma_um() = runViewModelTest {
        val viewModel = BetaViewModel(message = "hi")

        repeat(3) { viewModel.onAction(BetaAction.OnTapClick) }

        assertThat(viewModel.state.value.tapCount).isEqualTo(3)
    }
}
