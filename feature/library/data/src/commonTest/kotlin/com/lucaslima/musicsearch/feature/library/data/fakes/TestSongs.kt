package com.lucaslima.musicsearch.feature.library.data.fakes

import com.lucaslima.musicsearch.feature.library.domain.Album
import com.lucaslima.musicsearch.feature.library.domain.Song
import kotlin.time.Duration.Companion.seconds

internal fun song(
    id: Long,
    albumId: Long = 100,
    previewUrl: String? = "https://audio.example/$id.m4a",
    trackNumber: Int? = id.toInt(),
) = Song(
    id = id,
    title = "Song $id",
    artistName = "Artist",
    albumId = albumId,
    albumTitle = "Album $albumId",
    artworkUrl = "https://art.example/$id.jpg",
    previewUrl = previewUrl,
    duration = 30.seconds,
    trackNumber = trackNumber,
)

internal fun album(
    id: Long = 100,
    songs: List<Song> = listOf(song(1, id), song(2, id)),
) = Album(id = id, title = "Album $id", artistName = "Artist", artworkUrl = null, songs = songs)
