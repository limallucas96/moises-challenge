package cmp.skeleton.app.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF7B5CFF),
    onPrimary = Color.White,
    background = Color(0xFFF5F5F3),
    surface = Color.White,
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFA796FF),
    onPrimary = Color(0xFF1B0F5C),
    background = Color(0xFF0D0D0D),
    surface = Color(0xFF1A1A1C),
)

/**
 * Único ponto de verdade visual. Nenhuma feature instancia MaterialTheme
 * nem declara Color(0xFF...) fora daqui.
 */
@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val typography = remember { appTypography() }
    CompositionLocalProvider(
        LocalAppColors provides if (darkTheme) DarkAppColors else LightAppColors,
        LocalAppTypography provides typography,
    ) {
        MaterialTheme(
            colorScheme = if (darkTheme) DarkColors else LightColors,
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
