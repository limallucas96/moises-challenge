package com.lucaslima.musicsearch.core.designsystem.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember

/**
 * Único ponto de verdade visual. Nenhuma feature instancia MaterialTheme
 * nem declara Color(0xFF...) fora daqui.
 *
 * O design só tem tema escuro, então o app não segue o tema do sistema. O Material recebe as
 * mesmas cores para que os componentes dele (slider, sheet, ripple) combinem com o tema estendido.
 */
@Composable
fun AppTheme(content: @Composable () -> Unit) {
    val typography = remember { appTypography() }
    val colors = DarkAppColors
    CompositionLocalProvider(
        LocalAppColors provides colors,
        LocalAppTypography provides typography,
    ) {
        MaterialTheme(
            colorScheme = darkColorScheme(
                primary = colors.ink,
                onPrimary = colors.onInk,
                background = colors.bg,
                onBackground = colors.ink,
                surface = colors.bg,
                onSurface = colors.ink,
                onSurfaceVariant = colors.muted,
                surfaceContainerLow = colors.sheet,
                surfaceContainerHighest = colors.field,
                outline = colors.line,
                outlineVariant = colors.line,
            ),
            content = content,
        )
    }
}

/** Tokens do tema estendido. */
object AppTheme {
    val colors: AppColors
        @Composable
        @ReadOnlyComposable
        get() = LocalAppColors.current

    val typography: AppTypography
        @Composable
        @ReadOnlyComposable
        get() = LocalAppTypography.current
}
