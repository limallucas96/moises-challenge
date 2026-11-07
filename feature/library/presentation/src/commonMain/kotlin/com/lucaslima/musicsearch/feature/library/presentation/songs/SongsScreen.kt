package com.lucaslima.musicsearch.feature.library.presentation.songs

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.lucaslima.musicsearch.core.designsystem.icons.AppIcons
import com.lucaslima.musicsearch.core.designsystem.theme.AppTheme
import com.lucaslima.musicsearch.core.designsystem.theme.Radius
import com.lucaslima.musicsearch.core.designsystem.theme.Spacing
import com.lucaslima.musicsearch.core.domain.DataError
import com.lucaslima.musicsearch.core.domain.DataErrorException
import com.lucaslima.musicsearch.core.presentation.toUiText
import com.lucaslima.musicsearch.feature.library.domain.Song
import com.lucaslima.musicsearch.feature.library.presentation.components.SearchField
import com.lucaslima.musicsearch.feature.library.presentation.components.SongOptionsSheet
import com.lucaslima.musicsearch.feature.library.presentation.components.SongRow
import com.lucaslima.musicsearch.feature.library.presentation.components.SongRowPlaceholder
import com.lucaslima.musicsearch.feature.library.presentation.components.StateMessage
import com.lucaslima.musicsearch.feature.library.presentation.components.previewSong
import com.lucaslima.musicsearch.feature.library.presentation.resources.Res
import com.lucaslima.musicsearch.feature.library.presentation.resources.loading
import com.lucaslima.musicsearch.feature.library.presentation.resources.recently_played
import com.lucaslima.musicsearch.feature.library.presentation.resources.recents_empty_body
import com.lucaslima.musicsearch.feature.library.presentation.resources.recents_empty_title
import com.lucaslima.musicsearch.feature.library.presentation.resources.retry
import com.lucaslima.musicsearch.feature.library.presentation.resources.search_no_results_body
import com.lucaslima.musicsearch.feature.library.presentation.resources.search_no_results_title
import com.lucaslima.musicsearch.feature.library.presentation.resources.search_offline_banner
import com.lucaslima.musicsearch.feature.library.presentation.resources.search_open
import com.lucaslima.musicsearch.feature.library.presentation.resources.songs_title
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/**
 * Home. A navegação fica aqui: tocar uma música abre o player e "View album" abre o álbum; o
 * ViewModel cuida do resto.
 */
@Composable
internal fun SongsRoot(
    onOpenPlayer: () -> Unit,
    onOpenAlbum: (albumId: Long) -> Unit,
    viewModel: SongsViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val songs = viewModel.searchResults.collectAsLazyPagingItems()
    SongsScreen(
        state = state,
        songs = songs,
        onAction = { action ->
            when (action) {
                is SongsAction.OnSongClick -> onOpenPlayer()
                is SongsAction.OnViewAlbumClick -> onOpenAlbum(action.song.albumId)
                else -> Unit
            }
            viewModel.onAction(action)
        },
    )
}

/**
 * O campo de busca é o primeiro item da lista: some ao rolar e, fora da tela, vira uma lupa no
 * topo que volta para ele. Sem termo, mostra os tocados recentemente; com termo, os resultados
 * paginados, com puxar para atualizar.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SongsScreen(
    state: SongsState,
    songs: LazyPagingItems<Song>,
    onAction: (SongsAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val searchFocus = remember { FocusRequester() }
    val isSearchFieldHidden by remember { derivedStateOf { listState.firstVisibleItemIndex > 0 } }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppTheme.colors.bg)
            .systemBarsPadding(),
    ) {
        SongsTopBar(
            showSearchButton = isSearchFieldHidden,
            onSearchClick = {
                scope.launch {
                    listState.animateScrollToItem(0)
                    searchFocus.requestFocus()
                }
            },
        )
        PullToRefreshBox(
            isRefreshing = state.isSearching && songs.isRefreshingWithContent(),
            onRefresh = { if (state.isSearching) songs.refresh() },
            modifier = Modifier.fillMaxSize(),
        ) {
            LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
                item(key = "search", contentType = "search") {
                    SearchField(
                        query = state.query,
                        onQueryChange = { onAction(SongsAction.OnQueryChange(it)) },
                        onClearClick = { onAction(SongsAction.OnClearQueryClick) },
                        modifier = Modifier
                            .padding(horizontal = Spacing.md, vertical = Spacing.sm)
                            .focusRequester(searchFocus),
                    )
                }
                if (state.isSearching) {
                    searchResults(state = state, songs = songs, onAction = onAction)
                } else {
                    recentlyPlayed(state = state, onAction = onAction)
                }
            }
        }
    }

    state.optionsSong?.let { song ->
        SongOptionsSheet(
            song = song,
            onViewAlbumClick = { onAction(SongsAction.OnViewAlbumClick(song)) },
            onDismiss = { onAction(SongsAction.OnOptionsDismiss) },
        )
    }
}

@Composable
private fun SongsTopBar(
    showSearchButton: Boolean,
    onSearchClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .padding(start = Spacing.md, end = Spacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(Res.string.songs_title),
            style = AppTheme.typography.screenTitle,
            color = AppTheme.colors.ink,
            modifier = Modifier
                .weight(1f)
                .semantics { heading() },
        )
        AnimatedVisibility(visible = showSearchButton, enter = fadeIn(), exit = fadeOut()) {
            IconButton(onClick = onSearchClick) {
                Icon(
                    imageVector = AppIcons.Search,
                    contentDescription = stringResource(Res.string.search_open),
                    tint = AppTheme.colors.ink,
                )
            }
        }
    }
}

private fun LazyListScope.recentlyPlayed(
    state: SongsState,
    onAction: (SongsAction) -> Unit,
) {
    when {
        // Antes da primeira leitura do banco não mostra nada: evita piscar o vazio.
        !state.isRecentlyPlayedLoaded -> Unit

        state.recentlyPlayed.isEmpty() -> item(key = "recents-empty", contentType = "message") {
            StateMessage(
                icon = AppIcons.MusicNote,
                title = stringResource(Res.string.recents_empty_title),
                body = stringResource(Res.string.recents_empty_body),
                modifier = Modifier.padding(top = Spacing.xl),
            )
        }

        else -> {
            item(key = "recents-header", contentType = "header") {
                SectionHeader(text = stringResource(Res.string.recently_played))
            }
            items(items = state.recentlyPlayed, key = { "recent-${it.id}" }, contentType = { "song" }) { song ->
                SongRow(
                    song = song,
                    isPlaying = song.id == state.playingSongId,
                    onClick = { onAction(SongsAction.OnSongClick(song, state.recentlyPlayed)) },
                    onMoreClick = { onAction(SongsAction.OnMoreClick(song)) },
                    modifier = Modifier.animateItem(),
                )
            }
        }
    }
}

private fun LazyListScope.searchResults(
    state: SongsState,
    songs: LazyPagingItems<Song>,
    onAction: (SongsAction) -> Unit,
) {
    val refresh = songs.loadState.refresh
    val refreshError = (refresh as? LoadState.Error)?.toDataError()
    val isEmpty = songs.itemCount == 0

    when {
        isEmpty && refresh is LoadState.Loading -> items(count = PLACEHOLDER_ROWS, contentType = { "placeholder" }) {
            SongRowPlaceholder()
        }

        isEmpty && refreshError != null -> item(key = "error", contentType = "message") {
            StateMessage(
                icon = AppIcons.CloudOff,
                title = refreshError.toUiText().asString(),
                body = null,
                actionLabel = stringResource(Res.string.retry),
                onActionClick = songs::retry,
                modifier = Modifier.padding(top = Spacing.xl),
            )
        }

        isEmpty && songs.loadState.append.endOfPaginationReached -> item(key = "no-results", contentType = "message") {
            StateMessage(
                icon = AppIcons.Search,
                title = stringResource(Res.string.search_no_results_title, state.query.trim()),
                body = stringResource(Res.string.search_no_results_body),
                modifier = Modifier.padding(top = Spacing.xl),
            )
        }
    }

    // Tem resultado salvo mas a rede falhou: mostra o salvo e avisa.
    if (!isEmpty && refreshError != null) {
        item(key = "offline-banner", contentType = "banner") {
            OfflineBanner(onRetryClick = songs::retry)
        }
    }

    items(count = songs.itemCount, key = songs.itemKey { "song-${it.id}" }, contentType = { "song" }) { index ->
        val song = songs[index] ?: return@items
        SongRow(
            song = song,
            isPlaying = song.id == state.playingSongId,
            onClick = { onAction(SongsAction.OnSongClick(song, songs.itemSnapshotList.items)) },
            onMoreClick = { onAction(SongsAction.OnMoreClick(song)) },
        )
    }

    if (!isEmpty) {
        when (val append = songs.loadState.append) {
            is LoadState.Loading -> item(key = "append-loading", contentType = "footer") { LoadingFooter() }

            is LoadState.Error -> item(key = "append-error", contentType = "footer") {
                StateMessage(
                    icon = AppIcons.CloudOff,
                    title = append.toDataError().toUiText().asString(),
                    body = null,
                    actionLabel = stringResource(Res.string.retry),
                    onActionClick = songs::retry,
                )
            }

            is LoadState.NotLoading -> Unit
        }
    }
}

@Composable
private fun SectionHeader(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        style = AppTheme.typography.topBarTitle,
        color = AppTheme.colors.ink,
        modifier = modifier
            .padding(horizontal = Spacing.md, vertical = Spacing.sm)
            .semantics { heading() },
    )
}

@Composable
private fun OfflineBanner(
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.md, vertical = Spacing.xs)
            .background(AppTheme.colors.field, RoundedCornerShape(Radius.field))
            .padding(start = Spacing.md),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        Icon(
            imageVector = AppIcons.CloudOff,
            contentDescription = null,
            tint = AppTheme.colors.muted,
            modifier = Modifier.size(20.dp),
        )
        Text(
            text = stringResource(Res.string.search_offline_banner),
            style = AppTheme.typography.caption,
            color = AppTheme.colors.muted,
            modifier = Modifier.weight(1f),
        )
        TextButton(onClick = onRetryClick) {
            Text(
                text = stringResource(Res.string.retry),
                style = AppTheme.typography.caption,
                color = AppTheme.colors.ink,
            )
        }
    }
}

@Composable
private fun LoadingFooter(modifier: Modifier = Modifier) {
    val label = stringResource(Res.string.loading)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(Spacing.md),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(
            color = AppTheme.colors.muted,
            strokeWidth = 2.dp,
            modifier = Modifier
                .size(24.dp)
                .semantics { contentDescription = label },
        )
    }
}

/** Atualizando com conteúdo na tela: é o indicador do puxar para atualizar, não o esqueleto. */
private fun LazyPagingItems<Song>.isRefreshingWithContent(): Boolean =
    loadState.refresh is LoadState.Loading && itemCount > 0

private fun LoadState.Error.toDataError(): DataError =
    (error as? DataErrorException)?.error ?: DataError.Network.UNKNOWN

private const val PLACEHOLDER_ROWS = 8

@Preview
@Composable
private fun SongsScreenRecentsPreview() {
    AppTheme {
        SongsScreen(
            state = SongsState(
                recentlyPlayed = listOf(previewSong(1), previewSong(2, title = "Instant Crush")),
                isRecentlyPlayedLoaded = true,
                playingSongId = 1,
            ),
            songs = flowOf(PagingData.empty<Song>()).collectAsLazyPagingItems(),
            onAction = {},
        )
    }
}

@Preview
@Composable
private fun SongsScreenEmptyPreview() {
    AppTheme {
        SongsScreen(
            state = SongsState(isRecentlyPlayedLoaded = true),
            songs = flowOf(PagingData.empty<Song>()).collectAsLazyPagingItems(),
            onAction = {},
        )
    }
}

@Preview
@Composable
private fun SongsScreenResultsPreview() {
    AppTheme {
        SongsScreen(
            state = SongsState(query = "daft punk", isRecentlyPlayedLoaded = true),
            songs = flowOf(PagingData.from(List(6) { previewSong(it.toLong(), title = "Song $it") }))
                .collectAsLazyPagingItems(),
            onAction = {},
        )
    }
}
