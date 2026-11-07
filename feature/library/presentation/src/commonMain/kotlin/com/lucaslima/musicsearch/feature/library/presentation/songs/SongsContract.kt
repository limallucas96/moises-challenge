package com.lucaslima.musicsearch.feature.library.presentation.songs

import com.lucaslima.musicsearch.feature.library.domain.Song

/**
 * Estado da Home. Os resultados paginados da busca não ficam aqui: `PagingData` é um fluxo
 * próprio ([SongsViewModel.searchResults]) e não deve ir dentro de um data class de estado.
 */
data class SongsState(
    val query: String = "",
    val recentlyPlayed: List<Song> = emptyList(),
    /** false até a primeira leitura dos recentes: evita piscar o estado vazio ao abrir. */
    val isRecentlyPlayedLoaded: Boolean = false,
    /** Id da música tocando agora, para destacar a linha. */
    val playingSongId: Long? = null,
    /** Música com o bottom sheet de opções aberto. */
    val optionsSong: Song? = null,
) {
    val isSearching: Boolean get() = query.isNotBlank()
}

sealed interface SongsAction {
    data class OnQueryChange(val query: String) : SongsAction

    data object OnClearQueryClick : SongsAction

    /** [queue] é a lista visível no momento do toque: vira a fila do player. */
    data class OnSongClick(
        val song: Song,
        val queue: List<Song>,
    ) : SongsAction

    data class OnMoreClick(val song: Song) : SongsAction

    data object OnOptionsDismiss : SongsAction

    data class OnViewAlbumClick(val song: Song) : SongsAction
}
