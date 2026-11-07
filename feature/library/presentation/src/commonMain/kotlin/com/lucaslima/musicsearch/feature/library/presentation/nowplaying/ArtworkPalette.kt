package com.lucaslima.musicsearch.feature.library.presentation.nowplaying

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import coil3.SingletonImageLoader
import coil3.compose.LocalPlatformContext
import coil3.compose.asPainter
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import coil3.size.Size as CoilSize
import kotlin.math.max
import kotlin.math.min

/** Cores usadas quando a capa não carrega ou é quase toda cinza (verde do equalizador clássico). */
internal val DefaultWavePalette = listOf(Color(0xFF7CFC00), Color(0xFF00E5A0), Color(0xFF00C8FF))

/**
 * As cores vivas da capa, para a onda sonora. Começa com [DefaultWavePalette] e troca quando a
 * capa termina de carregar (vem do cache do Coil, que a tela já baixou).
 */
@Composable
internal fun rememberArtworkPalette(url: String?): List<Color> {
    val context = LocalPlatformContext.current
    var palette by remember(url) { mutableStateOf(DefaultWavePalette) }
    LaunchedEffect(url) {
        if (url == null) return@LaunchedEffect
        val request = ImageRequest.Builder(context)
            .data(url)
            .size(CoilSize(SAMPLE_SIZE, SAMPLE_SIZE))
            .allowPixelReads()
            .build()
        val image = (SingletonImageLoader.get(context).execute(request) as? SuccessResult)?.image
            ?: return@LaunchedEffect
        val bitmap = ImageBitmap(SAMPLE_SIZE, SAMPLE_SIZE)
        val drawSize = Size(SAMPLE_SIZE.toFloat(), SAMPLE_SIZE.toFloat())
        CanvasDrawScope().draw(Density(1f), LayoutDirection.Ltr, Canvas(bitmap), drawSize) {
            with(image.asPainter(context)) { draw(drawSize) }
        }
        val pixels = IntArray(SAMPLE_SIZE * SAMPLE_SIZE)
        bitmap.readPixels(pixels)
        palette = wavePaletteFrom(pixels)
    }
    return palette
}

/**
 * Até [maxColors] cores vivas e de matizes diferentes, das mais presentes para as menos. Pixels
 * cinzentos ou escuros não contam. Sem cor suficiente, devolve [DefaultWavePalette].
 */
internal fun wavePaletteFrom(
    argb: IntArray,
    maxColors: Int = 3,
): List<Color> {
    val weights = FloatArray(HUE_BUCKETS)
    val hueSums = FloatArray(HUE_BUCKETS)
    for (pixel in argb) {
        val hsv = hsv(pixel)
        if (hsv.saturation < MIN_SATURATION || hsv.value < MIN_VALUE) continue
        val bucket = (hsv.hue / 360f * HUE_BUCKETS).toInt().coerceIn(0, HUE_BUCKETS - 1)
        val weight = hsv.saturation * hsv.value
        weights[bucket] += weight
        hueSums[bucket] += hsv.hue * weight
    }
    val minWeight = argb.size * MIN_SHARE
    val colors = weights.indices
        .filter { weights[it] >= minWeight }
        .sortedByDescending { weights[it] }
        .take(maxColors)
        // Clareia e satura: a onda fica sobre fundo preto e precisa brilhar.
        .map { Color.hsv(hueSums[it] / weights[it], VIVID_SATURATION, VIVID_VALUE) }
    return when (colors.size) {
        0 -> DefaultWavePalette

        // Uma cor só vira um degradê com a vizinha, para a onda não ficar chapada.
        1 -> listOf(colors[0], shiftHue(colors[0], NEIGHBOR_HUE))

        else -> colors
    }
}

private data class Hsv(
    val hue: Float,
    val saturation: Float,
    val value: Float,
)

private fun hsv(argb: Int): Hsv {
    val r = (argb shr 16 and 0xFF) / 255f
    val g = (argb shr 8 and 0xFF) / 255f
    val b = (argb and 0xFF) / 255f
    val maxC = max(r, max(g, b))
    val delta = maxC - min(r, min(g, b))
    val hue = when {
        delta == 0f -> 0f
        maxC == r -> 60f * (((g - b) / delta).mod(6f))
        maxC == g -> 60f * ((b - r) / delta + 2f)
        else -> 60f * ((r - g) / delta + 4f)
    }
    return Hsv(hue = hue, saturation = if (maxC == 0f) 0f else delta / maxC, value = maxC)
}

private fun shiftHue(
    color: Color,
    degrees: Float,
): Color {
    val argb = (color.alpha * 255).toInt() shl 24 or
        ((color.red * 255).toInt() shl 16) or
        ((color.green * 255).toInt() shl 8) or
        (color.blue * 255).toInt()
    val hsv = hsv(argb)
    return Color.hsv((hsv.hue + degrees).mod(360f), VIVID_SATURATION, VIVID_VALUE)
}

/** No Android a capa precisa vir num bitmap comum: o de hardware não deixa ler pixel. */
internal expect fun ImageRequest.Builder.allowPixelReads(): ImageRequest.Builder

private const val SAMPLE_SIZE = 32
private const val HUE_BUCKETS = 12
private const val MIN_SATURATION = 0.3f
private const val MIN_VALUE = 0.25f

/** Fatia mínima (em peso) da imagem para uma cor contar: 3%. */
private const val MIN_SHARE = 0.03f
private const val VIVID_SATURATION = 0.75f
private const val VIVID_VALUE = 1f
private const val NEIGHBOR_HUE = 40f
