package com.lucaslima.musicsearch.feature.alpha.domain

import com.lucaslima.musicsearch.core.domain.DataError
import com.lucaslima.musicsearch.core.domain.Result

/** O domain dita o contrato; quem implementa é o módulo data. */
interface GreetingRepository {
    suspend fun getGreeting(): Result<Greeting, DataError>
}
