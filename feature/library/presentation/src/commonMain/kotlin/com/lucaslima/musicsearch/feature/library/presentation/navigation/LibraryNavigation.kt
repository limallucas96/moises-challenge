package com.lucaslima.musicsearch.feature.library.presentation.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.lucaslima.musicsearch.core.presentation.navigation.NavKeyRegistration
import com.lucaslima.musicsearch.core.presentation.navigation.navKey
import com.lucaslima.musicsearch.feature.library.presentation.album.AlbumRoot
import com.lucaslima.musicsearch.feature.library.presentation.nowplaying.NowPlayingRoot
import com.lucaslima.musicsearch.feature.library.presentation.songs.SongsRoot
import kotlinx.serialization.Serializable

/** Key de entrada da feature: a lista de músicas (Home). */
@Serializable
data object SongsKey : NavKey

/** O player. Sem argumento: mostra o que o player estiver tocando. */
@Serializable
data object NowPlayingKey : NavKey

@Serializable
data class AlbumKey(val albumId: Long) : NavKey

/**
 * Telas da feature. Quem é dono da pilha (o :shared) decide o que cada callback faz, como no
 * resto do app.
 */
fun EntryProviderScope<NavKey>.libraryEntries(
    onOpenNowPlaying: () -> Unit,
    onOpenAlbum: (albumId: Long) -> Unit,
    onBack: () -> Unit,
) {
    entry<SongsKey> {
        SongsRoot(onOpenPlayer = onOpenNowPlaying, onOpenAlbum = onOpenAlbum)
    }
    entry<NowPlayingKey> {
        NowPlayingRoot(onBackClick = onBack, onOpenAlbum = onOpenAlbum)
    }
    entry<AlbumKey> { key ->
        AlbumRoot(albumId = key.albumId, onBackClick = onBack, onOpenPlayer = onOpenNowPlaying)
    }
}

/** TODAS as NavKey da feature. Key nova = uma linha nova aqui. */
val libraryNavKeys: List<NavKeyRegistration<*>> = listOf(
    navKey(SongsKey),
    navKey(NowPlayingKey),
    navKey(AlbumKey(albumId = 1)),
)
