package com.lucaslima.musicsearch.feature.library.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import com.lucaslima.musicsearch.core.designsystem.icons.AppIcons
import com.lucaslima.musicsearch.core.designsystem.theme.AppTheme
import com.lucaslima.musicsearch.core.designsystem.theme.Radius
import com.lucaslima.musicsearch.core.designsystem.theme.Sizes
import com.lucaslima.musicsearch.core.designsystem.theme.Spacing
import com.lucaslima.musicsearch.feature.library.domain.Song
import com.lucaslima.musicsearch.feature.library.presentation.resources.Res
import com.lucaslima.musicsearch.feature.library.presentation.resources.view_album
import org.jetbrains.compose.resources.stringResource

/** "More options" do Figma: nome da música, artista e a ação de ver o álbum. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SongOptionsSheet(
    song: Song,
    onViewAlbumClick: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        shape = RoundedCornerShape(topStart = Radius.sheet, topEnd = Radius.sheet),
        containerColor = AppTheme.colors.sheet,
        contentColor = AppTheme.colors.ink,
        dragHandle = { BottomSheetDefaults.DragHandle(color = AppTheme.colors.muted) },
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(bottom = Spacing.md),
        ) {
            Text(
                text = song.title,
                style = AppTheme.typography.button,
                color = AppTheme.colors.ink,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.lg),
            )
            Text(
                text = song.artistName,
                style = AppTheme.typography.body,
                color = AppTheme.colors.muted,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.lg),
            )
            Row(
                modifier = Modifier
                    .padding(top = Spacing.md)
                    .fillMaxWidth()
                    .heightIn(min = Sizes.touchTarget)
                    .clickable(role = Role.Button, onClick = onViewAlbumClick)
                    .padding(horizontal = Spacing.lg, vertical = Spacing.md),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.md),
            ) {
                Icon(imageVector = AppIcons.Album, contentDescription = null, tint = AppTheme.colors.ink)
                Text(text = stringResource(Res.string.view_album), style = AppTheme.typography.itemTitle)
            }
        }
    }
}
