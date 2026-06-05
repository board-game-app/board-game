package ru.internet.boardgames.spygame.presentation.game

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import ru.internet.boardgames.spygame.MainDispatcherRule
import ru.internet.boardgames.spygame.domain.model.AppSettings
import ru.internet.boardgames.spygame.domain.model.Category
import ru.internet.boardgames.spygame.domain.usecase.GenerateGameSessionUseCase
import ru.internet.boardgames.spygame.domain.usecase.GetRandomCategoryUseCase
import ru.internet.boardgames.spygame.domain.usecase.GetSettingsUseCase

/**
 * Unit-тесты для [GameViewModel].
 *
 * Зависимости ([GetRandomCategoryUseCase], [GetSettingsUseCase]) — замоканы через MockK.
 * [GenerateGameSessionUseCase] — используем реальную реализацию (нет внешних зависимостей).
 *
 * Время (таймер) управляется виртуально через [advanceTimeBy] —
 * реальный поток не блокируется.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class GameViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    // ─── Моки зависимостей ────────────────────────────────────────────────────
    private val getRandomCategoryUseCase: GetRandomCategoryUseCase = mockk()
    private val getSettingsUseCase: GetSettingsUseCase             = mockk()
    // Реальная реализация — не мок. Логика генерации сессии уже покрыта отдельным тестом.
    private val generateGameSessionUseCase = GenerateGameSessionUseCase()

    // ─── Тестовые данные ──────────────────────────────────────────────────────
    private val testCategory = Category(
        id    = "airport",
        name  = "Аэропорт",
        words = listOf("Пилот", "Стюардесса", "Пассажир", "Таможенник", "Охранник")
    )
    private val defaultSettings = AppSettings(playerCount = 4, language = "ru")

    // ─────────────────────────────────────────────────────────────────────────

    private fun createViewModel() = GameViewModel(
        getRandomCategoryUseCase   = getRandomCategoryUseCase,
        generateGameSessionUseCase = generateGameSessionUseCase,
        getSettingsUseCase         = getSettingsUseCase
    )

    @Before
    fun setUp() {
        // Стандартный ответ UseCase-ов для большинства тестов
        coEvery { getRandomCategoryUseCase(any(), anyNullable()) } returns testCategory
    }

    // ─── Начальное состояние ──────────────────────────────────────────────────

    @Test
    fun `initial state is loading`() = runTest(mainDispatcherRule.testDispatcher) {
        every { getSettingsUseCase() } returns emptyFlow()
        val vm = createViewModel()
        assertTrue("Начальное состояние должно быть isLoading=true", vm.uiState.value.isLoading)
        assertNull("Сессия должна отсутствовать до загрузки", vm.uiState.value.session)
    }

    // ─── Загрузка сессии ──────────────────────────────────────────────────────

    @Test
    fun `session loads after settings emit`() = runTest(mainDispatcherRule.testDispatcher) {
        every { getSettingsUseCase() } returns flowOf(defaultSettings)
        val vm = createViewModel()
        advanceUntilIdle()

        assertFalse(vm.uiState.value.isLoading)
        assertNotNull(vm.uiState.value.session)
        assertNull("Ошибки не должно быть", vm.uiState.value.error)
    }

    @Test
    fun `loaded session has correct player count`() = runTest(mainDispatcherRule.testDispatcher) {
        every { getSettingsUseCase() } returns flowOf(defaultSettings)
        val vm = createViewModel()
        advanceUntilIdle()

        assertEquals(
            "Число карточек должно совпадать с playerCount",
            defaultSettings.playerCount,
            vm.uiState.value.session?.totalPlayers
        )
        assertEquals(
            "Число состояний карточек должно совпадать с playerCount",
            defaultSettings.playerCount,
            vm.uiState.value.cardStates.size
        )
    }

    @Test
    fun `all cards start as STACKED`() = runTest(mainDispatcherRule.testDispatcher) {
        every { getSettingsUseCase() } returns flowOf(defaultSettings)
        val vm = createViewModel()
        advanceUntilIdle()

        assertTrue(
            "Все карточки должны начинать в состоянии STACKED",
            vm.uiState.value.cardStates.all { it == CardUiState.STACKED }
        )
    }

    @Test
    fun `error state when category load fails`() = runTest(mainDispatcherRule.testDispatcher) {
        every { getSettingsUseCase() } returns flowOf(defaultSettings)
        coEvery { getRandomCategoryUseCase(any(), anyNullable()) } throws
                IllegalStateException("БД пуста")

        val vm = createViewModel()
        advanceUntilIdle()

        assertFalse(vm.uiState.value.isLoading)
        assertNotNull("Ошибка должна быть установлена", vm.uiState.value.error)
        assertNull("Сессия не должна быть установлена при ошибке", vm.uiState.value.session)
    }

    // ─── onCardTap — STACKED → REVEALED ──────────────────────────────────────

    @Test
    fun `tapping top card transitions it to REVEALED`() = runTest(mainDispatcherRule.testDispatcher) {
        every { getSettingsUseCase() } returns flowOf(defaultSettings)
        val vm = createViewModel()
        advanceUntilIdle()

        val topPos = vm.uiState.value.topStackedPosition!!
        vm.onCardTap(topPos)

        assertEquals(
            "Верхняя карточка должна стать REVEALED после тапа",
            CardUiState.REVEALED,
            vm.uiState.value.cardStates[topPos]
        )
    }

    @Test
    fun `tapping non-top stacked card does nothing`() = runTest(mainDispatcherRule.testDispatcher) {
        every { getSettingsUseCase() } returns flowOf(AppSettings(playerCount = 3, language = "ru"))
        val vm = createViewModel()
        advanceUntilIdle()

        val topPos    = vm.uiState.value.topStackedPosition!!
        val nonTopPos = (topPos + 1) % 3
        val statesBefore = vm.uiState.value.cardStates.toList()

        vm.onCardTap(nonTopPos)  // Не верхняя карточка

        assertEquals("Состояния карточек не должны измениться", statesBefore, vm.uiState.value.cardStates)
    }

    @Test
    fun `only one card can be revealed at a time`() = runTest(mainDispatcherRule.testDispatcher) {
        every { getSettingsUseCase() } returns flowOf(AppSettings(playerCount = 4, language = "ru"))
        val vm = createViewModel()
        advanceUntilIdle()

        val topPos = vm.uiState.value.topStackedPosition!!
        vm.onCardTap(topPos)  // Reveal first card

        val revealedCount = vm.uiState.value.cardStates.count { it == CardUiState.REVEALED }
        assertEquals("Только одна карточка может быть открыта", 1, revealedCount)
    }

    // ─── onCardTap — REVEALED → DISMISSED ────────────────────────────────────

    @Test
    fun `second tap on revealed card dismisses it`() = runTest(mainDispatcherRule.testDispatcher) {
        every { getSettingsUseCase() } returns flowOf(defaultSettings)
        val vm = createViewModel()
        advanceUntilIdle()

        val topPos = vm.uiState.value.topStackedPosition!!
        vm.onCardTap(topPos)  // → REVEALED
        vm.onCardTap(topPos)  // → DISMISSED

        assertEquals(
            "Повторный тап по открытой карточке должен её отбросить",
            CardUiState.DISMISSED,
            vm.uiState.value.cardStates[topPos]
        )
    }

    @Test
    fun `timer progress resets to 1f after dismiss`() = runTest(mainDispatcherRule.testDispatcher) {
        every { getSettingsUseCase() } returns flowOf(defaultSettings)
        val vm = createViewModel()
        advanceUntilIdle()

        val topPos = vm.uiState.value.topStackedPosition!!
        vm.onCardTap(topPos)   // Reveal → timer starts
        advanceTimeBy(1_000L)  // Даём таймеру убыть
        vm.onCardTap(topPos)   // Dismiss → timer cancels

        assertEquals(
            "timerProgress должен сброситься в 1f после dismiss",
            1f,
            vm.uiState.value.timerProgress
        )
    }

    // ─── onCardSwiped ─────────────────────────────────────────────────────────

    @Test
    fun `swiping revealed card dismisses it`() = runTest(mainDispatcherRule.testDispatcher) {
        every { getSettingsUseCase() } returns flowOf(defaultSettings)
        val vm = createViewModel()
        advanceUntilIdle()

        val topPos = vm.uiState.value.topStackedPosition!!
        vm.onCardTap(topPos)    // → REVEALED
        vm.onCardSwiped(topPos) // → DISMISSED via swipe

        assertEquals(CardUiState.DISMISSED, vm.uiState.value.cardStates[topPos])
    }

    @Test
    fun `swiping stacked card does nothing`() = runTest(mainDispatcherRule.testDispatcher) {
        every { getSettingsUseCase() } returns flowOf(defaultSettings)
        val vm = createViewModel()
        advanceUntilIdle()

        val statesBefore = vm.uiState.value.cardStates.toList()
        vm.onCardSwiped(0)  // Карточка ещё STACKED

        assertEquals("Свайп по STACKED-карточке не должен менять состояние", statesBefore, vm.uiState.value.cardStates)
    }

    // ─── Таймер ───────────────────────────────────────────────────────────────

    @Test
    fun `timer auto-dismisses card after TIMER_DURATION_MS`() = runTest(mainDispatcherRule.testDispatcher) {
        every { getSettingsUseCase() } returns flowOf(defaultSettings)
        val vm = createViewModel()
        advanceUntilIdle()

        val topPos = vm.uiState.value.topStackedPosition!!
        vm.onCardTap(topPos)  // Reveal + start timer

        advanceTimeBy(GameViewModel.TIMER_DURATION_MS + 100L)

        assertEquals(
            "Карточка должна быть DISMISSED после истечения таймера",
            CardUiState.DISMISSED,
            vm.uiState.value.cardStates[topPos]
        )
    }

    @Test
    fun `timer progress starts at 1f`() = runTest(mainDispatcherRule.testDispatcher) {
        every { getSettingsUseCase() } returns flowOf(defaultSettings)
        val vm = createViewModel()
        advanceUntilIdle()

        val topPos = vm.uiState.value.topStackedPosition!!
        vm.onCardTap(topPos)

        // Сразу после reveal до первого тика таймера прогресс ≈ 1.0
        assertTrue(
            "Прогресс после reveal должен быть близок к 1.0",
            vm.uiState.value.timerProgress > 0.9f
        )
    }

    @Test
    fun `timer progress decreases over time`() = runTest(mainDispatcherRule.testDispatcher) {
        every { getSettingsUseCase() } returns flowOf(defaultSettings)
        val vm = createViewModel()
        advanceUntilIdle()

        val topPos = vm.uiState.value.topStackedPosition!!
        vm.onCardTap(topPos)

        advanceTimeBy(2_500L)  // Половина таймера

        val progress = vm.uiState.value.timerProgress
        assertTrue("Прогресс должен уменьшиться за 2.5с (было ~1.0, стало $progress)", progress < 0.6f)
        assertTrue("Прогресс должен быть > 0 до истечения таймера", progress >= 0f)
    }

    @Test
    fun `manual dismiss before timer expiry cancels timer`() = runTest(mainDispatcherRule.testDispatcher) {
        every { getSettingsUseCase() } returns flowOf(defaultSettings)
        val vm = createViewModel()
        advanceUntilIdle()

        val topPos = vm.uiState.value.topStackedPosition!!
        vm.onCardTap(topPos)   // Reveal
        advanceTimeBy(1_000L)
        vm.onCardTap(topPos)   // Manual dismiss (отменяет таймер)

        // Состояние не должно снова поменяться
        advanceTimeBy(GameViewModel.TIMER_DURATION_MS + 100L)
        assertEquals(
            "После ручного dismiss карточка должна оставаться DISMISSED (не менять состояние снова)",
            CardUiState.DISMISSED,
            vm.uiState.value.cardStates[topPos]
        )
    }

    // ─── refreshGame ──────────────────────────────────────────────────────────

    @Test
    fun `refreshGame loads new session`() = runTest(mainDispatcherRule.testDispatcher) {
        every { getSettingsUseCase() } returns flowOf(defaultSettings)
        val vm = createViewModel()
        advanceUntilIdle()

        vm.refreshGame()
        advanceUntilIdle()

        // Должно быть два вызова: первоначальный + refresh
        coVerify(exactly = 2) { getRandomCategoryUseCase(any(), anyNullable()) }
    }

    @Test
    fun `refreshGame excludes current category id`() = runTest(mainDispatcherRule.testDispatcher) {
        every { getSettingsUseCase() } returns flowOf(defaultSettings)
        val vm = createViewModel()
        advanceUntilIdle()

        val currentCategoryId = vm.uiState.value.session?.category?.id
        vm.refreshGame()
        advanceUntilIdle()

        coVerify {
            getRandomCategoryUseCase(any(), match { it == currentCategoryId })
        }
    }

    @Test
    fun `refreshGame resets all cards to STACKED`() = runTest(mainDispatcherRule.testDispatcher) {
        every { getSettingsUseCase() } returns flowOf(defaultSettings)
        val vm = createViewModel()
        advanceUntilIdle()

        // Открываем и закрываем первую карточку
        val topPos = vm.uiState.value.topStackedPosition!!
        vm.onCardTap(topPos)
        vm.onCardTap(topPos)

        vm.refreshGame()
        advanceUntilIdle()

        assertTrue(
            "После refresh все карточки должны быть STACKED",
            vm.uiState.value.cardStates.all { it == CardUiState.STACKED }
        )
    }

    @Test
    fun `refreshGame while loading is ignored`() = runTest(mainDispatcherRule.testDispatcher) {
        // CompletableDeferred заставит корутину реально подвиснуть на await()
        val loadingGate = CompletableDeferred<Category>()
        coEvery { getRandomCategoryUseCase(any(), anyNullable()) } coAnswers { loadingGate.await() }

        every { getSettingsUseCase() } returns flowOf(defaultSettings)
        val vm = createViewModel()

        // Теперь coroutine реально suspended на await() → isLoading = true
        assertTrue("Загрузка должна идти", vm.uiState.value.isLoading)

        vm.refreshGame()  // isLoading = true → должно игнорироваться

        // Разблокируем загрузку
        loadingGate.complete(testCategory)
        advanceUntilIdle()

        // Только 1 вызов — из init, refreshGame был проигнорирован
        coVerify(exactly = 1) { getRandomCategoryUseCase(any(), anyNullable()) }
    }

    // ─── isGameComplete ───────────────────────────────────────────────────────

    @Test
    fun `isGameComplete is false during active game`() = runTest(mainDispatcherRule.testDispatcher) {
        every { getSettingsUseCase() } returns flowOf(defaultSettings)
        val vm = createViewModel()
        advanceUntilIdle()

        assertFalse(vm.uiState.value.isGameComplete)
    }

    @Test
    fun `isGameComplete is true when all cards dismissed`() = runTest(mainDispatcherRule.testDispatcher) {
        every { getSettingsUseCase() } returns flowOf(AppSettings(playerCount = 2, language = "ru"))
        val vm = createViewModel()
        advanceUntilIdle()

        // Отбрасываем обе карточки
        repeat(2) {
            val top = vm.uiState.value.topStackedPosition ?: return@repeat
            vm.onCardTap(top)  // reveal
            vm.onCardTap(top)  // dismiss
        }

        assertTrue(
            "После dismiss всех карточек isGameComplete должен быть true",
            vm.uiState.value.isGameComplete
        )
    }

    // ─── 5+ игроков: регрессия «3-я карточка не реагирует» ───────────────────

    /**
     * Полный прогон с 5 игроками: каждая карточка должна последовательно
     * пройти STACKED → REVEALED → DISMISSED без «пропусков».
     *
     * Проверяет весь цикл: topStackedPosition движется 0→1→2→3→4→null,
     * каждый тап меняет состояние и в конце isGameComplete = true.
     */
    @Test
    fun `5 players — every card can be revealed and dismissed in order`() =
        runTest(mainDispatcherRule.testDispatcher) {
            every { getSettingsUseCase() } returns flowOf(AppSettings(playerCount = 5, language = "ru"))
            val vm = createViewModel()
            advanceUntilIdle()

            assertEquals("Должно быть ровно 5 карточек", 5, vm.uiState.value.cardStates.size)

            for (expectedIndex in 0..4) {
                val topPos = vm.uiState.value.topStackedPosition
                assertNotNull("Шаг $expectedIndex: topStackedPosition не должен быть null", topPos)
                assertEquals(
                    "Шаг $expectedIndex: topStackedPosition должен быть $expectedIndex",
                    expectedIndex, topPos
                )
                assertEquals(
                    "Шаг $expectedIndex: карточка $expectedIndex должна быть STACKED до тапа",
                    CardUiState.STACKED, vm.uiState.value.cardStates[expectedIndex]
                )

                vm.onCardTap(topPos!!)  // STACKED → REVEALED
                assertEquals(
                    "Шаг $expectedIndex: первый тап должен перевести карточку в REVEALED",
                    CardUiState.REVEALED, vm.uiState.value.cardStates[expectedIndex]
                )

                vm.onCardTap(topPos)    // REVEALED → DISMISSED
                assertEquals(
                    "Шаг $expectedIndex: второй тап должен перевести карточку в DISMISSED",
                    CardUiState.DISMISSED, vm.uiState.value.cardStates[expectedIndex]
                )
            }

            assertNull(
                "После финального dismiss topStackedPosition должен быть null",
                vm.uiState.value.topStackedPosition
            )
            assertTrue(
                "После закрытия всех 5 карточек isGameComplete должен быть true",
                vm.uiState.value.isGameComplete
            )
        }

    /**
     * Регрессионный тест для конкретного баг-репорта:
     * «при playerCount > 4 третья карточка (index 2) перестаёт реагировать на нажатие».
     *
     * Если этот тест проходит, а в приложении баг воспроизводится —
     * причина в UI/Compose слое (z-order, pointerInput, передача position),
     * а не в ViewModel.
     */
    @Test
    fun `5 players — card at index 2 responds after cards 0 and 1 are dismissed`() =
        runTest(mainDispatcherRule.testDispatcher) {
            every { getSettingsUseCase() } returns flowOf(AppSettings(playerCount = 5, language = "ru"))
            val vm = createViewModel()
            advanceUntilIdle()

            // Последовательно закрываем карточки 0 и 1
            repeat(2) { step ->
                val top = vm.uiState.value.topStackedPosition
                assertNotNull("topStackedPosition на шаге $step не должен быть null", top)
                vm.onCardTap(top!!)  // reveal
                vm.onCardTap(top)    // dismiss
            }

            // Проверяем, что состояние после двух dismiss корректно
            assertEquals(CardUiState.DISMISSED, vm.uiState.value.cardStates[0])
            assertEquals(CardUiState.DISMISSED, vm.uiState.value.cardStates[1])
            assertEquals(CardUiState.STACKED,   vm.uiState.value.cardStates[2])
            assertEquals(
                "После dismiss карточек 0 и 1 верхней должна стать карточка 2",
                2, vm.uiState.value.topStackedPosition
            )

            // ← Это и есть «3-я карточка»: должна ответить на тап
            vm.onCardTap(2)
            assertEquals(
                "Карточка на позиции 2 должна стать REVEALED — баг: не реагировала на тап",
                CardUiState.REVEALED, vm.uiState.value.cardStates[2]
            )

            vm.onCardTap(2)
            assertEquals(CardUiState.DISMISSED, vm.uiState.value.cardStates[2])
            assertEquals(
                "После dismiss карточки 2 верхней должна стать карточка 3",
                3, vm.uiState.value.topStackedPosition
            )
        }

    // ─── Реакция на смену настроек ────────────────────────────────────────────

    @Test
    fun `language change triggers session reload`() = runTest(mainDispatcherRule.testDispatcher) {
        val settingsFlow = MutableStateFlow(defaultSettings)
        every { getSettingsUseCase() } returns settingsFlow

        val vm = createViewModel()
        advanceUntilIdle()
        coVerify(exactly = 1) { getRandomCategoryUseCase(any(), anyNullable()) }

        // Меняем язык
        settingsFlow.emit(defaultSettings.copy(language = "en"))
        advanceUntilIdle()

        coVerify(exactly = 2) { getRandomCategoryUseCase(any(), anyNullable()) }
    }

    @Test
    fun `player count change triggers session reload`() = runTest(mainDispatcherRule.testDispatcher) {
        val settingsFlow = MutableStateFlow(defaultSettings)
        every { getSettingsUseCase() } returns settingsFlow

        val vm = createViewModel()
        advanceUntilIdle()
        coVerify(exactly = 1) { getRandomCategoryUseCase(any(), anyNullable()) }

        settingsFlow.emit(defaultSettings.copy(playerCount = 8))
        advanceUntilIdle()

        coVerify(exactly = 2) { getRandomCategoryUseCase(any(), anyNullable()) }
    }

    @Test
    fun `same settings re-emit does not trigger reload`() = runTest(mainDispatcherRule.testDispatcher) {
        val settingsFlow = MutableStateFlow(defaultSettings)
        every { getSettingsUseCase() } returns settingsFlow

        val vm = createViewModel()
        advanceUntilIdle()

        // Эмитируем те же настройки ещё раз
        settingsFlow.emit(defaultSettings)
        advanceUntilIdle()

        // Только один вызов (первоначальный)
        coVerify(exactly = 1) { getRandomCategoryUseCase(any(), anyNullable()) }
    }
}
