package com.lucaslima.musicsearch.feature.alpha.data

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import com.lucaslima.musicsearch.core.domain.Result
import kotlin.test.Test
import kotlinx.coroutines.test.runTest

class InMemoryGreetingRepositoryTest {

    @Test
    fun devolve_a_saudacao_com_sucesso() = runTest {
        val result = InMemoryGreetingRepository().getGreeting()

        assertThat(result).isInstanceOf(Result.Success::class)
    }

    @Test
    fun cada_chamada_devolve_uma_saudacao_diferente() = runTest {
        val repository = InMemoryGreetingRepository()

        val first = (repository.getGreeting() as Result.Success).data.text
        val second = (repository.getGreeting() as Result.Success).data.text

        assertThat(first == second).isEqualTo(false)
    }
}
