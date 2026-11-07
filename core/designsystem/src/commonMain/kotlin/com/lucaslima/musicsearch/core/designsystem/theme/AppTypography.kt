package com.lucaslima.musicsearch.core.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Estilos de texto do tema estendido. Leia por [AppTheme.typography].
 * Fonte do sistema (Roboto no Android, SF no iOS), próxima da usada no Figma.
 */
@Immutable
data class AppTypography(
    /** Título de tela ("Songs", título do álbum): 22, 600. */
    val screenTitle: TextStyle,
    /** Título da barra superior ("Now playing"): 16, 500. */
    val topBarTitle: TextStyle,
    /** Título da música no player: 28, 600. */
    val playerTitle: TextStyle,
    /** Linha principal de um item de lista: 16, 400. */
    val itemTitle: TextStyle,
    /** Linha secundária de um item de lista: 12, 400. */
    val itemSubtitle: TextStyle,
    /** Texto corrido e artista no player: 14/20, 400. */
    val body: TextStyle,
    /** Texto de botão e ação do sheet: 16, 500. */
    val button: TextStyle,
    /** Legenda e tempo do slider: 12, 400. */
    val caption: TextStyle,
)

internal fun appTypography(fontFamily: FontFamily = FontFamily.Default): AppTypography {
    fun style(
        size: Int,
        weight: FontWeight,
    ) = TextStyle(fontFamily = fontFamily, fontSize = size.sp, fontWeight = weight)
    return AppTypography(
        screenTitle = style(22, FontWeight.SemiBold),
        topBarTitle = style(16, FontWeight.Medium),
        playerTitle = style(28, FontWeight.SemiBold),
        itemTitle = style(16, FontWeight.Normal),
        itemSubtitle = style(12, FontWeight.Normal),
        body = style(14, FontWeight.Normal).copy(lineHeight = 20.sp),
        button = style(16, FontWeight.Medium),
        caption = style(12, FontWeight.Normal),
    )
}

// Exceção à regra de CompositionLocal: liberada no suppressLintsFor do build.gradle.kts raiz.
internal val LocalAppTypography = staticCompositionLocalOf { appTypography() }
