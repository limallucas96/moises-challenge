package com.lucaslima.musicsearch.feature.library.presentation.fakes

import com.lucaslima.musicsearch.feature.library.domain.PlayerController
import com.lucaslima.musicsearch.feature.library.domain.PlayerState
import com.lucaslima.musicsearch.feature.library.domain.Song
import kotlin.time.Duration
import kotlinx.coroutines.flow.MutableStateFlow

/** Registra o último play; o estado é controlado pelo teste. */
class FakePlayerController : PlayerController {
    override val state = MutableStateFlow(PlayerState())
    var lastQueue: List<Song>? = null
    var lastStartIndex: Int? = null

    override fun play(
        queue: List<Song>,
        startIndex: Int,
    ) {
        lastQueue = queue
        lastStartIndex = startIndex
    }

    override fun togglePlayPause() = Unit

    override fun seekTo(position: Duration) = Unit

    override fun skipToNext() = Unit

    override fun skipToPrevious() = Unit

    override fun toggleRepeat() = Unit
}
