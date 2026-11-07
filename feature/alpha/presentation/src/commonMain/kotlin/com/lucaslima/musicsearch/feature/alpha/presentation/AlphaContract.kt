package com.lucaslima.musicsearch.feature.alpha.presentation

import com.lucaslima.musicsearch.core.presentation.UiText

internal data class AlphaState(
    val isLoading: Boolean = true,
    val greeting: String? = null,
    val error: UiText? = null,
)

internal sealed interface AlphaAction {
    data object OnRefreshClick : AlphaAction
}
