package cmp.skeleton.app.feature.beta.presentation

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
import cmp.skeleton.app.feature.beta.presentation.resources.Res
import cmp.skeleton.app.feature.beta.presentation.resources.beta_back
import cmp.skeleton.app.feature.beta.presentation.resources.beta_tap
import cmp.skeleton.app.feature.beta.presentation.resources.beta_taps
import cmp.skeleton.app.feature.beta.presentation.resources.beta_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
internal fun BetaRoot(
    message: String,
    onBackClick: () -> Unit,
    viewModel: BetaViewModel = koinViewModel { parametersOf(message) },
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    BetaScreen(
        state = state,
        onAction = viewModel::onAction,
        onBackClick = onBackClick,
    )
}

@Composable
internal fun BetaScreen(
    state: BetaState,
    onAction: (BetaAction) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .systemBarsPadding()
            .padding(Spacing.lg),
        verticalArrangement = Arrangement.spacedBy(Spacing.md, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(Res.string.beta_title),
            style = AppTheme.typography.screenTitle,
            color = AppTheme.colors.ink,
        )
        Text(
            text = state.message,
            style = AppTheme.typography.body,
            color = AppTheme.colors.muted,
        )
        Text(
            text = stringResource(Res.string.beta_taps, state.tapCount),
            style = AppTheme.typography.caption,
            color = AppTheme.colors.muted,
        )
        Button(onClick = { onAction(BetaAction.OnTapClick) }) {
            Text(text = stringResource(Res.string.beta_tap), style = AppTheme.typography.button)
        }
        OutlinedButton(onClick = onBackClick) {
            Text(text = stringResource(Res.string.beta_back), style = AppTheme.typography.button)
        }
    }
}

@Preview
@Composable
private fun BetaScreenPreview() {
    AppTheme {
        BetaScreen(
            state = BetaState(message = "Sent from Alpha: Hello", tapCount = 3),
            onAction = {},
            onBackClick = {},
        )
    }
}
