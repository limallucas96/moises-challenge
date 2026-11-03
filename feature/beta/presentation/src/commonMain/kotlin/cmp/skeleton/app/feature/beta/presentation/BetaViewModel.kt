package cmp.skeleton.app.feature.beta.presentation

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/** [message] chega pela NavKey: é o argumento que a Alpha passou ao abrir esta tela. */
internal class BetaViewModel(message: String) : ViewModel() {

    private val _state = MutableStateFlow(BetaState(message = message))
    val state = _state.asStateFlow()

    fun onAction(action: BetaAction) {
        when (action) {
            BetaAction.OnTapClick -> _state.update { it.copy(tapCount = it.tapCount + 1) }
        }
    }
}
