package com.lucaslima.musicsearch.feature.library.data.player

import com.lucaslima.musicsearch.core.playback.AudioPlayer
import com.lucaslima.musicsearch.core.playback.PlayableItem
import com.lucaslima.musicsearch.core.playback.PlaybackState
import com.lucaslima.musicsearch.core.playback.RepeatMode as PlaybackRepeatMode
import com.lucaslima.musicsearch.feature.library.data.local.LibraryLocalDataSource
import com.lucaslima.musicsearch.feature.library.domain.PlayerController
import com.lucaslima.musicsearch.feature.library.domain.PlayerState
import com.lucaslima.musicsearch.feature.library.domain.RepeatMode
import com.lucaslima.musicsearch.feature.library.domain.Song
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Clock
import kotlin.time.Duration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Adapter da porta [PlayerController] (domain) para o [AudioPlayer] (`:core:playback`). Traduz
 * [Song] para [PlayableItem] e de volta, e grava nos recentes cada música que começa a tocar.
 *
 * [scope] vive o tempo do app: o player continua tocando quando a tela fecha.
 */
internal class MediaPlayerController(
    private val audioPlayer: AudioPlayer,
    private val local: LibraryLocalDataSource,
    private val clock: Clock,
    scope: CoroutineScope,
) : PlayerController {

    /** A fila atual, pelo id do [PlayableItem], para voltar do item tocando para a [Song]. */
    private val queue = MutableStateFlow<Map<String, Song>>(emptyMap())

    override val state: StateFlow<PlayerState> = combine(audioPlayer.state, queue) { playback, songs ->
        playback.toPlayerState(songs)
    }.stateIn(scope, SharingStarted.Eagerly, PlayerState())

    init {
        scope.launch {
            state.map { it.song }
                .filterNotNull()
                .distinctUntilChangedBy { it.id }
                .collect { song ->
                    try {
                        local.markPlayed(song, clock.now())
                    } catch (e: Exception) {
                        // Falhar ao gravar o histórico não pode parar a música.
                        if (e is CancellationException) throw e
                    }
                }
        }
    }

    override fun play(
        queue: List<Song>,
        startIndex: Int,
    ) {
        val start = queue.getOrNull(startIndex) ?: return
        val playable = queue.filter { it.previewUrl != null }
        val index = playable.indexOfFirst { it.id == start.id }
        if (index < 0) return
        this.queue.value = playable.associateBy { it.id.toString() }
        audioPlayer.play(playable.map { it.toPlayableItem() }, index)
    }

    override fun togglePlayPause() = audioPlayer.togglePlayPause()

    override fun seekTo(position: Duration) = audioPlayer.seekTo(position)

    override fun skipToNext() = audioPlayer.skipToNext()

    override fun skipToPrevious() = audioPlayer.skipToPrevious()

    override fun toggleRepeat() = audioPlayer.toggleRepeat()
}

internal fun Song.toPlayableItem(): PlayableItem = PlayableItem(
    id = id.toString(),
    title = title,
    artist = artistName,
    albumTitle = albumTitle,
    artworkUrl = artworkUrl,
    audioUrl = requireNotNull(previewUrl) { "música sem prévia de áudio não entra na fila" },
)

private fun PlaybackState.toPlayerState(queue: Map<String, Song>): PlayerState = PlayerState(
    song = current?.let { queue[it.id] },
    isPlaying = isPlaying,
    isBuffering = isBuffering,
    position = position,
    duration = duration,
    hasNext = hasNext,
    hasPrevious = hasPrevious,
    repeatMode = when (repeatMode) {
        PlaybackRepeatMode.OFF -> RepeatMode.OFF
        PlaybackRepeatMode.ONE -> RepeatMode.ONE
        PlaybackRepeatMode.ALL -> RepeatMode.ALL
    },
)
