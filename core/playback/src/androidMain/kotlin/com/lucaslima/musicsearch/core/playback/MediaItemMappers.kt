package com.lucaslima.musicsearch.core.playback

import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata

/**
 * [PlayableItem] para MediaItem e de volta. O endereço do áudio vai também em
 * `requestMetadata.mediaUri`: o Media3 não repassa `localConfiguration` do controller para a
 * sessão, e é por ali que o [PlaybackService] o recupera.
 */
internal fun PlayableItem.toMediaItem(): MediaItem {
    val uri = Uri.parse(audioUrl)
    return MediaItem.Builder()
        .setMediaId(id)
        .setUri(uri)
        .setRequestMetadata(MediaItem.RequestMetadata.Builder().setMediaUri(uri).build())
        .setMediaMetadata(
            MediaMetadata.Builder()
                .setTitle(title)
                .setArtist(artist)
                .setAlbumTitle(albumTitle)
                .setArtworkUri(artworkUrl?.let(Uri::parse))
                .setIsPlayable(true)
                .setIsBrowsable(false)
                .setMediaType(MediaMetadata.MEDIA_TYPE_MUSIC)
                .build(),
        )
        .build()
}

internal fun MediaItem.toPlayableItem(): PlayableItem = PlayableItem(
    id = mediaId,
    title = mediaMetadata.title?.toString().orEmpty(),
    artist = mediaMetadata.artist?.toString().orEmpty(),
    albumTitle = mediaMetadata.albumTitle?.toString().orEmpty(),
    artworkUrl = mediaMetadata.artworkUri?.toString(),
    audioUrl = (localConfiguration?.uri ?: requestMetadata.mediaUri)?.toString().orEmpty(),
)
