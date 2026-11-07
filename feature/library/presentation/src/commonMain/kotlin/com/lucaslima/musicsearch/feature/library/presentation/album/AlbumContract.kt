package com.lucaslima.musicsearch.feature.library.presentation.album

import com.lucaslima.musicsearch.core.domain.DataError
import com.lucaslima.musicsearch.feature.library.domain.Album
import com.lucaslima.musicsearch.feature.library.domain.Song

internal data class AlbumState(
    val album: Album? = null,
    val isLoading: Boolean = true,
    /** Falha ao buscar o álbum. Só aparece na tela se não houver nada salvo para mostrar. */
    val error: DataError? = null,
    val playingSongId: Long? = null,
)

internal sealed interface AlbumAction {
    data object OnBackClick : AlbumAction

    data object OnRetryClick : AlbumAction

    data class OnSongClick(val song: Song) : AlbumAction
}
