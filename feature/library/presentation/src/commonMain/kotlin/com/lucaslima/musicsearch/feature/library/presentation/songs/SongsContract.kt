package com.lucaslima.musicsearch.feature.library.presentation.songs

internal data class SongsState(val query: String = "")

internal sealed interface SongsAction {
    data class OnQueryChange(val query: String) : SongsAction
}
