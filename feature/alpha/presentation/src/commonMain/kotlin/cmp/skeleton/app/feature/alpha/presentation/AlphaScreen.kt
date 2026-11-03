package cmp.skeleton.app.feature.alpha.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cmp.skeleton.app.core.designsystem.theme.AppTheme
import cmp.skeleton.app.core.designsystem.theme.Spacing
import cmp.skeleton.app.core.presentation.UiText
import cmp.skeleton.app.feature.alpha.presentation.resources.Res
import cmp.skeleton.app.feature.alpha.presentation.resources.alpha_beta_message
import cmp.skeleton.app.feature.alpha.presentation.resources.alpha_loading
import cmp.skeleton.app.feature.alpha.presentation.resources.alpha_open_beta
import cmp.skeleton.app.feature.alpha.presentation.resources.alpha_refresh
import cmp.skeleton.app.feature.alpha.presentation.resources.alpha_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun AlphaRoot(
    onOpenBetaClick: (message: String) -> Unit,
    viewModel: AlphaViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    AlphaScreen(
        state = state,
        onAction = viewModel::onAction,
        onOpenBetaClick = onOpenBetaClick,
    )
}

@Composable
internal fun AlphaScreen(
    state: AlphaState,
    onAction: (AlphaAction) -> Unit,
    onOpenBetaClick: (message: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val betaMessage = stringResource(Res.string.alpha_beta_message, state.greeting.orEmpty())
    Column(
        modifier = modifier
            .fillMaxSize()
            .systemBarsPadding()
            .padding(Spacing.lg),
        verticalArrangement = Arrangement.spacedBy(Spacing.md, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(Res.string.alpha_title),
            style = AppTheme.typography.screenTitle,
            color = AppTheme.colors.ink,
        )
        Text(
            text = when {
                state.isLoading -> stringResource(Res.string.alpha_loading)
                state.error != null -> state.error.asString()
                else -> state.greeting.orEmpty()
            },
            style = AppTheme.typography.body,
            color = AppTheme.colors.muted,
        )
        OutlinedButton(
            onClick = { onAction(AlphaAction.OnRefreshClick) },
            enabled = !state.isLoading,
        ) {
            Text(text = stringResource(Res.string.alpha_refresh), style = AppTheme.typography.button)
        }
        Button(
            onClick = { onOpenBetaClick(betaMessage) },
            enabled = !state.isLoading && state.greeting != null,
        ) {
            Text(text = stringResource(Res.string.alpha_open_beta), style = AppTheme.typography.button)
        }
    }
}

@Preview
@Composable
private fun AlphaScreenPreview() {
    AppTheme {
        AlphaScreen(
            state = AlphaState(isLoading = false, greeting = "Hello from the data layer (#1)"),
            onAction = {},
            onOpenBetaClick = {},
        )
    }
}

@Preview
@Composable
private fun AlphaScreenErrorPreview() {
    AppTheme {
        AlphaScreen(
            state = AlphaState(isLoading = false, error = UiText.DynamicString("Something went wrong.")),
            onAction = {},
            onOpenBetaClick = {},
        )
    }
}
