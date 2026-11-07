package com.lucaslima.musicsearch.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.navigation3.scene.Scene

/**
 * Troca de tela: um fade curto. O padrão do Navigation 3 leva 700 ms, o que deixa a capa
 * compartilhada lenta e a navegação com cara de travada. A tela que sai some mais rápido do que a
 * nova aparece, para as duas não ficarem sobrepostas.
 */
internal fun <T : Any> AnimatedContentTransitionScope<Scene<T>>.screenTransition(): ContentTransform =
    fadeIn(tween(NAV_ENTER_MILLIS, easing = FastOutSlowInEasing)) togetherWith
        fadeOut(tween(NAV_EXIT_MILLIS, easing = FastOutSlowInEasing))

/** Duração da tela que entra; a capa compartilhada usa a mesma. */
internal const val NAV_ENTER_MILLIS = 300
private const val NAV_EXIT_MILLIS = 200
