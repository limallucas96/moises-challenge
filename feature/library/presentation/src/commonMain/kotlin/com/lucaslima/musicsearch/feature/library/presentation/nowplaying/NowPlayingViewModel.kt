package com.lucaslima.musicsearch.feature.library.presentation.nowplaying

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lucaslima.musicsearch.feature.library.domain.PlayerController
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class NowPlayingViewModel(
    private val playerController: PlayerController,
    private val seekSettleTimeout: Duration = SEEK_SETTLE_TIMEOUT,
) : ViewModel() {

    private val _state = MutableStateFlow(NowPlayingState(player = playerController.state.value))
    val state = _state.asStateFlow()

    private var settleJob: Job? = null

    init {
        playerController.state
            .onEach { player ->
                _state.update { current ->
                    // O player chegou perto de onde o usuário soltou: volta a seguir o player.
                    val settled = !current.isScrubbing &&
                        current.scrubPosition?.let { (player.position - it).absoluteValue < SEEK_TOLERANCE } == true
                    current.copy(player = player, scrubPosition = current.scrubPosition.takeUnless { settled })
                }
            }
            .launchIn(viewModelScope)
    }

    fun onAction(action: NowPlayingAction) {
        when (action) {
            NowPlayingAction.OnPlayPauseClick -> playerController.togglePlayPause()

            NowPlayingAction.OnNextClick -> playerController.skipToNext()

            NowPlayingAction.OnPreviousClick -> playerController.skipToPrevious()

            NowPlayingAction.OnRepeatClick -> playerController.toggleRepeat()

            is NowPlayingAction.OnSeekChange -> {
                settleJob?.cancel()
                _state.update { it.copy(scrubPosition = action.position, isScrubbing = true) }
            }

            NowPlayingAction.OnSeekFinished -> finishSeek()

            NowPlayingAction.OnMoreClick -> _state.update { it.copy(isOptionsOpen = true) }

            // A navegação é do Root; aqui só fecha o sheet.
            NowPlayingAction.OnOptionsDismiss,
            is NowPlayingAction.OnViewAlbumClick,
            -> _state.update { it.copy(isOptionsOpen = false) }

            NowPlayingAction.OnBackClick -> Unit
        }
    }

    private fun finishSeek() {
        val target = _state.value.scrubPosition ?: return
        playerController.seekTo(target)
        _state.update { it.copy(isScrubbing = false) }
        // Se o player não confirmar a posição (rede lenta, erro), não prende a tela nela.
        settleJob = viewModelScope.launch {
            delay(seekSettleTimeout)
            _state.update { if (it.isScrubbing) it else it.copy(scrubPosition = null) }
        }
    }

    companion object {
        val SEEK_SETTLE_TIMEOUT = 1.seconds
        private val SEEK_TOLERANCE = 1.seconds
    }
}
