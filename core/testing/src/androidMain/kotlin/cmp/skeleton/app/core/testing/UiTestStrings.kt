package cmp.skeleton.app.core.testing

import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.PluralStringResource
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getPluralString
import org.jetbrains.compose.resources.getString

/**
 * O texto de uma string resource, para o teste de UI procurar o nó pelo mesmo texto que a tela
 * mostra (skill android-ui-testing). As strings do projeto são Compose Resources, então não há
 * `R.string` nem `activity.getString`.
 */
fun text(
    resource: StringResource,
    vararg args: Any,
): String = runBlocking { getString(resource, *args) }

/** O mesmo para plurais: [quantity] escolhe a forma e [args] preenche o texto. */
fun pluralText(
    resource: PluralStringResource,
    quantity: Int,
    vararg args: Any,
): String = runBlocking { getPluralString(resource, quantity, *args) }
