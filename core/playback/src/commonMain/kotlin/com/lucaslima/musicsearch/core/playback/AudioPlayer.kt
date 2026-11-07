package com.lucaslima.musicsearch.core.playback

import kotlin.time.Duration
import kotlinx.coroutines.flow.StateFlow

/**
 * Porta do player. A implementação de cada plataforma vive no source set dela (Media3 no
 * Android, AVPlayer no iOS) e é registrada no Koin pelo módulo da plataforma.
 */
interface AudioPlayer {
    val state: StateFlow<PlaybackState>

    /** Troca a fila e começa a tocar a partir de [startIndex]. */
    fun play(
        queue: List<PlayableItem>,
        startIndex: Int,
    )

    fun togglePlayPause()

    fun seekTo(position: Duration)

    fun skipToNext()

    fun skipToPrevious()

    fun toggleRepeat()
}

data class PlaybackState(
    val current: PlayableItem? = null,
    /** Tocando ou prestes a tocar (carregando depois do play). */
    val isPlaying: Boolean = false,
    val isBuffering: Boolean = false,
    val position: Duration = Duration.ZERO,
    val duration: Duration = Duration.ZERO,
    val hasNext: Boolean = false,
    val hasPrevious: Boolean = false,
    val repeatMode: RepeatMode = RepeatMode.OFF,
    /** A música atual não carregou (rede, formato). Some quando outra começa. */
    val hasError: Boolean = false,
)

enum class RepeatMode { OFF, ONE, ALL }
