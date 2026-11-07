package com.lucaslima.musicsearch.feature.library.data.remote

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isInstanceOf
import assertk.assertions.isTrue
import assertk.assertions.prop
import com.lucaslima.musicsearch.core.data.network.HttpClientFactory
import com.lucaslima.musicsearch.core.data.network.NetworkConfig
import com.lucaslima.musicsearch.core.data.network.NoAuthTokenProvider
import com.lucaslima.musicsearch.core.domain.DataError
import com.lucaslima.musicsearch.core.domain.Result
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpRequestData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlin.test.Test
import kotlinx.coroutines.test.runTest

class ItunesCatalogDataSourceTest {

    private val requests = mutableListOf<HttpRequestData>()

    private fun dataSource(
        handler: MockRequestHandleScope.(HttpRequestData) -> io.ktor.client.request.HttpResponseData,
    ) = ItunesCatalogDataSource(
        HttpClientFactory.create(
            engine = MockEngine { request ->
                requests += request
                handler(request)
            },
            config = NetworkConfig(baseUrl = "https://itunes.apple.com/", enableLogging = false),
            tokenProvider = NoAuthTokenProvider,
        ),
    )

    /** A API do iTunes responde JSON com content-type text/javascript. */
    private fun MockRequestHandleScope.respondItunes(
        body: String,
        status: HttpStatusCode = HttpStatusCode.OK,
    ) = respond(body, status, headersOf(HttpHeaders.ContentType, "text/javascript; charset=utf-8"))

    private fun songJson(
        id: Long,
        kind: String = "song",
    ) = """
        {"wrapperType":"track","kind":"$kind","trackId":$id,"collectionId":10,"trackName":"Song $id",
         "artistName":"Artist","collectionName":"Album","trackTimeMillis":1000,"trackNumber":$id,
         "previewUrl":"https://audio/$id.m4a",
         "artworkUrl100":"https://img/x/100x100bb.jpg"}
    """.trimIndent()

    private fun searchJson(ids: List<Long>) =
        """{"resultCount":${ids.size},"results":[${ids.joinToString(",") { songJson(it) }}]}"""

    @Test
    fun primeira_pagina_le_resposta_text_javascript() = runTest {
        val source = dataSource { respondItunes(searchJson(listOf(1, 2))) }

        val result = source.searchSongs("daft punk", page = 0, pageSize = 2)

        assertThat(result).isInstanceOf<Result.Success<SongPage>>()
            .prop(Result.Success<SongPage>::data)
            .transform { page -> page.songs.map { it.id } }
            .containsExactly(1L, 2L)
        val url = requests.single().url
        assertThat(url.encodedPath).isEqualTo("/search")
        assertThat(url.parameters["term"]).isEqualTo("daft punk")
        assertThat(url.parameters["entity"]).isEqualTo("song")
        assertThat(url.parameters["limit"]).isEqualTo("2")
    }

    @Test
    fun pagina_seguinte_pede_limit_acumulado_e_descarta_as_anteriores() = runTest {
        // A API ignora offset: a página 1 pede limit 4 e recebe as 4 primeiras.
        val source = dataSource { respondItunes(searchJson(listOf(1, 2, 3, 4))) }

        val page = (source.searchSongs("x", page = 1, pageSize = 2) as Result.Success).data

        assertThat(requests.single().url.parameters["limit"]).isEqualTo("4")
        assertThat(page.songs.map { it.id }).containsExactly(3L, 4L)
        assertThat(page.endReached).isFalse()
    }

    @Test
    fun menos_itens_que_o_pedido_marca_o_fim() = runTest {
        val source = dataSource { respondItunes(searchJson(listOf(1, 2, 3))) }

        val page = (source.searchSongs("x", page = 1, pageSize = 2) as Result.Success).data

        assertThat(page.songs.map { it.id }).containsExactly(3L)
        assertThat(page.endReached).isTrue()
    }

    @Test
    fun teto_de_200_marca_o_fim_e_nao_pede_alem_dele() = runTest {
        val source = dataSource { respondItunes(searchJson((1L..200L).toList())) }

        val last = (source.searchSongs("x", page = 7, pageSize = 25) as Result.Success).data
        val beyond = (source.searchSongs("x", page = 8, pageSize = 25) as Result.Success).data

        assertThat(last.songs.size).isEqualTo(25)
        assertThat(last.endReached).isTrue()
        assertThat(beyond.songs.size).isEqualTo(0)
        assertThat(beyond.endReached).isTrue()
        assertThat(requests.size).isEqualTo(1)
    }

    @Test
    fun itens_que_nao_sao_musica_ficam_de_fora() = runTest {
        val body = """{"resultCount":2,"results":[${songJson(1)},${songJson(2, kind = "music-video")}]}"""
        val source = dataSource { respondItunes(body) }

        val page = (source.searchSongs("x", page = 0, pageSize = 25) as Result.Success).data

        assertThat(page.songs.map { it.id }).containsExactly(1L)
    }

    @Test
    fun album_usa_o_item_collection_como_cabecalho_e_ordena_as_faixas() = runTest {
        val body = """
            {"resultCount":3,"results":[
              {"wrapperType":"collection","collectionId":10,"collectionName":"Discovery","artistName":"Daft Punk",
               "artworkUrl100":"https://img/a/100x100bb.jpg"},
              ${songJson(2)},
              ${songJson(1)}
            ]}
        """.trimIndent()
        val source = dataSource { respondItunes(body) }

        val album = (source.album(10) as Result.Success).data

        assertThat(requests.single().url.encodedPath).isEqualTo("/lookup")
        assertThat(album.title).isEqualTo("Discovery")
        assertThat(album.artworkUrl).isEqualTo("https://img/a/600x600bb.jpg")
        assertThat(album.songs.map { it.id }).containsExactly(1L, 2L)
    }

    @Test
    fun album_sem_item_collection_vira_not_found() = runTest {
        val source = dataSource { respondItunes("""{"resultCount":0,"results":[]}""") }

        assertThat(source.album(10)).isEqualTo(Result.Error(DataError.Network.NOT_FOUND))
    }

    @Test
    fun erro_http_vira_data_error() = runTest {
        val source = dataSource { respondItunes("", HttpStatusCode.ServiceUnavailable) }

        assertThat(source.searchSongs("x", 0, 25)).isEqualTo(Result.Error(DataError.Network.SERVICE_UNAVAILABLE))
    }
}
