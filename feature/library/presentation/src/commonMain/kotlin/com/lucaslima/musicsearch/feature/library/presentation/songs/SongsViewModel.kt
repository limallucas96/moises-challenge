package com.lucaslima.musicsearch.feature.library.presentation.songs

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

internal class SongsViewModel : ViewModel() {

    private val _state = MutableStateFlow(SongsState())
    val state = _state.asStateFlow()

    fun onAction(action: SongsAction) {
        when (action) {
            is SongsAction.OnQueryChange -> _state.update { it.copy(query = action.query) }
        }
    }
}
