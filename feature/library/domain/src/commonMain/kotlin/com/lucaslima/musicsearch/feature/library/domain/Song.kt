package com.lucaslima.musicsearch.feature.library.domain

import kotlin.time.Duration

/** Uma faixa do catálogo. O id é o `trackId` da fonte, estável entre buscas. */
data class Song(
    val id: Long,
    val title: String,
    val artistName: String,
    val albumId: Long,
    val albumTitle: String,
    val artworkUrl: String?,
    val previewUrl: String?,
    val duration: Duration,
    val trackNumber: Int?,
)
