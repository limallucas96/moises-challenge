package com.lucaslima.musicsearch.feature.library.presentation.songs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lucaslima.musicsearch.core.designsystem.theme.AppTheme
import com.lucaslima.musicsearch.core.designsystem.theme.Spacing
import com.lucaslima.musicsearch.feature.library.presentation.resources.Res
import com.lucaslima.musicsearch.feature.library.presentation.resources.songs_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun SongsRoot(viewModel: SongsViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    SongsScreen(state = state, onAction = viewModel::onAction)
}

@Composable
internal fun SongsScreen(
    state: SongsState,
    onAction: (SongsAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .systemBarsPadding()
            .padding(Spacing.md),
    ) {
        Text(
            text = stringResource(Res.string.songs_title),
            style = AppTheme.typography.screenTitle,
            color = AppTheme.colors.ink,
        )
    }
}

@Preview
@Composable
private fun SongsScreenPreview() {
    AppTheme {
        SongsScreen(state = SongsState(), onAction = {})
    }
}
