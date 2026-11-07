package com.lucaslima.musicsearch.wear.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.CircularProgressIndicator
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import com.lucaslima.musicsearch.feature.library.presentation.album.AlbumAction
import com.lucaslima.musicsearch.feature.library.presentation.album.AlbumState
import com.lucaslima.musicsearch.feature.library.presentation.album.AlbumViewModel
import com.lucaslima.musicsearch.wear.R
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

/** O mesmo ViewModel da tela de álbum do celular: tocar uma faixa toca o álbum a partir dela. */
@Composable
internal fun TrackListRoot(
    albumId: Long,
    onOpenPlayer: () -> Unit,
    viewModel: AlbumViewModel = koinViewModel { parametersOf(albumId) },
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    TrackListScreen(
        state = state,
        onAction = { action ->
            viewModel.onAction(action)
            if (action is AlbumAction.OnSongClick) onOpenPlayer()
        },
    )
}

@Composable
internal fun TrackListScreen(
    state: AlbumState,
    onAction: (AlbumAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberTransformingLazyColumnState()
    ScreenScaffold(scrollState = listState, modifier = modifier) { contentPadding ->
        TransformingLazyColumn(state = listState, contentPadding = contentPadding) {
            val album = state.album
            when {
                album != null -> {
                    item {
                        ListHeader {
                            Text(
                                album.title,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                    item {
                        Text(
                            text = album.artistName,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    items(count = album.songs.size, key = { album.songs[it].id }) { index ->
                        val song = album.songs[index]
                        SongButton(
                            song = song,
                            isPlaying = song.id == state.playingSongId,
                            onClick = { onAction(AlbumAction.OnSongClick(song)) },
                        )
                    }
                }

                state.isLoading -> item { CircularProgressIndicator() }

                else -> {
                    item { Message(stringResource(R.string.album_error)) }
                    item {
                        Button(
                            onClick = { onAction(AlbumAction.OnRetryClick) },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text(stringResource(R.string.retry)) },
                        )
                    }
                }
            }
        }
    }
}
