package com.lucaslima.musicsearch.feature.library.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.lucaslima.musicsearch.core.designsystem.theme.AppTheme
import com.lucaslima.musicsearch.core.designsystem.theme.Spacing

/** Estado vazio ou de erro, centralizado: ícone, título, texto e, se houver, uma ação. */
@Composable
internal fun StateMessage(
    icon: ImageVector,
    title: String,
    body: String?,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onActionClick: () -> Unit = {},
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.xl, vertical = Spacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = AppTheme.colors.muted,
            modifier = Modifier.size(48.dp),
        )
        // Título e texto são lidos juntos, e o leitor de tela anuncia quando o estado aparece.
        Column(
            modifier = Modifier.semantics(mergeDescendants = true) { liveRegion = LiveRegionMode.Polite },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            Text(
                text = title,
                style = AppTheme.typography.button,
                color = AppTheme.colors.ink,
                textAlign = TextAlign.Center,
            )
            if (body != null) {
                Text(
                    text = body,
                    style = AppTheme.typography.body,
                    color = AppTheme.colors.muted,
                    textAlign = TextAlign.Center,
                )
            }
        }
        if (actionLabel != null) {
            OutlinedButton(onClick = onActionClick, modifier = Modifier.padding(top = Spacing.sm)) {
                Text(text = actionLabel, style = AppTheme.typography.button, color = AppTheme.colors.ink)
            }
        }
    }
}
