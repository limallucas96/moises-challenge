package cmp.skeleton.app.feature.beta.presentation.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import cmp.skeleton.app.core.presentation.navigation.NavKeyRegistration
import cmp.skeleton.app.core.presentation.navigation.navKey
import cmp.skeleton.app.feature.beta.presentation.BetaRoot
import kotlinx.serialization.Serializable

/** Key de entrada da feature, com argumento: [message] é o texto que a tela mostra. */
@Serializable
data class BetaKey(val message: String) : NavKey

fun EntryProviderScope<NavKey>.betaEntries(onBackClick: () -> Unit) {
    entry<BetaKey> { key -> BetaRoot(message = key.message, onBackClick = onBackClick) }
}

/** TODAS as NavKey da feature. Key com argumentos recebe uma instância de exemplo. */
val betaNavKeys: List<NavKeyRegistration<*>> = listOf(
    navKey(BetaKey(message = "sample")),
)
