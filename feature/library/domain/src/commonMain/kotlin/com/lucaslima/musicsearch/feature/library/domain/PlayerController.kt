package com.lucaslima.musicsearch.feature.library.domain

import kotlin.time.Duration
import kotlinx.coroutines.flow.StateFlow

/**
 * O player do ponto de vista da feature: toca [Song], não sabe que existe Media3 ou AVPlayer.
 * Tocar uma música também a registra nos recentes.
 */
interface PlayerController {
    val state: StateFlow<PlayerState>

    /** Troca a fila e toca a partir de [startIndex]. Músicas sem prévia de áudio ficam de fora. */
    fun play(
        queue: List<Song>,
        startIndex: Int,
    )

    fun togglePlayPause()

    fun seekTo(position: Duration)

    fun skipToNext()

    fun skipToPrevious()

    fun toggleRepeat()
}

data class PlayerState(
    val song: Song? = null,
    val isPlaying: Boolean = false,
    val isBuffering: Boolean = false,
    val position: Duration = Duration.ZERO,
    val duration: Duration = Duration.ZERO,
    val hasNext: Boolean = false,
    val hasPrevious: Boolean = false,
    val repeatMode: RepeatMode = RepeatMode.OFF,
    /** A música atual não carregou; a tela avisa e deixa tentar de novo pelo play. */
    val hasError: Boolean = false,
)

enum class RepeatMode { OFF, ONE, ALL }
