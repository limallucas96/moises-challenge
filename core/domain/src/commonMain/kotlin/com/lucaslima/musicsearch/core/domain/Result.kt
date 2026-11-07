package com.lucaslima.musicsearch.core.domain

/**
 * Retorno de toda operação que pode falhar de forma esperada.
 * Falha esperada é valor, não exceção.
 */
sealed interface Result<out D, out E : Error> {
    data class Success<out D>(val data: D) : Result<D, Nothing>
    data class Error<out E : com.lucaslima.musicsearch.core.domain.Error>(val error: E) : Result<Nothing, E>
}

typealias EmptyResult<E> = Result<Unit, E>

inline fun <T, E : Error, R> Result<T, E>.map(map: (T) -> R): Result<R, E> = when (this) {
    is Result.Error -> Result.Error(error)
    is Result.Success -> Result.Success(map(data))
}

inline fun <T, E : Error, F : Error> Result<T, E>.mapError(map: (E) -> F): Result<T, F> = when (this) {
    is Result.Error -> Result.Error(map(error))
    is Result.Success -> this
}

inline fun <T, E : Error> Result<T, E>.onSuccess(action: (T) -> Unit): Result<T, E> {
    if (this is Result.Success) action(data)
    return this
}

inline fun <T, E : Error> Result<T, E>.onFailure(action: (E) -> Unit): Result<T, E> {
    if (this is Result.Error) action(error)
    return this
}

fun <T, E : Error> Result<T, E>.asEmptyResult(): EmptyResult<E> = map { }
