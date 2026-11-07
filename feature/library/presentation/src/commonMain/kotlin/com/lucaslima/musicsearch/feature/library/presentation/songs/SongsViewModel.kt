package com.lucaslima.musicsearch.feature.library.presentation.songs

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.lucaslima.musicsearch.feature.library.domain.PlayerController
import com.lucaslima.musicsearch.feature.library.domain.Song
import com.lucaslima.musicsearch.feature.library.domain.SongRepository
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update

class SongsViewModel(
    private val songRepository: SongRepository,
    private val playerController: PlayerController,
    private val savedStateHandle: SavedStateHandle,
    private val searchDebounce: Duration = SEARCH_DEBOUNCE,
) : ViewModel() {

    private val _state = MutableStateFlow(SongsState(query = savedStateHandle[KEY_QUERY] ?: ""))
    val state = _state.asStateFlow()

    /**
     * Resultados da busca. Espera o usuário parar de digitar; apagar o termo é imediato. O
     * `cachedIn` mantém as páginas na rotação e ao voltar de outra tela.
     */
    val searchResults: Flow<PagingData<Song>> = _state
        .map { it.query.trim() }
        .distinctUntilChanged()
        .debounce { term -> if (term.isEmpty()) Duration.ZERO else searchDebounce }
        .flatMapLatest { term -> songRepository.searchSongs(term) }
        .cachedIn(viewModelScope)

    init {
        songRepository.recentlyPlayed()
            .onEach { songs -> _state.update { it.copy(recentlyPlayed = songs, isRecentlyPlayedLoaded = true) } }
            .launchIn(viewModelScope)
        playerController.state
            .map { it.song?.id }
            .distinctUntilChanged()
            .onEach { id -> _state.update { it.copy(playingSongId = id) } }
            .launchIn(viewModelScope)
    }

    fun onAction(action: SongsAction) {
        when (action) {
            is SongsAction.OnQueryChange -> updateQuery(action.query)

            SongsAction.OnClearQueryClick -> updateQuery("")

            is SongsAction.OnSongClick -> {
                val index = action.queue.indexOfFirst { it.id == action.song.id }
                if (index >= 0) {
                    playerController.play(action.queue, index)
                } else {
                    playerController.play(listOf(action.song), 0)
                }
            }

            is SongsAction.OnMoreClick -> _state.update { it.copy(optionsSong = action.song) }

            // A navegação para o álbum é do Root; aqui só fecha o sheet.
            SongsAction.OnOptionsDismiss,
            is SongsAction.OnViewAlbumClick,
            -> _state.update { it.copy(optionsSong = null) }
        }
    }

    private fun updateQuery(query: String) {
        savedStateHandle[KEY_QUERY] = query
        _state.update { it.copy(query = query) }
    }

    companion object {
        val SEARCH_DEBOUNCE = 300.milliseconds
        private const val KEY_QUERY = "query"
    }
}
