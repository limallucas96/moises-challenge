package com.lucaslima.musicsearch.wear.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.compose.material3.CircularProgressIndicator
import androidx.wear.compose.material3.FilledIconButton
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.IconButton
import androidx.wear.compose.material3.IconButtonDefaults
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import com.lucaslima.musicsearch.core.designsystem.icons.AppIcons
import com.lucaslima.musicsearch.feature.library.domain.RepeatMode
import com.lucaslima.musicsearch.feature.library.presentation.nowplaying.NowPlayingAction
import com.lucaslima.musicsearch.feature.library.presentation.nowplaying.NowPlayingState
import com.lucaslima.musicsearch.feature.library.presentation.nowplaying.NowPlayingViewModel
import com.lucaslima.musicsearch.wear.R
import org.koin.compose.viewmodel.koinViewModel

/** O mesmo ViewModel do player do celular. No relógio não há seek: o anel mostra o progresso. */
@Composable
internal fun PlayerRoot(viewModel: NowPlayingViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    PlayerScreen(state = state, onAction = viewModel::onAction)
}

@Composable
internal fun PlayerScreen(
    state: NowPlayingState,
    onAction: (NowPlayingAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    ScreenScaffold(modifier = modifier) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            val song = state.song
            if (song == null) {
                Message(stringResource(R.string.nothing_playing))
                return@Box
            }
            // A capa ao fundo, apagada, como no player de mídia do sistema.
            Artwork(url = song.artworkUrl, size = 240.dp, modifier = Modifier.alpha(0.3f))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = song.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = song.artistName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(
                        onClick = { onAction(NowPlayingAction.OnPreviousClick) },
                        enabled = state.canSkipPrevious,
                    ) {
                        Icon(AppIcons.SkipPrevious, contentDescription = stringResource(R.string.previous))
                    }
                    PlayPauseButton(state = state, onClick = { onAction(NowPlayingAction.OnPlayPauseClick) })
                    IconButton(
                        onClick = { onAction(NowPlayingAction.OnNextClick) },
                        enabled = state.canSkipNext,
                    ) {
                        Icon(AppIcons.SkipNext, contentDescription = stringResource(R.string.next))
                    }
                }
                RepeatButton(mode = state.player.repeatMode, onClick = { onAction(NowPlayingAction.OnRepeatClick) })
            }
        }
    }
}

/** Play/pause com o progresso da música em volta; girando sem fim enquanto carrega. */
@Composable
private fun PlayPauseButton(
    state: NowPlayingState,
    onClick: () -> Unit,
) {
    val duration = state.player.duration
    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(64.dp)) {
        if (state.player.isBuffering) {
            CircularProgressIndicator(modifier = Modifier.fillMaxSize())
        } else {
            CircularProgressIndicator(
                progress = {
                    if (duration.isPositive()) (state.position / duration).toFloat().coerceIn(0f, 1f) else 0f
                },
                modifier = Modifier.fillMaxSize(),
            )
        }
        FilledIconButton(onClick = onClick, modifier = Modifier.size(IconButtonDefaults.DefaultButtonSize)) {
            val playing = state.player.isPlaying
            Icon(
                imageVector = if (playing) AppIcons.Pause else AppIcons.Play,
                contentDescription = stringResource(if (playing) R.string.pause else R.string.play),
            )
        }
    }
}

@Composable
private fun RepeatButton(
    mode: RepeatMode,
    onClick: () -> Unit,
) {
    val description = when (mode) {
        RepeatMode.OFF -> R.string.repeat_off
        RepeatMode.ALL -> R.string.repeat_all
        RepeatMode.ONE -> R.string.repeat_one
    }
    IconButton(onClick = onClick, modifier = Modifier.alpha(if (mode == RepeatMode.OFF) 0.5f else 1f)) {
        Icon(
            imageVector = if (mode == RepeatMode.ONE) AppIcons.RepeatOne else AppIcons.Repeat,
            contentDescription = stringResource(description),
        )
    }
}
