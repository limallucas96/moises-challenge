package com.lucaslima.musicsearch.feature.library.presentation.fakes

import com.lucaslima.musicsearch.feature.library.domain.PlayerController
import com.lucaslima.musicsearch.feature.library.domain.PlayerState
import com.lucaslima.musicsearch.feature.library.domain.Song
import kotlin.time.Duration
import kotlinx.coroutines.flow.MutableStateFlow

/** Registra os comandos recebidos; o estado é controlado pelo teste. */
class FakePlayerController : PlayerController {
    override val state = MutableStateFlow(PlayerState())
    var lastQueue: List<Song>? = null
    var lastStartIndex: Int? = null
    var lastQueueTitle: String? = null
    val commands = mutableListOf<String>()
    val seeks = mutableListOf<Duration>()

    override fun play(
        queue: List<Song>,
        startIndex: Int,
        queueTitle: String?,
    ) {
        lastQueueTitle = queueTitle
        lastQueue = queue
        lastStartIndex = startIndex
    }

    override fun togglePlayPause() {
        commands += "togglePlayPause"
    }

    override fun seekTo(position: Duration) {
        seeks += position
    }

    override fun skipToNext() {
        commands += "next"
    }

    override fun skipToPrevious() {
        commands += "previous"
    }

    override fun toggleRepeat() {
        commands += "repeat"
    }
}
