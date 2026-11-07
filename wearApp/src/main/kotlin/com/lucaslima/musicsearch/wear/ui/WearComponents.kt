package com.lucaslima.musicsearch.wear.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import coil3.compose.AsyncImage
import com.lucaslima.musicsearch.core.designsystem.icons.AppIcons
import com.lucaslima.musicsearch.feature.library.domain.Song
import com.lucaslima.musicsearch.wear.R

/** Capa redonda sobre a nota musical: sem rede ou sem capa, a nota fica. */
@Composable
internal fun Artwork(
    url: String?,
    modifier: Modifier = Modifier,
    size: Dp = ButtonDefaults.IconSize,
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = AppIcons.MusicNote,
            contentDescription = null,
            modifier = Modifier.size(size / 2),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        AsyncImage(
            model = url,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.size(size),
        )
    }
}

/** Uma música da lista. A que está tocando ganha o destaque de cor e o aviso para o leitor de tela. */
@Composable
internal fun SongButton(
    song: Song,
    isPlaying: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val playing = stringResource(R.string.playing)
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .semantics { if (isPlaying) stateDescription = playing },
        colors = if (isPlaying) {
            ButtonDefaults.filledTonalButtonColors()
        } else {
            ButtonDefaults.filledTonalButtonColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
            )
        },
        icon = { Artwork(url = song.artworkUrl) },
        label = { Text(song.title, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        secondaryLabel = { Text(song.artistName, maxLines = 1, overflow = TextOverflow.Ellipsis) },
    )
}

/** Texto centralizado para estados vazios e erros. */
@Composable
internal fun Message(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        textAlign = TextAlign.Center,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}
