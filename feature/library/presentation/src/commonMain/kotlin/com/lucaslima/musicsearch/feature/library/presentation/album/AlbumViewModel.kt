package com.lucaslima.musicsearch.feature.library.presentation.album

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lucaslima.musicsearch.core.domain.Result
import com.lucaslima.musicsearch.feature.library.domain.AlbumRepository
import com.lucaslima.musicsearch.feature.library.domain.PlayerController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Mostra o que está salvo na hora e atualiza pela rede em seguida (offline-first). */
class AlbumViewModel(
    private val albumId: Long,
    private val albumRepository: AlbumRepository,
    private val playerController: PlayerController,
) : ViewModel() {

    private val _state = MutableStateFlow(AlbumState())
    val state = _state.asStateFlow()

    init {
        albumRepository.observeAlbum(albumId)
            .onEach { album -> _state.update { it.copy(album = album) } }
            .launchIn(viewModelScope)
        playerController.state
            .map { it.song?.id }
            .distinctUntilChanged()
            .onEach { id -> _state.update { it.copy(playingSongId = id) } }
            .launchIn(viewModelScope)
        refresh()
    }

    fun onAction(action: AlbumAction) {
        when (action) {
            AlbumAction.OnRetryClick -> refresh()

            is AlbumAction.OnSongClick -> {
                val album = _state.value.album ?: return
                val index = album.songs.indexOfFirst { it.id == action.song.id }
                if (index >= 0) playerController.play(album.songs, index, queueTitle = album.title)
            }

            // A navegação é do Root.
            AlbumAction.OnBackClick -> Unit
        }
    }

    private fun refresh() {
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            val error = (albumRepository.refreshAlbum(albumId) as? Result.Error)?.error
            _state.update { it.copy(isLoading = false, error = error) }
        }
    }
}
