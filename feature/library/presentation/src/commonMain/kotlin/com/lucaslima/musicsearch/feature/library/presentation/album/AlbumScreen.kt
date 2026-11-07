package com.lucaslima.musicsearch.feature.library.presentation.album

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lucaslima.musicsearch.core.designsystem.icons.AppIcons
import com.lucaslima.musicsearch.core.designsystem.theme.AppTheme
import com.lucaslima.musicsearch.core.designsystem.theme.Radius
import com.lucaslima.musicsearch.core.designsystem.theme.Sizes
import com.lucaslima.musicsearch.core.designsystem.theme.Spacing
import com.lucaslima.musicsearch.core.presentation.toUiText
import com.lucaslima.musicsearch.feature.library.domain.Album
import com.lucaslima.musicsearch.feature.library.presentation.components.Artwork
import com.lucaslima.musicsearch.feature.library.presentation.components.ArtworkTransition
import com.lucaslima.musicsearch.feature.library.presentation.components.BackTopBar
import com.lucaslima.musicsearch.feature.library.presentation.components.SongRow
import com.lucaslima.musicsearch.feature.library.presentation.components.StateMessage
import com.lucaslima.musicsearch.feature.library.presentation.components.previewSong
import com.lucaslima.musicsearch.feature.library.presentation.resources.Res
import com.lucaslima.musicsearch.feature.library.presentation.resources.album_title_fallback
import com.lucaslima.musicsearch.feature.library.presentation.resources.loading
import com.lucaslima.musicsearch.feature.library.presentation.resources.retry
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
internal fun AlbumRoot(
    albumId: Long,
    onBackClick: () -> Unit,
    onOpenPlayer: () -> Unit,
    artworkTransition: ArtworkTransition?,
    viewModel: AlbumViewModel = koinViewModel { parametersOf(albumId) },
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    AlbumScreen(
        state = state,
        onAction = { action ->
            when (action) {
                AlbumAction.OnBackClick -> onBackClick()
                is AlbumAction.OnSongClick -> if (action.song.previewUrl != null) onOpenPlayer()
                AlbumAction.OnRetryClick -> Unit
            }
            viewModel.onAction(action)
        },
        artworkTransition = artworkTransition,
    )
}

/** Capa, título e artista no topo; as faixas na ordem do disco. Tocar uma toca o álbum dali. */
@Composable
internal fun AlbumScreen(
    state: AlbumState,
    onAction: (AlbumAction) -> Unit,
    modifier: Modifier = Modifier,
    artworkTransition: ArtworkTransition? = null,
) {
    val album = state.album
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppTheme.colors.bg)
            .systemBarsPadding(),
    ) {
        BackTopBar(
            title = album?.title ?: stringResource(Res.string.album_title_fallback),
            onBackClick = { onAction(AlbumAction.OnBackClick) },
        )
        when {
            album != null -> AlbumContent(
                album = album,
                state = state,
                onAction = onAction,
                artworkTransition = artworkTransition,
            )

            state.error != null -> StateMessage(
                icon = AppIcons.CloudOff,
                title = state.error.toUiText().asString(),
                body = null,
                actionLabel = stringResource(Res.string.retry),
                onActionClick = { onAction(AlbumAction.OnRetryClick) },
                modifier = Modifier.padding(top = Spacing.xl),
            )

            else -> Loading(Modifier.fillMaxSize())
        }
    }
}

@Composable
private fun AlbumContent(
    album: Album,
    state: AlbumState,
    onAction: (AlbumAction) -> Unit,
    artworkTransition: ArtworkTransition?,
    modifier: Modifier = Modifier,
) {
    LazyColumn(modifier = modifier.fillMaxSize()) {
        item(key = "header", contentType = "header") {
            AlbumHeader(album = album)
        }
        items(items = album.songs, key = { it.id }, contentType = { "song" }) { song ->
            // O Figma não tem "mais opções" no álbum: o sheet só leva ao próprio álbum.
            SongRow(
                song = song,
                isPlaying = song.id == state.playingSongId,
                onClick = { onAction(AlbumAction.OnSongClick(song)) },
                onMoreClick = null,
                artworkTransition = artworkTransition,
            )
        }
    }
}

@Composable
private fun AlbumHeader(
    album: Album,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.lg, vertical = Spacing.md),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Spacing.xs),
    ) {
        Artwork(url = album.artworkUrl, size = Sizes.artworkMedium, cornerRadius = Radius.artworkLarge)
        Text(
            text = album.title,
            style = AppTheme.typography.screenTitle,
            color = AppTheme.colors.ink,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(top = Spacing.sm)
                .semantics { heading() },
        )
        Text(
            text = album.artistName,
            style = AppTheme.typography.caption,
            color = AppTheme.colors.muted,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun Loading(modifier: Modifier = Modifier) {
    val label = stringResource(Res.string.loading)
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        CircularProgressIndicator(
            color = AppTheme.colors.muted,
            strokeWidth = 2.dp,
            modifier = Modifier
                .size(32.dp)
                .semantics { contentDescription = label },
        )
    }
}

@Preview
@Composable
private fun AlbumScreenPreview() {
    AppTheme {
        AlbumScreen(
            state = AlbumState(
                album = Album(
                    id = 10,
                    title = "Random Access Memories",
                    artistName = "Daft Punk",
                    artworkUrl = null,
                    songs = listOf(previewSong(1, "Give Life Back to Music"), previewSong(2, "The Game of Love")),
                ),
                isLoading = false,
                playingSongId = 2,
            ),
            onAction = {},
        )
    }
}
