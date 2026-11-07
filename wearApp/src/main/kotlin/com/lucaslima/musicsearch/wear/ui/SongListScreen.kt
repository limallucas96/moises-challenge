package com.lucaslima.musicsearch.wear.ui

import android.app.Activity
import android.app.RemoteInput
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.CircularProgressIndicator
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import com.lucaslima.musicsearch.core.designsystem.icons.AppIcons
import com.lucaslima.musicsearch.feature.library.domain.Song
import com.lucaslima.musicsearch.feature.library.presentation.songs.SongsAction
import com.lucaslima.musicsearch.feature.library.presentation.songs.SongsState
import com.lucaslima.musicsearch.feature.library.presentation.songs.SongsViewModel
import com.lucaslima.musicsearch.wear.R
import org.koin.compose.viewmodel.koinViewModel

/**
 * O mesmo ViewModel da Home do celular. Sem teclado na tela, a busca vem do RemoteInput do
 * sistema: voz, teclado ou rabisco, à escolha de quem usa.
 */
@Composable
internal fun SongListRoot(
    onOpenPlayer: () -> Unit,
    viewModel: SongsViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val results = viewModel.searchResults.collectAsLazyPagingItems()
    val searchLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val query = result.data
            ?.takeIf { result.resultCode == Activity.RESULT_OK }
            ?.let { RemoteInput.getResultsFromIntent(it) }
            ?.getCharSequence(SEARCH_INPUT_KEY)
            ?.toString()
        if (!query.isNullOrBlank()) viewModel.onAction(SongsAction.OnQueryChange(query))
    }
    val searchPrompt = stringResource(R.string.search_prompt)
    SongListScreen(
        state = state,
        results = results,
        onSearchClick = { searchLauncher.launch(remoteInputIntent(searchPrompt)) },
        onAction = { action ->
            viewModel.onAction(action)
            if (action is SongsAction.OnSongClick) onOpenPlayer()
        },
    )
}

@Composable
internal fun SongListScreen(
    state: SongsState,
    results: LazyPagingItems<Song>,
    onSearchClick: () -> Unit,
    onAction: (SongsAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberTransformingLazyColumnState()
    ScreenScaffold(scrollState = listState, modifier = modifier) { contentPadding ->
        TransformingLazyColumn(state = listState, contentPadding = contentPadding) {
            item { ListHeader { Text(stringResource(R.string.songs)) } }
            item {
                Button(
                    onClick = onSearchClick,
                    modifier = Modifier.fillMaxWidth(),
                    icon = { Icon(AppIcons.Search, contentDescription = null) },
                    label = {
                        Text(
                            text = state.query.ifBlank { stringResource(R.string.search) },
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    },
                )
            }
            if (state.isSearching) {
                item {
                    Button(
                        onClick = { onAction(SongsAction.OnClearQueryClick) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(),
                        border = ButtonDefaults.outlinedButtonBorder(enabled = true),
                        icon = { Icon(AppIcons.Close, contentDescription = null) },
                        label = { Text(stringResource(R.string.clear_search)) },
                    )
                }
                item { ListHeader { Text(stringResource(R.string.results)) } }
                val visible = results.itemSnapshotList.items
                items(count = results.itemCount) { index ->
                    results[index]?.let { song ->
                        SongButton(
                            song = song,
                            isPlaying = song.id == state.playingSongId,
                            onClick = { onAction(SongsAction.OnSongClick(song, visible)) },
                        )
                    }
                }
                val refresh = results.loadState.refresh
                when {
                    refresh is LoadState.Loading || results.loadState.append is LoadState.Loading ->
                        item { CircularProgressIndicator() }

                    refresh is LoadState.Error && results.itemCount == 0 -> item {
                        Message(stringResource(R.string.search_error))
                    }

                    refresh is LoadState.NotLoading && results.itemCount == 0 &&
                        results.loadState.append.endOfPaginationReached -> item {
                        Message(stringResource(R.string.no_results))
                    }
                }
            } else {
                item { ListHeader { Text(stringResource(R.string.recently_played)) } }
                if (state.isRecentlyPlayedLoaded && state.recentlyPlayed.isEmpty()) {
                    item { Message(stringResource(R.string.recents_empty)) }
                }
                items(count = state.recentlyPlayed.size, key = { state.recentlyPlayed[it].id }) { index ->
                    val song = state.recentlyPlayed[index]
                    SongButton(
                        song = song,
                        isPlaying = song.id == state.playingSongId,
                        onClick = { onAction(SongsAction.OnSongClick(song, state.recentlyPlayed)) },
                    )
                }
            }
        }
    }
}

private const val SEARCH_INPUT_KEY = "query"

/**
 * O pedido de texto do Wear OS (o mesmo que o `RemoteInputIntentHelper` do wear-input monta),
 * sem trazer a biblioteca por duas linhas.
 */
private fun remoteInputIntent(label: String): Intent =
    Intent("android.support.wearable.input.action.REMOTE_INPUT").putExtra(
        "android.support.wearable.input.extra.REMOTE_INPUTS",
        arrayOf(RemoteInput.Builder(SEARCH_INPUT_KEY).setLabel(label).build()),
    )
