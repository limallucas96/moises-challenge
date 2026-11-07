package com.lucaslima.musicsearch.core.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Cores do tema estendido. Leia por [AppTheme.colors]; nunca instancie fora deste módulo.
 * Valores medidos no Figma do desafio, que só tem tema escuro.
 */
@Immutable
data class AppColors(
    /** Fundo da tela. */
    val bg: Color,
    /** Texto primário, ícones e elementos ativos (thumb e trilha tocada do slider). */
    val ink: Color,
    /** Conteúdo sobre [ink]. */
    val onInk: Color,
    /** Texto secundário: artista, tempo, legendas. */
    val muted: Color,
    /** Campo de busca e placeholders de capa. */
    val field: Color,
    /** Fundo do bottom sheet. */
    val sheet: Color,
    /** Botão de play circular. */
    val control: Color,
    /** Trilha do slider ainda não tocada, divisores e o puxador do sheet. */
    val line: Color,
    /** Degradê da splash: do canto superior até o [bg]. */
    val splashTop: Color,
)

internal val DarkAppColors = AppColors(
    bg = Color(0xFF000000),
    ink = Color(0xFFFFFFFF),
    onInk = Color(0xFF000000),
    muted = Color(0xFF8A8A8A),
    field = Color(0xFF1A1A1A),
    sheet = Color(0xFF2C2C2C),
    control = Color(0xFF333333),
    line = Color(0xFF3F3F3F),
    splashTop = Color(0xFF0F3440),
)

// Exceção à regra de CompositionLocal: liberada no suppressLintsFor do build.gradle.kts raiz.
internal val LocalAppColors = staticCompositionLocalOf { DarkAppColors }
