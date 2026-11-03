package cmp.skeleton.app.feature.alpha.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cmp.skeleton.app.core.domain.Result
import cmp.skeleton.app.core.presentation.toUiText
import cmp.skeleton.app.feature.alpha.domain.GreetingRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class AlphaViewModel(private val repository: GreetingRepository) : ViewModel() {

    private val _state = MutableStateFlow(AlphaState())
    val state = _state.asStateFlow()

    private var loadJob: Job? = null

    init {
        load()
    }

    fun onAction(action: AlphaAction) {
        when (action) {
            AlphaAction.OnRefreshClick -> load()
        }
    }

    private fun load() {
        if (loadJob?.isActive == true) return
        _state.update { it.copy(isLoading = true, error = null) }
        loadJob = viewModelScope.launch {
            when (val result = repository.getGreeting()) {
                is Result.Success -> _state.update {
                    it.copy(isLoading = false, greeting = result.data.text)
                }

                is Result.Error -> _state.update {
                    it.copy(isLoading = false, error = result.error.toUiText())
                }
            }
        }
    }
}
