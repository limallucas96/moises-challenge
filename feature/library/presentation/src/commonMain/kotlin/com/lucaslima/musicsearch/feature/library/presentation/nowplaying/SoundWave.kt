package com.lucaslima.musicsearch.feature.library.presentation.nowplaying

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.lucaslima.musicsearch.core.designsystem.theme.AppTheme
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

/**
 * Onda sonora animada, no estilo de um equalizador: barras mais altas no meio, que se mexem
 * enquanto a música toca e baixam quando pausa. As cores percorrem [colors] devagar, então a onda
 * muda de tom ao longo da música. Decorativa: não lê o áudio de verdade, que no Android pediria
 * permissão de microfone.
 */
@Composable
internal fun SoundWave(
    isPlaying: Boolean,
    colors: List<Color>,
    modifier: Modifier = Modifier,
) {
    var time by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(isPlaying) {
        if (!isPlaying) return@LaunchedEffect
        var last = withFrameNanos { it }
        while (true) {
            withFrameNanos { now ->
                time += (now - last) / NANOS_PER_SECOND
                last = now
            }
        }
    }
    val energy by animateFloatAsState(
        targetValue = if (isPlaying) 1f else PAUSED_ENERGY,
        animationSpec = tween(ENERGY_MILLIS),
        label = "energy",
    )

    // O tempo só é lido no desenho: a animação redesenha sem recompor.
    Canvas(modifier = modifier) {
        val slot = size.width / BAR_COUNT
        val stroke = slot * BAR_FILL
        val minHeight = stroke
        for (i in 0 until BAR_COUNT) {
            val x = i / (BAR_COUNT - 1f)
            val level = barLevel(index = i, position = x, time = time) * energy
            val height = (size.height * level).coerceAtLeast(minHeight)
            val centerX = slot * (i + 0.5f)
            val top = (size.height - height) / 2f
            drawLine(
                color = colorAt(colors, x + time * COLOR_SPEED),
                start = Offset(centerX, top),
                end = Offset(centerX, top + height),
                strokeWidth = stroke,
                cap = StrokeCap.Round,
            )
        }
    }
}

/** Altura de 0 a 1 de uma barra: um envelope em sino vezes duas senoides com fase própria. */
private fun barLevel(
    index: Int,
    position: Float,
    time: Float,
): Float {
    val distance = position - 0.5f
    val envelope = ENVELOPE_FLOOR + (1f - ENVELOPE_FLOOR) * exp(-(distance * distance) / ENVELOPE_WIDTH)
    // Fases "aleatórias" mas fixas por barra, para cada uma ter o seu ritmo.
    val phase = index * GOLDEN_ANGLE
    val speed = BASE_SPEED + (index * 7 % 5) * SPEED_STEP
    val wave = 0.55f + 0.3f * sin(time * speed + phase) + 0.15f * sin(time * speed * 2.3f + phase * 1.7f)
    return (envelope * wave).coerceIn(0f, 1f)
}

/** Cor num ponto de [colors] tratado como um círculo: o fim emenda no começo. */
private fun colorAt(
    colors: List<Color>,
    position: Float,
): Color {
    if (colors.size == 1) return colors[0]
    val scaled = (position.mod(1f)) * colors.size
    val index = scaled.toInt() % colors.size
    return lerp(colors[index], colors[(index + 1) % colors.size], scaled - scaled.toInt())
}

private const val BAR_COUNT = 40
private const val BAR_FILL = 0.45f
private const val PAUSED_ENERGY = 0.18f
private const val ENERGY_MILLIS = 400
private const val NANOS_PER_SECOND = 1_000_000_000f
private const val ENVELOPE_FLOOR = 0.15f
private const val ENVELOPE_WIDTH = 0.06f
private const val BASE_SPEED = 5f
private const val SPEED_STEP = 1.3f
private const val GOLDEN_ANGLE = (PI * (3 - 2.2360679775)).toFloat()

/** Voltas da paleta por segundo: uma volta a cada 12 s. */
private const val COLOR_SPEED = 1f / 12f

@Preview
@Composable
private fun SoundWavePreview() {
    AppTheme {
        SoundWave(isPlaying = false, colors = DefaultWavePalette, modifier = Modifier.size(320.dp, 160.dp))
    }
}
