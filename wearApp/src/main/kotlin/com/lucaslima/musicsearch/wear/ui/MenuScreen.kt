package com.lucaslima.musicsearch.wear.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import com.lucaslima.musicsearch.core.designsystem.icons.AppIcons
import com.lucaslima.musicsearch.feature.library.domain.Song
import com.lucaslima.musicsearch.feature.library.presentation.nowplaying.NowPlayingViewModel
import com.lucaslima.musicsearch.wear.R
import org.koin.compose.viewmodel.koinViewModel

/** O menu só lê o que está tocando; o resto é navegação. */
@Composable
internal fun MenuRoot(
    onOpenPlayer: () -> Unit,
    onOpenSongs: () -> Unit,
    onOpenAlbums: () -> Unit,
    viewModel: NowPlayingViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    MenuScreen(
        playing = state.song,
        onOpenPlayer = onOpenPlayer,
        onOpenSongs = onOpenSongs,
        onOpenAlbums = onOpenAlbums,
    )
}

@Composable
internal fun MenuScreen(
    playing: Song?,
    onOpenPlayer: () -> Unit,
    onOpenSongs: () -> Unit,
    onOpenAlbums: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberTransformingLazyColumnState()
    ScreenScaffold(scrollState = listState, modifier = modifier) { contentPadding ->
        TransformingLazyColumn(state = listState, contentPadding = contentPadding) {
            item { ListHeader { Text(stringResource(R.string.menu_title)) } }
            item {
                Button(
                    onClick = onOpenPlayer,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.filledTonalButtonColors(),
                    icon = { Icon(AppIcons.Play, contentDescription = null) },
                    label = { Text(stringResource(R.string.now_playing)) },
                    secondaryLabel = {
                        Text(
                            text = playing?.title ?: stringResource(R.string.nothing_playing),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    },
                )
            }
            item {
                Button(
                    onClick = onOpenAlbums,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.filledTonalButtonColors(),
                    icon = { Icon(AppIcons.Album, contentDescription = null) },
                    label = { Text(stringResource(R.string.albums)) },
                )
            }
            item {
                Button(
                    onClick = onOpenSongs,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.filledTonalButtonColors(),
                    icon = { Icon(AppIcons.MusicNote, contentDescription = null) },
                    label = { Text(stringResource(R.string.songs)) },
                )
            }
        }
    }
}
