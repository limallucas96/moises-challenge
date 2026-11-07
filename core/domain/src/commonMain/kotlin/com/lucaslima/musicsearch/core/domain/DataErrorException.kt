package com.lucaslima.musicsearch.core.domain

/**
 * Leva um [DataError] por APIs que só aceitam Throwable (o `LoadState.Error` do Paging). Quem
 * recebe lê o [error] e trata como valor; a exceção em si nunca é lançada para fora do data.
 */
class DataErrorException(val error: DataError) : Exception(error.toString())
