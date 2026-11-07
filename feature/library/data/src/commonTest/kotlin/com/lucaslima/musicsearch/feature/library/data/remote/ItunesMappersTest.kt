package com.lucaslima.musicsearch.feature.library.data.remote

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import assertk.assertions.prop
import com.lucaslima.musicsearch.feature.library.domain.Song
import kotlin.test.Test
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

class ItunesMappersTest {

    private val track = ItunesItemDto(
        wrapperType = "track",
        kind = "song",
        trackId = 1,
        collectionId = 10,
        trackName = "Get Lucky",
        artistName = "Daft Punk",
        collectionName = "Random Access Memories",
        artworkUrl100 = "https://img/x/100x100bb.jpg",
        previewUrl = "https://audio/1.m4a",
        trackTimeMillis = 369_629,
        trackNumber = 8,
    )

    @Test
    fun faixa_vira_song_com_capa_grande_e_duracao() {
        val song = track.toSongOrNull()

        assertThat(song).isNotNull().prop(Song::artworkUrl).isEqualTo("https://img/x/600x600bb.jpg")
        assertThat(song).isNotNull().prop(Song::duration).isEqualTo(369_629.milliseconds)
        assertThat(song).isNotNull().prop(Song::albumId).isEqualTo(10L)
    }

    @Test
    fun faixa_sem_duracao_fica_com_zero() {
        assertThat(track.copy(trackTimeMillis = null).toSongOrNull())
            .isNotNull()
            .prop(Song::duration)
            .isEqualTo(Duration.ZERO)
    }

    @Test
    fun item_que_nao_e_musica_vira_null() {
        assertThat(track.copy(kind = "feature-movie").toSongOrNull()).isNull()
        assertThat(track.copy(wrapperType = "collection").toSongOrNull()).isNull()
        assertThat(track.copy(trackId = null).toSongOrNull()).isNull()
    }
}
