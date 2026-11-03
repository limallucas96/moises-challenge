package cmp.skeleton.app.core.testing

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

/**
 * runTest com Dispatchers.Main trocado por UnconfinedTestDispatcher, como a skill
 * android-unit-testing pede para ViewModels (viewModelScope roda em Main).
 * Crie o ViewModel DENTRO do bloco.
 */
fun runViewModelTest(testBody: suspend TestScope.() -> Unit): TestResult {
    val dispatcher = UnconfinedTestDispatcher()
    Dispatchers.setMain(dispatcher)
    try {
        return runTest(dispatcher) { testBody() }
    } finally {
        Dispatchers.resetMain()
    }
}
