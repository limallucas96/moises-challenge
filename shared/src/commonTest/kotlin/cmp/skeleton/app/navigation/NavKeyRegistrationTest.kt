package cmp.skeleton.app.navigation

import androidx.navigation3.runtime.NavKey
import assertk.assertFailure
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import kotlin.test.Test
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

/**
 * Garante que toda NavKey do app está registrada para restaurar a pilha. Usa JSON com o mesmo
 * SerializersModule do navConfig: o que se verifica é o registro polimórfico, que é igual em
 * qualquer formato.
 */
class NavKeyRegistrationTest {

    private val json = Json { serializersModule = navSerializersModule }
    private val navKeySerializer = PolymorphicSerializer(NavKey::class)

    @Test
    fun cada_navkey_registrada_sobrevive_ao_round_trip() {
        allNavKeys.forEach { registration ->
            val encoded = json.encodeToString(navKeySerializer, registration.sample)
            val decoded = json.decodeFromString(navKeySerializer, encoded)

            assertThat(decoded, name = registration.type.simpleName)
                .isEqualTo(registration.sample)
        }
    }

    @Test
    fun pilha_com_todas_as_keys_sobrevive_ao_round_trip() {
        val backStack = allNavKeys.map { it.sample }
        val serializer = ListSerializer(navKeySerializer)

        val decoded = json.decodeFromString(serializer, json.encodeToString(serializer, backStack))

        assertThat(decoded).containsExactly(*backStack.toTypedArray())
    }

    @Test
    fun cada_navkey_aparece_uma_vez_so() {
        val names = allNavKeys.map { it.type.simpleName }

        assertThat(names.groupingBy { it }.eachCount().filterValues { it > 1 })
            .isEqualTo(emptyMap())
    }

    @Test
    fun navkey_sem_registro_falha() {
        assertFailure { json.encodeToString(navKeySerializer, UnregisteredKey) }
            .isInstanceOf(SerializationException::class)
    }

    @Serializable
    private data object UnregisteredKey : NavKey
}
