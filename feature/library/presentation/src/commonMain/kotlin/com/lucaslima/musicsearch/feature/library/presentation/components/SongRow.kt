package com.lucaslima.musicsearch.feature.library.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
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
import com.lucaslima.musicsearch.feature.library.presentation.resources.song_more_options
import com.lucaslima.musicsearch.feature.library.presentation.resources.song_no_preview
import com.lucaslima.musicsearch.feature.library.presentation.resources.song_now_playing
import com.lucaslima.musicsearch.feature.library.presentation.resources.song_play
import kotlin.time.Duration.Companion.seconds
import org.jetbrains.compose.resources.stringResource

/**
 * Linha de música: capa, título, artista e o botão de mais opções. Música sem prévia de áudio
 * aparece esmaecida e não toca, mas ainda abre as opções (dá para ver o álbum).
 */
@Composable
internal fun SongRow(
    song: Song,
    isPlaying: Boolean,
    onClick: () -> Unit,
    onMoreClick: () -> Unit,
    modifier: Modifier = Modifier,
    showArtwork: Boolean = true,
) {
    val isPlayable = song.previewUrl != null
    val nowPlaying = stringResource(Res.string.song_now_playing)
    val noPreview = stringResource(Res.string.song_no_preview)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                enabled = isPlayable,
                onClickLabel = stringResource(Res.string.song_play),
                role = Role.Button,
                onClick = onClick,
            )
            .semantics {
                when {
                    isPlaying -> stateDescription = nowPlaying
                    !isPlayable -> stateDescription = noPreview
                }
            }
            .padding(start = Spacing.md, top = Spacing.sm, bottom = Spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .alpha(if (isPlayable) 1f else DISABLED_ALPHA),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.md),
        ) {
            if (showArtwork) {
                Artwork(url = song.artworkUrl, size = Sizes.artworkSmall, cornerRadius = Radius.artworkSmall)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = song.title,
                    style = AppTheme.typography.itemTitle,
                    color = AppTheme.colors.ink,
                    fontWeight = if (isPlaying) FontWeight.SemiBold else null,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = if (isPlayable) song.artistName else "${song.artistName} · $noPreview",
                    style = AppTheme.typography.itemSubtitle,
                    color = AppTheme.colors.muted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        IconButton(onClick = onMoreClick) {
            Icon(
                imageVector = AppIcons.MoreVert,
                contentDescription = stringResource(Res.string.song_more_options, song.title),
                tint = AppTheme.colors.muted,
            )
        }
    }
}

/** Esqueleto de [SongRow] enquanto a primeira página carrega. */
@Composable
internal fun SongRowPlaceholder(modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(Radius.artworkSmall)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.md, vertical = Spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.md),
    ) {
        Box(Modifier.size(Sizes.artworkSmall).background(AppTheme.colors.field, shape))
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            Box(Modifier.width(160.dp).height(12.dp).background(AppTheme.colors.field, shape))
            Box(Modifier.width(96.dp).height(10.dp).background(AppTheme.colors.field, shape))
        }
    }
}

private const val DISABLED_ALPHA = 0.4f

@Preview
@Composable
private fun SongRowPreview() {
    AppTheme {
        Column(Modifier.background(AppTheme.colors.bg)) {
            SongRow(song = previewSong(), isPlaying = false, onClick = {}, onMoreClick = {})
            SongRow(song = previewSong(previewUrl = null), isPlaying = false, onClick = {}, onMoreClick = {})
            SongRowPlaceholder()
        }
    }
}

internal fun previewSong(
    id: Long = 1,
    title: String = "Get Lucky",
    previewUrl: String? = "https://example.com/preview.m4a",
) = Song(
    id = id,
    title = title,
    artistName = "Daft Punk feat. Pharrell Williams",
    albumId = 10,
    albumTitle = "Random Access Memories",
    artworkUrl = null,
    previewUrl = previewUrl,
    duration = 248.seconds,
    trackNumber = 8,
)
