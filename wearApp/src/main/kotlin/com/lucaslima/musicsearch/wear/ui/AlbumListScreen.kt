package com.lucaslima.musicsearch.wear.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import com.lucaslima.musicsearch.feature.library.domain.Song
import com.lucaslima.musicsearch.feature.library.presentation.songs.SongsViewModel
import com.lucaslima.musicsearch.wear.R
import org.koin.compose.viewmodel.koinViewModel

/** Os álbuns saem dos recentes: o relógio não tem tela de busca de álbum. */
@Composable
internal fun AlbumListRoot(
    onOpenAlbum: (albumId: Long) -> Unit,
    viewModel: SongsViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val albums = remember(state.recentlyPlayed) { state.recentlyPlayed.distinctBy { it.albumId } }
    AlbumListScreen(
        albums = albums,
        isLoaded = state.isRecentlyPlayedLoaded,
        onAlbumClick = onOpenAlbum,
    )
}

/** [albums] traz uma música de cada álbum, que dá título, artista e capa. */
@Composable
internal fun AlbumListScreen(
    albums: List<Song>,
    isLoaded: Boolean,
    onAlbumClick: (albumId: Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberTransformingLazyColumnState()
    ScreenScaffold(scrollState = listState, modifier = modifier) { contentPadding ->
        TransformingLazyColumn(state = listState, contentPadding = contentPadding) {
            item { ListHeader { Text(stringResource(R.string.albums)) } }
            if (isLoaded && albums.isEmpty()) {
                item { Message(stringResource(R.string.albums_empty)) }
            }
            items(count = albums.size, key = { albums[it].albumId }) { index ->
                val album = albums[index]
                Button(
                    onClick = { onAlbumClick(album.albumId) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    ),
                    icon = { Artwork(url = album.artworkUrl) },
                    label = { Text(album.albumTitle, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                    secondaryLabel = { Text(album.artistName, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                )
            }
        }
    }
}
