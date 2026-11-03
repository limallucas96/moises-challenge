package cmp.skeleton.app.core.data.network

import assertk.assertThat
import assertk.assertions.isEqualTo
import cmp.skeleton.app.core.domain.DataError
import cmp.skeleton.app.core.domain.Result
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlin.test.Test
import kotlinx.coroutines.test.runTest
import kotlinx.io.IOException
import kotlinx.serialization.Serializable

class SafeCallTest {

    @Serializable
    private data class Payload(val value: String)

    private fun client(engine: MockEngine) = HttpClient(engine) {
        install(ContentNegotiation) { json(appJson) }
    }

    private fun respondWith(
        status: HttpStatusCode,
        body: String = "",
    ) = MockEngine {
        respond(body, status, headersOf(HttpHeaders.ContentType, "application/json"))
    }

    @Test
    fun resposta_2xx_vira_sucesso() = runTest {
        val result = client(respondWith(HttpStatusCode.OK, """{"value":"ok"}""")).safeGet<Payload>("x")
        assertThat(result).isEqualTo(Result.Success(Payload("ok")))
    }

    @Test
    fun status_http_viram_erros_tipados() = runTest {
        val cases = mapOf(
            HttpStatusCode.BadRequest to DataError.Network.BAD_REQUEST,
            HttpStatusCode.Unauthorized to DataError.Network.UNAUTHORIZED,
            HttpStatusCode.NotFound to DataError.Network.NOT_FOUND,
            HttpStatusCode.TooManyRequests to DataError.Network.TOO_MANY_REQUESTS,
            HttpStatusCode.ServiceUnavailable to DataError.Network.SERVICE_UNAVAILABLE,
            HttpStatusCode.InternalServerError to DataError.Network.SERVER_ERROR,
        )
        cases.forEach { (status, expected) ->
            val result = client(respondWith(status)).safeGet<Payload>("x")
            assertThat(result).isEqualTo(Result.Error(expected))
        }
    }

    @Test
    fun corpo_invalido_vira_serialization() = runTest {
        val result = client(respondWith(HttpStatusCode.OK, """{"outro":1}""")).safeGet<Payload>("x")
        assertThat(result).isEqualTo(Result.Error(DataError.Network.SERIALIZATION))
    }

    @Test
    fun falha_de_transporte_vira_no_internet() = runTest {
        val result = client(MockEngine { throw IOException("offline") }).safeGet<Payload>("x")
        assertThat(result).isEqualTo(Result.Error(DataError.Network.NO_INTERNET))
    }

    @Test
    fun excecao_inesperada_vira_unknown() = runTest {
        val result = client(MockEngine { throw IllegalStateException("bug") }).safeGet<Payload>("x")
        assertThat(result).isEqualTo(Result.Error(DataError.Network.UNKNOWN))
    }
}
