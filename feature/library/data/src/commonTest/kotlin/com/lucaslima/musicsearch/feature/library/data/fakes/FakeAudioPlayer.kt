package com.lucaslima.musicsearch.feature.library.data.fakes

import com.lucaslima.musicsearch.core.playback.AudioPlayer
import com.lucaslima.musicsearch.core.playback.PlayableItem
import com.lucaslima.musicsearch.core.playback.PlaybackState
import com.lucaslima.musicsearch.core.playback.RepeatMode
import kotlin.time.Duration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

/** Player sem áudio: guarda a fila e anda por ela como o de verdade. */
internal class FakeAudioPlayer : AudioPlayer {
    private val _state = MutableStateFlow(PlaybackState())
    override val state: StateFlow<PlaybackState> = _state

    var queue: List<PlayableItem> = emptyList()
        private set
    private var index = 0
    var queueTitle: String? = null
        private set

    override fun play(
        queue: List<PlayableItem>,
        startIndex: Int,
        queueTitle: String?,
    ) {
        this.queueTitle = queueTitle
        this.queue = queue
        index = startIndex
        publish(isPlaying = true)
    }

    override fun togglePlayPause() = _state.update { it.copy(isPlaying = !it.isPlaying) }

    override fun seekTo(position: Duration) = _state.update { it.copy(position = position) }

    override fun skipToNext() {
        if (index < queue.lastIndex) index++
        publish(isPlaying = _state.value.isPlaying)
    }

    override fun skipToPrevious() {
        if (index > 0) index--
        publish(isPlaying = _state.value.isPlaying)
    }

    override fun toggleRepeat() = _state.update {
        it.copy(repeatMode = RepeatMode.entries[(it.repeatMode.ordinal + 1) % RepeatMode.entries.size])
    }

    /** Simula a música atual falhando ao carregar. */
    fun fail() = _state.update { it.copy(isPlaying = false, hasError = true) }

    private fun publish(isPlaying: Boolean) = _state.update {
        it.copy(
            current = queue.getOrNull(index),
            isPlaying = isPlaying,
            hasNext = index < queue.lastIndex,
            hasPrevious = index > 0,
        )
    }
}
