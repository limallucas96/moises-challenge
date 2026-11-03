package cmp.skeleton.app.core.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Cores do tema estendido. Leia por [AppTheme.colors]; nunca instancie fora deste módulo.
 * Placeholder do esqueleto: trocar pelos tokens do design do projeto.
 */
@Immutable
data class AppColors(
    /** Fundo da tela. */
    val bg: Color,
    /** Texto primário e fundo do botão principal. */
    val ink: Color,
    /** Conteúdo sobre [ink]. */
    val onInk: Color,
    /** Texto secundário e legendas. */
    val muted: Color,
    /** Destaque: ações e elementos ativos. */
    val accent: Color,
    /** Conteúdo sobre [accent]. */
    val onAccent: Color,
    /** Fundo de card sobre [bg]. */
    val card: Color,
    /** Divisores e bordas finas. */
    val line: Color,
)

private val Accent = Color(0xFF7B5CFF)
private val Ink = Color(0xFF0D0D0D)

internal val LightAppColors = AppColors(
    bg = Color(0xFFF5F5F3),
    ink = Ink,
    onInk = Color.White,
    muted = Color(0xFF6E6E72),
    accent = Accent,
    onAccent = Color.White,
    card = Color.White,
    line = Color(0xFFE4E4E2),
)

internal val DarkAppColors = AppColors(
    bg = Ink,
    ink = Color(0xFFF5F5F3),
    onInk = Ink,
    muted = Color(0xFFA8A8AC),
    accent = Accent,
    onAccent = Color.White,
    card = Color(0xFF1A1A1C),
    line = Color(0xFF2A2A2D),
)

// Exceção à regra de CompositionLocal: liberada no suppressLintsFor do build.gradle.kts raiz.
internal val LocalAppColors = staticCompositionLocalOf { LightAppColors }
