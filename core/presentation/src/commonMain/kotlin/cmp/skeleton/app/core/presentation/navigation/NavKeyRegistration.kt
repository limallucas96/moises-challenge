package cmp.skeleton.app.core.presentation.navigation

import androidx.navigation3.runtime.NavKey
import kotlin.reflect.KClass
import kotlinx.serialization.KSerializer
import kotlinx.serialization.modules.PolymorphicModuleBuilder
import kotlinx.serialization.serializer

/**
 * Uma NavKey da feature com o que o :shared precisa para registrá-la no
 * SavedStateConfiguration e testar o round-trip.
 *
 * [sample] é uma instância válida da key: para `data object` é o próprio
 * objeto; para `data class` com argumentos, qualquer valor representativo.
 */
class NavKeyRegistration<T : NavKey>(
    val type: KClass<T>,
    val serializer: KSerializer<T>,
    val sample: T,
) {
    fun registerIn(builder: PolymorphicModuleBuilder<NavKey>) {
        builder.subclass(type, serializer)
    }
}

inline fun <reified T : NavKey> navKey(sample: T): NavKeyRegistration<T> =
    NavKeyRegistration(T::class, serializer<T>(), sample)

fun PolymorphicModuleBuilder<NavKey>.register(keys: List<NavKeyRegistration<*>>) {
    keys.forEach { it.registerIn(this) }
}
