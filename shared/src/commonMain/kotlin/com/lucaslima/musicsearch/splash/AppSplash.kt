package com.lucaslima.musicsearch.splash

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.lucaslima.musicsearch.core.designsystem.icons.AppIcons
import com.lucaslima.musicsearch.core.designsystem.theme.AppTheme
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.delay

/**
 * Splash do Figma por cima da primeira tela. A splash nativa (Android e iOS) começa na mesma
 * cor; esta mostra o degradê completo e some com um fade, sem atrasar o app: a Home já está
 * montada por baixo. Só aparece na abertura, não ao girar a tela.
 */
@Composable
internal fun AppSplash(modifier: Modifier = Modifier) {
    var isVisible by rememberSaveable { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        delay(SPLASH_DURATION)
        isVisible = false
    }
    AnimatedVisibility(
        visible = isVisible,
        enter = EnterTransition.None,
        exit = fadeOut(tween(FADE_MILLIS)),
        modifier = modifier,
    ) {
        SplashContent()
    }
}

@Composable
private fun SplashContent(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(AppTheme.colors.splashTop, Color.Black)))
            // Decorativa: o leitor de tela já anuncia a Home que está por baixo.
            .clearAndSetSemantics { },
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = AppIcons.MusicNote,
            contentDescription = null,
            tint = SPLASH_ICON,
            modifier = Modifier.size(96.dp),
        )
    }
}

private val SPLASH_DURATION = 600.milliseconds
private const val FADE_MILLIS = 300
private val SPLASH_ICON = Color(0xFFC4C4C4)

@Preview
@Composable
private fun SplashPreview() {
    AppTheme { SplashContent() }
}
