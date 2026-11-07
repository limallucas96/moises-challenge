package com.lucaslima.musicsearch.feature.library.data.local

import com.lucaslima.musicsearch.feature.library.domain.Album
import com.lucaslima.musicsearch.feature.library.domain.Song
import kotlin.time.Duration.Companion.milliseconds

internal fun SongEntity.toSong(): Song = Song(
    id = id,
    title = title,
    artistName = artistName,
    albumId = albumId,
    albumTitle = albumTitle,
    artworkUrl = artworkUrl,
    previewUrl = previewUrl,
    duration = durationMillis.milliseconds,
    trackNumber = trackNumber,
)

internal fun Song.toEntity(): SongEntity = SongEntity(
    id = id,
    title = title,
    artistName = artistName,
    albumId = albumId,
    albumTitle = albumTitle,
    artworkUrl = artworkUrl,
    previewUrl = previewUrl,
    durationMillis = duration.inWholeMilliseconds,
    trackNumber = trackNumber,
)

internal fun AlbumEntity.toAlbum(songs: List<SongEntity>): Album = Album(
    id = id,
    title = title,
    artistName = artistName,
    artworkUrl = artworkUrl,
    // O @Relation não garante ordem: a do disco vem do número da faixa.
    songs = songs.sortedBy { it.trackNumber ?: Int.MAX_VALUE }.map { it.toSong() },
)

internal fun Album.toEntity(updatedAtMillis: Long): AlbumEntity = AlbumEntity(
    id = id,
    title = title,
    artistName = artistName,
    artworkUrl = artworkUrl,
    updatedAtMillis = updatedAtMillis,
)
