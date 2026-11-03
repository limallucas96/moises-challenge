package cmp.skeleton.app.feature.alpha.domain

import cmp.skeleton.app.core.domain.DataError
import cmp.skeleton.app.core.domain.Result

/** O domain dita o contrato; quem implementa é o módulo data. */
interface GreetingRepository {
    suspend fun getGreeting(): Result<Greeting, DataError>
}
