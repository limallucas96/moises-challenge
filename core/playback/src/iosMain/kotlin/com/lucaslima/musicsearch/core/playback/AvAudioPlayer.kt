package com.lucaslima.musicsearch.core.playback

import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryPlayback
import platform.AVFAudio.setActive
import platform.AVFoundation.AVPlayer
import platform.AVFoundation.AVPlayerItem
import platform.AVFoundation.AVPlayerItemDidPlayToEndTimeNotification
import platform.AVFoundation.AVPlayerItemStatusFailed
import platform.AVFoundation.AVPlayerItemStatusUnknown
import platform.AVFoundation.AVPlayerTimeControlStatusPaused
import platform.AVFoundation.AVPlayerTimeControlStatusWaitingToPlayAtSpecifiedRate
import platform.AVFoundation.currentItem
import platform.AVFoundation.currentTime
import platform.AVFoundation.duration
import platform.AVFoundation.pause
import platform.AVFoundation.play
import platform.AVFoundation.replaceCurrentItemWithPlayerItem
import platform.AVFoundation.seekToTime
import platform.AVFoundation.timeControlStatus
import platform.CoreMedia.CMTimeGetSeconds
import platform.CoreMedia.CMTimeMakeWithSeconds
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSOperationQueue
import platform.Foundation.NSURL
import platform.MediaPlayer.MPChangePlaybackPositionCommandEvent
import platform.MediaPlayer.MPMediaItemPropertyAlbumTitle
import platform.MediaPlayer.MPMediaItemPropertyArtist
import platform.MediaPlayer.MPMediaItemPropertyPlaybackDuration
import platform.MediaPlayer.MPMediaItemPropertyTitle
import platform.MediaPlayer.MPNowPlayingInfoCenter
import platform.MediaPlayer.MPNowPlayingInfoPropertyElapsedPlaybackTime
import platform.MediaPlayer.MPNowPlayingInfoPropertyPlaybackRate
import platform.MediaPlayer.MPRemoteCommandCenter
import platform.MediaPlayer.MPRemoteCommandHandlerStatus
import platform.MediaPlayer.MPRemoteCommandHandlerStatusSuccess

/**
 * [AudioPlayer] sobre o AVPlayer. O AVPlayer toca um item por vez, então a fila, o repetir e a
 * passagem para a próxima ficam aqui. O estado é lido a cada [PROGRESS_INTERVAL] em vez de KVO:
 * mais simples e barato o bastante para uma tela de player.
 *
 * Também publica a música no Now Playing (tela de bloqueio, Central de Controle) e atende os
 * comandos de lá.
 *
 * [scope] precisa rodar na main thread.
 */
@OptIn(ExperimentalForeignApi::class)
internal class AvAudioPlayer(private val scope: CoroutineScope) : AudioPlayer {

    private val player = AVPlayer()
    private var queue: List<PlayableItem> = emptyList()
    private var index = -1
    private var repeatMode = RepeatMode.OFF

    /**
     * O usuário pediu para tocar. O AVPlayer diz "pausado" enquanto o item novo ainda carrega;
     * com isto a tela já mostra pausa e o carregando, como no Android.
     */
    private var wantsToPlay = false

    private val _state = MutableStateFlow(PlaybackState())
    override val state: StateFlow<PlaybackState> = _state.asStateFlow()

    /** O que já foi enviado ao Now Playing; só reenvia quando muda. */
    private var nowPlayingKey: Any? = null

    init {
        AVAudioSession.sharedInstance().run {
            // Categoria de reprodução: toca com o modo silencioso ligado e em segundo plano.
            setCategory(AVAudioSessionCategoryPlayback, error = null)
            setActive(true, error = null)
        }
        NSNotificationCenter.defaultCenter.addObserverForName(
            name = AVPlayerItemDidPlayToEndTimeNotification,
            `object` = null,
            queue = NSOperationQueue.mainQueue,
        ) { notification ->
            if (notification?.`object` == player.currentItem) onItemEnded()
        }
        registerRemoteCommands()
        scope.launch {
            while (isActive) {
                publish()
                delay(PROGRESS_INTERVAL)
            }
        }
    }

    override fun play(
        queue: List<PlayableItem>,
        startIndex: Int,
        // O Now Playing do iOS não mostra nome de fila.
        queueTitle: String?,
    ) {
        if (startIndex !in queue.indices) return
        this.queue = queue
        load(startIndex)
        start()
        publish()
    }

    override fun togglePlayPause() {
        if (player.currentItem == null && index in queue.indices) load(index)
        when {
            _state.value.isPlaying -> stop()

            else -> {
                // Item com erro: carrega de novo. Música no fim: volta ao começo.
                if (player.currentItem?.status == AVPlayerItemStatusFailed) load(index)
                if (isAtEnd()) seek(Duration.ZERO)
                start()
            }
        }
        publish()
    }

    override fun seekTo(position: Duration) {
        seek(position)
        publish()
    }

    override fun skipToNext() {
        when {
            index < queue.lastIndex -> load(index + 1)
            repeatMode == RepeatMode.ALL && queue.isNotEmpty() -> load(0)
            else -> return
        }
        start()
        publish()
    }

    /** Como em todo player: depois de alguns segundos, volta ao começo da música atual. */
    override fun skipToPrevious() {
        if (index > 0 && currentPosition() < RESTART_THRESHOLD) {
            load(index - 1)
            start()
        } else {
            seek(Duration.ZERO)
        }
        publish()
    }

    override fun toggleRepeat() {
        repeatMode = when (repeatMode) {
            RepeatMode.OFF -> RepeatMode.ALL
            RepeatMode.ALL -> RepeatMode.ONE
            RepeatMode.ONE -> RepeatMode.OFF
        }
        publish()
    }

    private fun onItemEnded() {
        when {
            repeatMode == RepeatMode.ONE -> {
                seek(Duration.ZERO)
                start()
            }

            index < queue.lastIndex -> {
                load(index + 1)
                start()
            }

            repeatMode == RepeatMode.ALL -> {
                load(0)
                start()
            }

            else -> stop()
        }
        publish()
    }

    private fun start() {
        wantsToPlay = true
        player.play()
    }

    private fun stop() {
        wantsToPlay = false
        player.pause()
    }

    private fun load(newIndex: Int) {
        index = newIndex
        val url = NSURL.URLWithString(queue[newIndex].audioUrl)
        player.replaceCurrentItemWithPlayerItem(url?.let { AVPlayerItem(uRL = it) })
    }

    private fun seek(position: Duration) {
        player.seekToTime(CMTimeMakeWithSeconds(position.inWholeMilliseconds / 1000.0, preferredTimescale = 600))
    }

    private fun currentPosition(): Duration = player.currentTime().toDuration()

    private fun currentDuration(): Duration = player.currentItem?.duration?.toDuration() ?: Duration.ZERO

    private fun isAtEnd(): Boolean {
        val duration = currentDuration()
        return duration > Duration.ZERO && currentPosition() >= duration - END_TOLERANCE
    }

    private fun publish() {
        val status = player.timeControlStatus
        val item = player.currentItem
        val isLoading = wantsToPlay && item?.status == AVPlayerItemStatusUnknown
        // Pausado com o item pronto e sem ter pedido pausa: o sistema pausou (ligação, outro app).
        val isPlaying = status != AVPlayerTimeControlStatusPaused || isLoading
        val state = PlaybackState(
            current = queue.getOrNull(index),
            isPlaying = isPlaying,
            isBuffering = isLoading || status == AVPlayerTimeControlStatusWaitingToPlayAtSpecifiedRate,
            position = currentPosition(),
            duration = currentDuration(),
            hasNext = index < queue.lastIndex,
            hasPrevious = index > 0,
            repeatMode = repeatMode,
            hasError = player.currentItem?.status == AVPlayerItemStatusFailed,
        )
        _state.value = state
        updateNowPlaying(state)
    }

    private fun updateNowPlaying(state: PlaybackState) {
        val item = state.current
        val key = Triple(item?.id, state.isPlaying, state.duration)
        if (key == nowPlayingKey) return
        nowPlayingKey = key
        MPNowPlayingInfoCenter.defaultCenter().nowPlayingInfo = item?.let {
            mapOf<Any?, Any?>(
                MPMediaItemPropertyTitle to it.title,
                MPMediaItemPropertyArtist to it.artist,
                MPMediaItemPropertyAlbumTitle to it.albumTitle,
                MPMediaItemPropertyPlaybackDuration to state.duration.inWholeMilliseconds / 1000.0,
                MPNowPlayingInfoPropertyElapsedPlaybackTime to state.position.inWholeMilliseconds / 1000.0,
                MPNowPlayingInfoPropertyPlaybackRate to if (state.isPlaying) 1.0 else 0.0,
            )
        }
    }

    private fun registerRemoteCommands() {
        val center = MPRemoteCommandCenter.sharedCommandCenter()
        center.playCommand.addTargetWithHandler { handled { if (!state.value.isPlaying) togglePlayPause() } }
        center.pauseCommand.addTargetWithHandler { handled { if (state.value.isPlaying) togglePlayPause() } }
        center.togglePlayPauseCommand.addTargetWithHandler { handled { togglePlayPause() } }
        center.nextTrackCommand.addTargetWithHandler { handled { skipToNext() } }
        center.previousTrackCommand.addTargetWithHandler { handled { skipToPrevious() } }
        center.changePlaybackPositionCommand.addTargetWithHandler { event ->
            handled {
                (event as? MPChangePlaybackPositionCommandEvent)?.let { seekTo(it.positionTime.seconds) }
                // Força o Now Playing a pegar a posição nova.
                nowPlayingKey = null
            }
        }
    }

    private inline fun handled(block: () -> Unit): MPRemoteCommandHandlerStatus {
        block()
        return MPRemoteCommandHandlerStatusSuccess
    }

    private companion object {
        val PROGRESS_INTERVAL = 250.milliseconds
        val RESTART_THRESHOLD = 3.seconds
        val END_TOLERANCE = 500.milliseconds
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun kotlinx.cinterop.CValue<platform.CoreMedia.CMTime>.toDuration(): Duration {
    val seconds = CMTimeGetSeconds(this)
    return if (seconds.isNaN() || seconds.isInfinite() || seconds < 0) Duration.ZERO else seconds.seconds
}
