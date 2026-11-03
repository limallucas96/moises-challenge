package cmp.skeleton.app.feature.alpha.data

import cmp.skeleton.app.core.domain.DataError
import cmp.skeleton.app.core.domain.Result
import cmp.skeleton.app.feature.alpha.domain.Greeting
import cmp.skeleton.app.feature.alpha.domain.GreetingRepository

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
