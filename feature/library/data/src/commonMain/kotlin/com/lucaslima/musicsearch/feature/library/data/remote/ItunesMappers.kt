package com.lucaslima.musicsearch.feature.library.data.remote

import com.lucaslima.musicsearch.feature.library.domain.Album
import com.lucaslima.musicsearch.feature.library.domain.Song
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

private const val WRAPPER_TRACK = "track"
private const val WRAPPER_COLLECTION = "collection"
private const val KIND_SONG = "song"

/** A API só documenta capas até 100x100, mas o CDN serve qualquer tamanho trocando o sufixo. */
private const val ARTWORK_SMALL_SUFFIX = "100x100bb"
private const val ARTWORK_LARGE_SUFFIX = "600x600bb"

internal val ItunesItemDto.isSong: Boolean
    get() = wrapperType == WRAPPER_TRACK && kind == KIND_SONG && trackId != null && collectionId != null

/** Null quando o item não é uma música (vídeo, álbum, item sem id). */
internal fun ItunesItemDto.toSongOrNull(): Song? {
    if (!isSong) return null
    return Song(
        id = requireNotNull(trackId),
        title = trackName.orEmpty(),
        artistName = artistName.orEmpty(),
        albumId = requireNotNull(collectionId),
        albumTitle = collectionName.orEmpty(),
        artworkUrl = artworkUrl100?.toLargeArtworkUrl(),
        previewUrl = previewUrl,
        duration = trackTimeMillis?.milliseconds ?: Duration.ZERO,
        trackNumber = trackNumber,
    )
}

/** Monta o álbum a partir do `/lookup`: o item `collection` dá o cabeçalho, os `track` dão as faixas. */
internal fun ItunesResponseDto.toAlbumOrNull(albumId: Long): Album? {
    val collection = results.firstOrNull { it.wrapperType == WRAPPER_COLLECTION && it.collectionId == albumId }
        ?: return null
    val songs = results
        .filter { it.collectionId == albumId }
        .sortedWith(compareBy({ it.discNumber ?: 1 }, { it.trackNumber ?: Int.MAX_VALUE }))
        .mapNotNull { it.toSongOrNull() }
    return Album(
        id = albumId,
        title = collection.collectionName.orEmpty(),
        artistName = collection.artistName.orEmpty(),
        artworkUrl = collection.artworkUrl100?.toLargeArtworkUrl(),
        songs = songs,
    )
}

internal fun String.toLargeArtworkUrl(): String = replace(ARTWORK_SMALL_SUFFIX, ARTWORK_LARGE_SUFFIX)
