package com.lucaslima.musicsearch.feature.library.presentation.nowplaying

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lucaslima.musicsearch.core.designsystem.icons.AppIcons
import com.lucaslima.musicsearch.core.designsystem.theme.AppTheme
import com.lucaslima.musicsearch.core.designsystem.theme.Radius
import com.lucaslima.musicsearch.core.designsystem.theme.Sizes
import com.lucaslima.musicsearch.core.designsystem.theme.Spacing
import com.lucaslima.musicsearch.feature.library.domain.PlayerState
import com.lucaslima.musicsearch.feature.library.domain.RepeatMode
import com.lucaslima.musicsearch.feature.library.domain.Song
import com.lucaslima.musicsearch.feature.library.presentation.components.Artwork
import com.lucaslima.musicsearch.feature.library.presentation.components.BackTopBar
import com.lucaslima.musicsearch.feature.library.presentation.components.SongOptionsSheet
import com.lucaslima.musicsearch.feature.library.presentation.components.StateMessage
import com.lucaslima.musicsearch.feature.library.presentation.components.previewSong
import com.lucaslima.musicsearch.feature.library.presentation.components.toClock
import com.lucaslima.musicsearch.feature.library.presentation.resources.Res
import com.lucaslima.musicsearch.feature.library.presentation.resources.next
import com.lucaslima.musicsearch.feature.library.presentation.resources.now_playing_empty_body
import com.lucaslima.musicsearch.feature.library.presentation.resources.now_playing_empty_title
import com.lucaslima.musicsearch.feature.library.presentation.resources.now_playing_error
import com.lucaslima.musicsearch.feature.library.presentation.resources.now_playing_more_options
import com.lucaslima.musicsearch.feature.library.presentation.resources.now_playing_title
import com.lucaslima.musicsearch.feature.library.presentation.resources.pause
import com.lucaslima.musicsearch.feature.library.presentation.resources.play
import com.lucaslima.musicsearch.feature.library.presentation.resources.playback_position
import com.lucaslima.musicsearch.feature.library.presentation.resources.playback_position_state
import com.lucaslima.musicsearch.feature.library.presentation.resources.previous
import com.lucaslima.musicsearch.feature.library.presentation.resources.repeat_all
import com.lucaslima.musicsearch.feature.library.presentation.resources.repeat_off
import com.lucaslima.musicsearch.feature.library.presentation.resources.repeat_one
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun NowPlayingRoot(
    onBackClick: () -> Unit,
    onOpenAlbum: (albumId: Long) -> Unit,
    viewModel: NowPlayingViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    NowPlayingScreen(
        state = state,
        onAction = { action ->
            when (action) {
                NowPlayingAction.OnBackClick -> onBackClick()
                is NowPlayingAction.OnViewAlbumClick -> onOpenAlbum(action.song.albumId)
                else -> Unit
            }
            viewModel.onAction(action)
        },
    )
}

/** "Song details" do Figma: capa, título, artista, linha do tempo e controles. */
@Composable
internal fun NowPlayingScreen(
    state: NowPlayingState,
    onAction: (NowPlayingAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppTheme.colors.bg)
            .systemBarsPadding(),
    ) {
        BackTopBar(
            title = stringResource(Res.string.now_playing_title),
            onBackClick = { onAction(NowPlayingAction.OnBackClick) },
            actions = {
                if (state.song != null) {
                    IconButton(onClick = { onAction(NowPlayingAction.OnMoreClick) }) {
                        Icon(
                            imageVector = AppIcons.MoreVert,
                            contentDescription = stringResource(Res.string.now_playing_more_options),
                            tint = AppTheme.colors.ink,
                        )
                    }
                }
            },
        )
        val song = state.song
        if (song == null) {
            StateMessage(
                icon = AppIcons.MusicNote,
                title = stringResource(Res.string.now_playing_empty_title),
                body = stringResource(Res.string.now_playing_empty_body),
                modifier = Modifier.weight(1f).padding(top = Spacing.xl),
            )
        } else {
            NowPlayingContent(song = song, state = state, onAction = onAction, modifier = Modifier.weight(1f))
        }
    }

    val optionsSong = state.song
    if (state.isOptionsOpen && optionsSong != null) {
        SongOptionsSheet(
            song = optionsSong,
            onViewAlbumClick = { onAction(NowPlayingAction.OnViewAlbumClick(optionsSong)) },
            onDismiss = { onAction(NowPlayingAction.OnOptionsDismiss) },
        )
    }
}

@Composable
private fun NowPlayingContent(
    song: Song,
    state: NowPlayingState,
    onAction: (NowPlayingAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.padding(horizontal = Spacing.lg)) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            Artwork(
                url = song.artworkUrl,
                cornerRadius = Radius.artworkLarge,
                modifier = Modifier
                    .fillMaxWidth(ARTWORK_WIDTH_FRACTION)
                    .widthIn(max = ARTWORK_MAX_WIDTH)
                    .aspectRatio(1f),
            )
        }
        Text(
            text = song.title,
            style = AppTheme.typography.playerTitle,
            color = AppTheme.colors.ink,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = song.artistName,
            style = AppTheme.typography.body,
            color = AppTheme.colors.muted,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = Spacing.xs),
        )
        if (state.player.hasError) {
            Text(
                text = stringResource(Res.string.now_playing_error),
                style = AppTheme.typography.caption,
                color = AppTheme.colors.muted,
                modifier = Modifier.padding(top = Spacing.sm),
            )
        }
        Timeline(
            position = state.position,
            duration = state.player.duration,
            onSeekChange = { onAction(NowPlayingAction.OnSeekChange(it)) },
            onSeekFinish = { onAction(NowPlayingAction.OnSeekFinished) },
            modifier = Modifier.padding(top = Spacing.md),
        )
        Controls(state = state, onAction = onAction, modifier = Modifier.padding(top = Spacing.sm, bottom = Spacing.xl))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Timeline(
    position: Duration,
    duration: Duration,
    onSeekChange: (Duration) -> Unit,
    onSeekFinish: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val hasDuration = duration > Duration.ZERO
    val totalMs = duration.inWholeMilliseconds.toFloat()
    val clamped = position.coerceIn(Duration.ZERO, duration.coerceAtLeast(Duration.ZERO))
    val label = stringResource(Res.string.playback_position)
    val positionState = stringResource(Res.string.playback_position_state, clamped.toClock(), duration.toClock())
    val colors = SliderDefaults.colors(
        thumbColor = AppTheme.colors.ink,
        activeTrackColor = AppTheme.colors.ink,
        inactiveTrackColor = AppTheme.colors.line,
        disabledThumbColor = AppTheme.colors.line,
        disabledInactiveTrackColor = AppTheme.colors.line,
    )
    Column(modifier = modifier.fillMaxWidth()) {
        Slider(
            value = if (hasDuration) clamped.inWholeMilliseconds.toFloat() else 0f,
            onValueChange = { onSeekChange(it.toLong().milliseconds) },
            onValueChangeFinished = onSeekFinish,
            valueRange = 0f..(if (hasDuration) totalMs else 1f),
            enabled = hasDuration,
            colors = colors,
            thumb = {
                Box(
                    Modifier
                        .size(THUMB_SIZE)
                        .background(if (hasDuration) AppTheme.colors.ink else AppTheme.colors.line, CircleShape),
                )
            },
            track = { sliderState ->
                SliderDefaults.Track(
                    sliderState = sliderState,
                    colors = colors,
                    enabled = hasDuration,
                    drawStopIndicator = null,
                    thumbTrackGapSize = 0.dp,
                    modifier = Modifier.height(TRACK_HEIGHT),
                )
            },
            modifier = Modifier.semantics {
                contentDescription = label
                stateDescription = positionState
            },
        )
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = clamped.toClock(),
                style = AppTheme.typography.caption,
                color = AppTheme.colors.muted,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = "-" + (duration - clamped).toClock(),
                style = AppTheme.typography.caption,
                color = AppTheme.colors.muted,
            )
        }
    }
}

@Composable
private fun Controls(
    state: NowPlayingState,
    onAction: (NowPlayingAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        PlayPauseButton(
            isPlaying = state.player.isPlaying,
            isBuffering = state.player.isBuffering,
            onClick = { onAction(NowPlayingAction.OnPlayPauseClick) },
        )
        Spacer(Modifier.size(Spacing.xs))
        ControlButton(
            icon = AppIcons.SkipPrevious,
            description = stringResource(Res.string.previous),
            enabled = state.canSkipPrevious,
            onClick = { onAction(NowPlayingAction.OnPreviousClick) },
        )
        ControlButton(
            icon = AppIcons.SkipNext,
            description = stringResource(Res.string.next),
            enabled = state.canSkipNext,
            onClick = { onAction(NowPlayingAction.OnNextClick) },
        )
        Spacer(Modifier.weight(1f))
        val repeat = state.player.repeatMode
        ControlButton(
            icon = if (repeat == RepeatMode.ONE) AppIcons.RepeatOne else AppIcons.Repeat,
            description = stringResource(
                when (repeat) {
                    RepeatMode.OFF -> Res.string.repeat_off
                    RepeatMode.ALL -> Res.string.repeat_all
                    RepeatMode.ONE -> Res.string.repeat_one
                },
            ),
            enabled = true,
            tint = if (repeat == RepeatMode.OFF) AppTheme.colors.muted else AppTheme.colors.ink,
            onClick = { onAction(NowPlayingAction.OnRepeatClick) },
        )
    }
}

@Composable
private fun PlayPauseButton(
    isPlaying: Boolean,
    isBuffering: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.size(Sizes.playButton), contentAlignment = Alignment.Center) {
        IconButton(
            onClick = onClick,
            colors = IconButtonDefaults.iconButtonColors(
                containerColor = AppTheme.colors.control,
                contentColor = AppTheme.colors.ink,
            ),
            modifier = Modifier.size(Sizes.playButton),
        ) {
            Icon(
                imageVector = if (isPlaying) AppIcons.Pause else AppIcons.Play,
                contentDescription = stringResource(if (isPlaying) Res.string.pause else Res.string.play),
                modifier = Modifier.size(32.dp),
            )
        }
        if (isBuffering) {
            CircularProgressIndicator(
                color = AppTheme.colors.ink,
                strokeWidth = 2.dp,
                modifier = Modifier.size(Sizes.playButton),
            )
        }
    }
}

@Composable
private fun ControlButton(
    icon: ImageVector,
    description: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = AppTheme.colors.ink,
) {
    IconButton(onClick = onClick, enabled = enabled, modifier = modifier.size(Sizes.touchTarget)) {
        Icon(
            imageVector = icon,
            contentDescription = description,
            tint = if (enabled) tint else AppTheme.colors.line,
            modifier = Modifier.size(32.dp),
        )
    }
}

private const val ARTWORK_WIDTH_FRACTION = 0.72f
private val ARTWORK_MAX_WIDTH = 360.dp
private val THUMB_SIZE = 12.dp
private val TRACK_HEIGHT = 3.dp

@Preview
@Composable
private fun NowPlayingScreenPreview() {
    AppTheme {
        NowPlayingScreen(
            state = NowPlayingState(
                player = PlayerState(
                    song = previewSong(),
                    isPlaying = true,
                    position = 86.seconds,
                    duration = 260.seconds,
                    hasNext = true,
                ),
            ),
            onAction = {},
        )
    }
}

@Preview
@Composable
private fun NowPlayingScreenEmptyPreview() {
    AppTheme {
        NowPlayingScreen(state = NowPlayingState(), onAction = {})
    }
}
