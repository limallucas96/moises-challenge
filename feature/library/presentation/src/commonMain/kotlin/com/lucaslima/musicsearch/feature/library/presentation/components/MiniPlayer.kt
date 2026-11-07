package com.lucaslima.musicsearch.feature.library.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.lucaslima.musicsearch.core.designsystem.icons.AppIcons
import com.lucaslima.musicsearch.core.designsystem.theme.AppTheme
import com.lucaslima.musicsearch.core.designsystem.theme.Radius
import com.lucaslima.musicsearch.core.designsystem.theme.Sizes
import com.lucaslima.musicsearch.core.designsystem.theme.Spacing
import com.lucaslima.musicsearch.feature.library.domain.Song
import com.lucaslima.musicsearch.feature.library.presentation.resources.Res
import com.lucaslima.musicsearch.feature.library.presentation.resources.mini_player_open
import com.lucaslima.musicsearch.feature.library.presentation.resources.pause
import com.lucaslima.musicsearch.feature.library.presentation.resources.play
import org.jetbrains.compose.resources.stringResource

/**
 * Barra flutuante com a música do player: capa, título, artista e play/pause. Tocar na barra abre
 * o player; o botão alterna sem sair da tela.
 */
@Composable
internal fun MiniPlayer(
    song: Song,
    isPlaying: Boolean,
    onClick: () -> Unit,
    onPlayPauseClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(Radius.artworkLarge)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .shadow(elevation = 8.dp, shape = shape)
            .clip(shape)
            .background(AppTheme.colors.sheet)
            .clickable(
                onClickLabel = stringResource(Res.string.mini_player_open),
                role = Role.Button,
                onClick = onClick,
            )
            .padding(start = Spacing.sm, top = Spacing.sm, bottom = Spacing.sm, end = Spacing.xs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.md),
    ) {
        Artwork(url = song.artworkUrl, size = Sizes.artworkSmall, cornerRadius = Radius.artworkSmall)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = song.title,
                style = AppTheme.typography.itemTitle,
                color = AppTheme.colors.ink,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = song.artistName,
                style = AppTheme.typography.itemSubtitle,
                color = AppTheme.colors.muted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        IconButton(onClick = onPlayPauseClick) {
            Icon(
                imageVector = if (isPlaying) AppIcons.Pause else AppIcons.Play,
                contentDescription = stringResource(if (isPlaying) Res.string.pause else Res.string.play),
                tint = AppTheme.colors.ink,
            )
        }
    }
}

@Preview
@Composable
private fun MiniPlayerPreview() {
    AppTheme {
        MiniPlayer(
            song = previewSong(id = 1, title = "One More Time"),
            isPlaying = true,
            onClick = {},
            onPlayPauseClick = {},
            modifier = Modifier.padding(Spacing.md),
        )
    }
}
