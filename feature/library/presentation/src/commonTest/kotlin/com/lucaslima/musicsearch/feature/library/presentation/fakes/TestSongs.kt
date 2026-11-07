package com.lucaslima.musicsearch.feature.library.presentation.fakes

import com.lucaslima.musicsearch.feature.library.domain.Song
import kotlin.time.Duration.Companion.seconds

fun song(
    id: Long,
    title: String = "Song $id",
    previewUrl: String? = "https://example.com/$id.m4a",
    albumId: Long = 100,
) = Song(
    id = id,
    title = title,
    artistName = "Artist $id",
    albumId = albumId,
    albumTitle = "Album $albumId",
    artworkUrl = null,
    previewUrl = previewUrl,
    duration = 30.seconds,
    trackNumber = id.toInt(),
)
