package com.lucaslima.musicsearch.core.playback

import android.content.ComponentName
import android.content.Context
import android.net.Uri
import android.os.Handler
import android.os.Looper
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import java.util.concurrent.Executor
import kotlin.coroutines.cancellation.CancellationException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine

/**
 * [AudioPlayer] sobre um MediaController ligado ao [PlaybackService]. A conexão é assíncrona:
 * comandos esperam por ela. Se o serviço morrer, o próximo comando reconecta.
 *
 * [scope] precisa rodar na main thread: o MediaController só aceita chamadas dela.
 */
internal class Media3AudioPlayer(
    private val context: Context,
    private val scope: CoroutineScope,
) : AudioPlayer {

    private val _state = MutableStateFlow(PlaybackState())
    override val state: StateFlow<PlaybackState> = _state.asStateFlow()

    private val mainExecutor = Executor { Handler(Looper.getMainLooper()).post(it) }
    private var controller: MediaController? = null
    private var connecting: Deferred<MediaController>? = null
    private var progressJob: Job? = null

    private val listener = object : Player.Listener {
        override fun onEvents(
            player: Player,
            events: Player.Events,
        ) = publish(player)
    }

    init {
        // Conecta já: se algo tocava antes de o app abrir (notificação, carro), a tela mostra.
        scope.launch { runCatchingNonCancellation { controller() } }
    }

    override fun play(
        queue: List<PlayableItem>,
        startIndex: Int,
    ) = command {
        it.setMediaItems(queue.map(PlayableItem::toMediaItem), startIndex, 0L)
        it.prepare()
        it.play()
    }

    override fun togglePlayPause() = command {
        when {
            it.isPlaying -> it.pause()

            else -> {
                // Depois de um erro ou do fim da fila o player para; play sozinho não basta.
                if (it.playbackState == Player.STATE_IDLE) it.prepare()
                if (it.playbackState == Player.STATE_ENDED) it.seekToDefaultPosition()
                it.play()
            }
        }
    }

    override fun seekTo(position: Duration) = command { it.seekTo(position.inWholeMilliseconds) }

    override fun skipToNext() = command { it.seekToNextMediaItem() }

    /** Como em todo player: depois de alguns segundos, volta ao começo da música atual. */
    override fun skipToPrevious() = command { it.seekToPrevious() }

    override fun toggleRepeat() = command {
        it.repeatMode = when (it.repeatMode) {
            Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
            Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
            else -> Player.REPEAT_MODE_OFF
        }
    }

    private fun command(block: (MediaController) -> Unit) {
        scope.launch { runCatchingNonCancellation { block(controller()) } }
    }

    private suspend fun controller(): MediaController {
        controller?.let { return it }
        val pending = connecting ?: scope.async { connect() }.also { connecting = it }
        return pending.await()
    }

    private suspend fun connect(): MediaController = suspendCancellableCoroutine { continuation ->
        val token = SessionToken(context, ComponentName(context, PlaybackService::class.java))
        val future = MediaController.Builder(context, token)
            .setListener(
                object : MediaController.Listener {
                    override fun onDisconnected(controller: MediaController) {
                        this@Media3AudioPlayer.controller = null
                        connecting = null
                    }
                },
            )
            .buildAsync()
        future.addListener(
            {
                try {
                    val connected = future.get()
                    connected.addListener(listener)
                    controller = connected
                    publish(connected)
                    continuation.resume(connected)
                } catch (e: Exception) {
                    connecting = null
                    continuation.resumeWithException(e)
                }
            },
            mainExecutor,
        )
        continuation.invokeOnCancellation { future.cancel(true) }
    }

    private fun publish(player: Player) {
        _state.value = player.toPlaybackState()
        // A posição não gera evento: enquanto toca, é lida a cada PROGRESS_INTERVAL.
        if (player.isPlaying && progressJob?.isActive != true) {
            progressJob = scope.launch {
                while (isActive && player.isPlaying) {
                    _state.value = _state.value.copy(position = player.currentPosition.coerceAtLeast(0).milliseconds)
                    delay(PROGRESS_INTERVAL)
                }
            }
        }
    }

    private suspend fun runCatchingNonCancellation(block: suspend () -> Unit) {
        try {
            block()
        } catch (e: Exception) {
            // Sem serviço não há o que tocar; a tela fica no estado atual.
            if (e is CancellationException) throw e
        }
    }

    private companion object {
        val PROGRESS_INTERVAL = 250.milliseconds
    }
}

private fun PlayableItem.toMediaItem(): MediaItem {
    val uri = Uri.parse(audioUrl)
    return MediaItem.Builder()
        .setMediaId(id)
        .setUri(uri)
        .setRequestMetadata(MediaItem.RequestMetadata.Builder().setMediaUri(uri).build())
        .setMediaMetadata(
            MediaMetadata.Builder()
                .setTitle(title)
                .setArtist(artist)
                .setAlbumTitle(albumTitle)
                .setArtworkUri(artworkUrl?.let(Uri::parse))
                .setIsPlayable(true)
                .setIsBrowsable(false)
                .build(),
        )
        .build()
}

private fun MediaItem.toPlayableItem(): PlayableItem = PlayableItem(
    id = mediaId,
    title = mediaMetadata.title?.toString().orEmpty(),
    artist = mediaMetadata.artist?.toString().orEmpty(),
    albumTitle = mediaMetadata.albumTitle?.toString().orEmpty(),
    artworkUrl = mediaMetadata.artworkUri?.toString(),
    audioUrl = (localConfiguration?.uri ?: requestMetadata.mediaUri)?.toString().orEmpty(),
)

private fun Player.toPlaybackState(): PlaybackState = PlaybackState(
    current = currentMediaItem?.toPlayableItem(),
    // Carregando depois do play também conta: o botão já mostra pausa.
    isPlaying = playWhenReady && playbackState != Player.STATE_ENDED && playbackState != Player.STATE_IDLE,
    isBuffering = playbackState == Player.STATE_BUFFERING,
    position = currentPosition.coerceAtLeast(0).milliseconds,
    duration = duration.takeIf { it != C.TIME_UNSET && it > 0 }?.milliseconds ?: Duration.ZERO,
    hasNext = hasNextMediaItem(),
    hasPrevious = hasPreviousMediaItem(),
    repeatMode = when (repeatMode) {
        Player.REPEAT_MODE_ONE -> RepeatMode.ONE
        Player.REPEAT_MODE_ALL -> RepeatMode.ALL
        else -> RepeatMode.OFF
    },
    hasError = playerError != null,
)
