package cmp.skeleton.app.feature.alpha.presentation.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import cmp.skeleton.app.core.presentation.navigation.NavKeyRegistration
import cmp.skeleton.app.core.presentation.navigation.navKey
import cmp.skeleton.app.feature.alpha.presentation.AlphaRoot
import kotlinx.serialization.Serializable

/** Key de entrada da feature. Pública porque o :shared precisa dela. */
@Serializable
data object AlphaKey : NavKey

fun EntryProviderScope<NavKey>.alphaEntries(onOpenBetaClick: (message: String) -> Unit) {
    entry<AlphaKey> { AlphaRoot(onOpenBetaClick = onOpenBetaClick) }
}

/**
 * TODAS as NavKey da feature. O :shared registra esta lista no SavedStateConfiguration e o
 * NavKeyRegistrationTest faz o round-trip de cada uma. Key nova na feature = uma linha nova aqui.
 */
val alphaNavKeys: List<NavKeyRegistration<*>> = listOf(
    navKey(AlphaKey),
)
