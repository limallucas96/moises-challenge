package com.lucaslima.musicsearch.core.presentation

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource

/**
 * Texto que vem (ou pode vir) de string resource. Valor sempre dinâmico
 * (nome do usuário, data formatada) é String, não UiText.
 */
sealed interface UiText {
    data class DynamicString(val value: String) : UiText

    class Resource(
        val id: StringResource,
        val args: Array<Any> = emptyArray(),
    ) : UiText

    @Composable
    fun asString(): String = when (this) {
        is DynamicString -> value
        is Resource -> stringResource(id, *args)
    }
}

/** Para fora de composição (ex.: mensagem de snackbar disparada por um Event). */
suspend fun UiText.asStringSuspend(): String = when (this) {
    is UiText.DynamicString -> value
    is UiText.Resource -> getString(id, *args)
}
