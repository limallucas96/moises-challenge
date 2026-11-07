package com.lucaslima.musicsearch.core.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Estilos de texto do tema estendido. Leia por [AppTheme.typography].
 * Placeholder do esqueleto: usa a fonte do sistema até a fonte do projeto entrar.
 */
@Immutable
data class AppTypography(
    /** Título de tela: 32, 800. */
    val screenTitle: TextStyle,
    /** Texto corrido: 16/24, 400. */
    val body: TextStyle,
    /** Texto de botão: 16, 700. */
    val button: TextStyle,
    /** Legenda e texto secundário: 13, 500. */
    val caption: TextStyle,
)

internal fun appTypography(fontFamily: FontFamily = FontFamily.Default): AppTypography {
    fun style(
        size: Int,
        weight: FontWeight,
    ) = TextStyle(fontFamily = fontFamily, fontSize = size.sp, fontWeight = weight)
    return AppTypography(
        screenTitle = style(32, FontWeight.ExtraBold).copy(letterSpacing = (-1.2).sp),
        body = style(16, FontWeight.Normal).copy(lineHeight = 24.sp),
        button = style(16, FontWeight.Bold),
        caption = style(13, FontWeight.Medium),
    )
}

// Exceção à regra de CompositionLocal: liberada no suppressLintsFor do build.gradle.kts raiz.
internal val LocalAppTypography = staticCompositionLocalOf { appTypography() }
