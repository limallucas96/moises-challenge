package com.lucaslima.musicsearch.feature.alpha.data

import com.lucaslima.musicsearch.core.domain.DataError
import com.lucaslima.musicsearch.core.domain.Result
import com.lucaslima.musicsearch.feature.alpha.domain.Greeting
import com.lucaslima.musicsearch.feature.alpha.domain.GreetingRepository

/**
 * Implementação de exemplo, sem rede nem disco. Cada chamada devolve uma saudação diferente, para
 * a tela mostrar que o dado recarregou. Num projeto real, este é o lugar do data source remoto.
 */
internal class InMemoryGreetingRepository : GreetingRepository {

    private var calls = 0

    override suspend fun getGreeting(): Result<Greeting, DataError> {
        calls += 1
        return Result.Success(Greeting(text = "Hello from the data layer (#$calls)"))
    }
}
