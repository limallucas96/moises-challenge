package cmp.skeleton.app.feature.alpha.presentation

import cmp.skeleton.app.core.presentation.UiText

internal data class AlphaState(
    val isLoading: Boolean = true,
    val greeting: String? = null,
    val error: UiText? = null,
)

internal sealed interface AlphaAction {
    data object OnRefreshClick : AlphaAction
}
