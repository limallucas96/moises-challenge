package cmp.skeleton.app.feature.alpha.presentation

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import cmp.skeleton.app.core.domain.DataError
import cmp.skeleton.app.core.domain.Result
import cmp.skeleton.app.core.testing.runViewModelTest
import cmp.skeleton.app.feature.alpha.domain.Greeting
import kotlin.test.Test

class AlphaViewModelTest {

    @Test
    fun carrega_a_saudacao_ao_abrir() = runViewModelTest {
        val repository = FakeGreetingRepository()

        val state = AlphaViewModel(repository).state.value

        assertThat(state.isLoading).isFalse()
        assertThat(state.greeting).isEqualTo("Hello")
        assertThat(state.error).isNull()
        assertThat(repository.calls).isEqualTo(1)
    }

    @Test
    fun falha_ao_carregar_mostra_erro_e_nao_mostra_saudacao() = runViewModelTest {
        val repository = FakeGreetingRepository().apply {
            result = Result.Error(DataError.Network.NO_INTERNET)
        }

        val state = AlphaViewModel(repository).state.value

        assertThat(state.isLoading).isFalse()
        assertThat(state.greeting).isNull()
        assertThat(state.error).isNotNull()
    }

    @Test
    fun atualizar_busca_de_novo_e_limpa_o_erro() = runViewModelTest {
        val repository = FakeGreetingRepository().apply {
            result = Result.Error(DataError.Network.NO_INTERNET)
        }
        val viewModel = AlphaViewModel(repository)

        repository.result = Result.Success(Greeting("Back online"))
        viewModel.onAction(AlphaAction.OnRefreshClick)

        assertThat(viewModel.state.value.greeting).isEqualTo("Back online")
        assertThat(viewModel.state.value.error).isNull()
        assertThat(repository.calls).isEqualTo(2)
    }
}
