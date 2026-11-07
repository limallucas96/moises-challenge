package com.lucaslima.musicsearch.feature.library.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import coil3.compose.AsyncImage
import com.lucaslima.musicsearch.core.designsystem.icons.AppIcons
import com.lucaslima.musicsearch.core.designsystem.theme.AppTheme

/**
 * Capa de álbum. Enquanto carrega, ou sem URL, mostra a nota musical sobre o fundo de campo.
 * Decorativa: o título ao lado já diz o que é, então não tem contentDescription.
 */
@Composable
internal fun Artwork(
    url: String?,
    cornerRadius: Dp,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(AppTheme.colors.field),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = AppIcons.MusicNote,
            contentDescription = null,
            tint = AppTheme.colors.line,
            modifier = Modifier.fillMaxSize(PLACEHOLDER_ICON_FRACTION),
        )
        if (url != null) {
            AsyncImage(
                model = url,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

/** Capa quadrada de tamanho fixo. */
@Composable
internal fun Artwork(
    url: String?,
    size: Dp,
    cornerRadius: Dp,
    modifier: Modifier = Modifier,
) = Artwork(url = url, cornerRadius = cornerRadius, modifier = modifier.size(size))

private const val PLACEHOLDER_ICON_FRACTION = 0.45f
