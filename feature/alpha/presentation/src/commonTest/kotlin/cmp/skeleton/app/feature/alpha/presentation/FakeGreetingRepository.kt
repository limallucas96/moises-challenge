package cmp.skeleton.app.feature.alpha.presentation

import cmp.skeleton.app.core.domain.DataError
import cmp.skeleton.app.core.domain.Result
import cmp.skeleton.app.feature.alpha.domain.Greeting
import cmp.skeleton.app.feature.alpha.domain.GreetingRepository

internal class FakeGreetingRepository : GreetingRepository {

    var result: Result<Greeting, DataError> = Result.Success(Greeting("Hello"))
    var calls = 0
        private set

    override suspend fun getGreeting(): Result<Greeting, DataError> {
        calls += 1
        return result
    }
}
