package com.lucaslima.musicsearch.feature.library.presentation.components

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation3.ui.LocalNavAnimatedContentScope

/**
 * Os dois escopos da transição da capa: o do SharedTransitionLayout em volta do NavDisplay e o da
 * animação da tela. Desce por parâmetro de cada tela até a capa; nulo em previews e testes.
 */
@Stable
internal class ArtworkTransition(
    val sharedScope: SharedTransitionScope,
    val animatedScope: AnimatedVisibilityScope,
)

/** Monta a transição dentro de uma entrada do NavDisplay. Sem [sharedScope], não há transição. */
@Composable
internal fun rememberArtworkTransition(sharedScope: SharedTransitionScope?): ArtworkTransition? {
    if (sharedScope == null) return null
    val animatedScope = LocalNavAnimatedContentScope.current
    return remember(sharedScope, animatedScope) { ArtworkTransition(sharedScope, animatedScope) }
}

/** Liga a capa da música entre telas: a da linha da lista cresce até a do player e volta. */
@Composable
internal fun Modifier.sharedArtwork(
    transition: ArtworkTransition?,
    songId: Long,
): Modifier {
    if (transition == null) return this
    return with(transition.sharedScope) {
        sharedElement(
            sharedContentState = rememberSharedContentState(key = "artwork-$songId"),
            animatedVisibilityScope = transition.animatedScope,
        )
    }
}
