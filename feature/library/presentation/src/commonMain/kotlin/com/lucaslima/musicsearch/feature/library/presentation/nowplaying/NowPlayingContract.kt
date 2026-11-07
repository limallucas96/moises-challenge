package com.lucaslima.musicsearch.feature.library.presentation.nowplaying

import com.lucaslima.musicsearch.feature.library.domain.PlayerState
import com.lucaslima.musicsearch.feature.library.domain.RepeatMode
import com.lucaslima.musicsearch.feature.library.domain.Song
import kotlin.time.Duration

data class NowPlayingState(
    val player: PlayerState = PlayerState(),
    /**
     * Posição escolhida no slider. Enquanto o dedo arrasta, e logo depois de soltar até o player
     * chegar lá, a tela mostra esta e não a do player: sem ela o thumb pularia de volta.
     */
    val scrubPosition: Duration? = null,
    val isScrubbing: Boolean = false,
    val isOptionsOpen: Boolean = false,
) {
    val song: Song? get() = player.song
    val position: Duration get() = scrubPosition ?: player.position

    /** Com repetir tudo, a última música passa para a primeira. */
    val canSkipNext: Boolean get() = player.hasNext || (player.repeatMode == RepeatMode.ALL && song != null)

    /** Voltar sempre funciona com música: no mínimo reinicia a atual. */
    val canSkipPrevious: Boolean get() = song != null
}

sealed interface NowPlayingAction {
    data object OnBackClick : NowPlayingAction

    data object OnPlayPauseClick : NowPlayingAction

    data object OnNextClick : NowPlayingAction

    data object OnPreviousClick : NowPlayingAction

    data object OnRepeatClick : NowPlayingAction

    data class OnSeekChange(val position: Duration) : NowPlayingAction

    data object OnSeekFinished : NowPlayingAction

    data object OnMoreClick : NowPlayingAction

    data object OnOptionsDismiss : NowPlayingAction

    data class OnViewAlbumClick(val song: Song) : NowPlayingAction
}
