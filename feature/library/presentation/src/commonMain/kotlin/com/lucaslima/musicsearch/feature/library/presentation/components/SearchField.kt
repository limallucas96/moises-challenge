package com.lucaslima.musicsearch.feature.library.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.lucaslima.musicsearch.core.designsystem.icons.AppIcons
import com.lucaslima.musicsearch.core.designsystem.theme.AppTheme
import com.lucaslima.musicsearch.core.designsystem.theme.Radius
import com.lucaslima.musicsearch.core.designsystem.theme.Sizes
import com.lucaslima.musicsearch.core.designsystem.theme.Spacing
import com.lucaslima.musicsearch.feature.library.presentation.resources.Res
import com.lucaslima.musicsearch.feature.library.presentation.resources.search_clear
import com.lucaslima.musicsearch.feature.library.presentation.resources.search_placeholder
import org.jetbrains.compose.resources.stringResource

/** Campo de busca do Figma: fundo de campo, lupa à esquerda e botão de limpar quando há texto. */
@Composable
internal fun SearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    onClearClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current
    val placeholder = stringResource(Res.string.search_placeholder)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = Sizes.touchTarget)
            .background(AppTheme.colors.field, RoundedCornerShape(Radius.field))
            .padding(start = Spacing.md),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        Icon(
            imageVector = AppIcons.Search,
            contentDescription = null,
            tint = AppTheme.colors.muted,
            modifier = Modifier.size(20.dp),
        )
        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
            if (query.isEmpty()) {
                Text(text = placeholder, style = AppTheme.typography.body, color = AppTheme.colors.muted)
            }
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                singleLine = true,
                textStyle = AppTheme.typography.body.copy(color = AppTheme.colors.ink),
                cursorBrush = SolidColor(AppTheme.colors.ink),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { contentDescription = placeholder },
            )
        }
        if (query.isNotEmpty()) {
            IconButton(onClick = onClearClick) {
                Icon(
                    imageVector = AppIcons.Close,
                    contentDescription = stringResource(Res.string.search_clear),
                    tint = AppTheme.colors.muted,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}
