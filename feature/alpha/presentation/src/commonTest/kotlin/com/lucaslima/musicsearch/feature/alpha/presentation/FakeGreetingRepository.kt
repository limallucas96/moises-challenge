package com.lucaslima.musicsearch.feature.alpha.presentation

import com.lucaslima.musicsearch.core.domain.DataError
import com.lucaslima.musicsearch.core.domain.Result
import com.lucaslima.musicsearch.feature.alpha.domain.Greeting
import com.lucaslima.musicsearch.feature.alpha.domain.GreetingRepository

internal class FakeGreetingRepository : GreetingRepository {

    var result: Result<Greeting, DataError> = Result.Success(Greeting("Hello"))
    var calls = 0
        private set

    override suspend fun getGreeting(): Result<Greeting, DataError> {
        calls += 1
        return result
    }
}
