package ru.internet.boardgames.spygame

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

/**
 * JUnit4-правило, заменяющее [kotlinx.coroutines.Dispatchers.Main] на [kotlinx.coroutines.test.TestDispatcher] на время теста.
 *
 * Необходимо для тестирования [ViewModel.viewModelScope], который внутри
 * использует `Dispatchers.Main.immediate`.
 *
 * Использование:
 * ```kotlin
 * @get:Rule
 * val mainDispatcherRule = MainDispatcherRule()
 *
 * @Test
 * fun test() = runTest(mainDispatcherRule.testDispatcher) {
 *     advanceTimeBy(5_001L)  // Виртуальное время, shared с viewModelScope
 * }
 * ```
 *
 * @param testDispatcher По умолчанию [kotlinx.coroutines.test.UnconfinedTestDispatcher] — корутины
 *   выполняются немедленно без явного вызова [advanceUntilIdle].
 *   Передайте [StandardTestDispatcher] если нужен точный контроль порядка выполнения.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()
) : TestWatcher() {

    override fun starting(description: Description) {
        Dispatchers.setMain(testDispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}