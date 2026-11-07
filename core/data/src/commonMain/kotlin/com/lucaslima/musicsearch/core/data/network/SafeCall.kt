package com.lucaslima.musicsearch.core.data.network

import com.lucaslima.musicsearch.core.domain.DataError
import com.lucaslima.musicsearch.core.domain.Result
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.serialization.ContentConvertException
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.io.IOException
import kotlinx.serialization.SerializationException

/**
 * Fronteira onde exceção de rede vira DataError.Network.
 * Acima do data source, ninguém faz try/catch de rede.
 */
suspend inline fun <reified T> safeCall(execute: () -> HttpResponse): Result<T, DataError.Network> {
    val response = try {
        execute()
    } catch (e: HttpRequestTimeoutException) {
        return Result.Error(DataError.Network.REQUEST_TIMEOUT)
    } catch (e: SerializationException) {
        return Result.Error(DataError.Network.SERIALIZATION)
    } catch (e: ContentConvertException) {
        return Result.Error(DataError.Network.SERIALIZATION)
    } catch (e: IOException) {
        // Falha de transporte (sem rede, DNS, conexão recusada): OkHttp e Darwin lançam IOException.
        return Result.Error(DataError.Network.NO_INTERNET)
    } catch (e: Exception) {
        if (e is CancellationException) throw e
        return Result.Error(DataError.Network.UNKNOWN)
    }
    return responseToResult(response)
}

suspend inline fun <reified T> responseToResult(response: HttpResponse): Result<T, DataError.Network> =
    when (response.status.value) {
        in 200..299 -> try {
            Result.Success(response.body<T>())
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Result.Error(
                when (e) {
                    // O ContentNegotiation embrulha a SerializationException em JsonConvertException.
                    is SerializationException, is ContentConvertException -> DataError.Network.SERIALIZATION

                    is IOException -> DataError.Network.NO_INTERNET

                    else -> DataError.Network.UNKNOWN
                },
            )
        }

        400 -> Result.Error(DataError.Network.BAD_REQUEST)

        401 -> Result.Error(DataError.Network.UNAUTHORIZED)

        403 -> Result.Error(DataError.Network.FORBIDDEN)

        404 -> Result.Error(DataError.Network.NOT_FOUND)

        408 -> Result.Error(DataError.Network.REQUEST_TIMEOUT)

        409 -> Result.Error(DataError.Network.CONFLICT)

        413 -> Result.Error(DataError.Network.PAYLOAD_TOO_LARGE)

        429 -> Result.Error(DataError.Network.TOO_MANY_REQUESTS)

        503 -> Result.Error(DataError.Network.SERVICE_UNAVAILABLE)

        in 500..599 -> Result.Error(DataError.Network.SERVER_ERROR)

        else -> Result.Error(DataError.Network.UNKNOWN)
    }

suspend inline fun <reified Response : Any> HttpClient.safeGet(route: String): Result<Response, DataError.Network> =
    safeCall { get(route) }

suspend inline fun <reified Request, reified Response : Any> HttpClient.safePost(
    route: String,
    body: Request,
): Result<Response, DataError.Network> = safeCall { post(route) { setBody(body) } }
