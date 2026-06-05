package ru.internet.boardgames.spygame.presentation.game.components

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.click
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.printToLog
import androidx.compose.ui.test.swipeRight
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import ru.internet.boardgames.spygame.domain.model.Category
import ru.internet.boardgames.spygame.domain.model.GameCard
import ru.internet.boardgames.spygame.domain.model.GameSession
import ru.internet.boardgames.spygame.presentation.game.CardUiState
import ru.internet.boardgames.spygame.presentation.game.CardUiState.DISMISSED
import ru.internet.boardgames.spygame.presentation.game.CardUiState.REVEALED
import ru.internet.boardgames.spygame.presentation.game.CardUiState.STACKED
import ru.internet.boardgames.spygame.presentation.theme.SpyGameTheme

/**
 * Регрессионный тест для бага:
 * «При playerCount > 4 карточка на позиции 2 не реагирует на тап
 * после того, как карточки 0 и 1 были закрыты».
 *
 * ## Почему тестируем CardStack, а не GameScreen
 *
 * Unit-тест GameViewModel проходит — баг в Compose-слое.
 * Изолируем CardStack: подаём состояние через MutableState и
 * перехватываем onCardTap-колбэки без реального ViewModel/Hilt.
 *
 * ## Таймер
 *
 * timerProgress = 1f (начальное значение) — карточки не закрываются
 * автоматически, так как таймер управляется ViewModel'ом вне этого теста.
 *
 * ## Проверка состояния
 *
 * Состояние карточки кодируется в contentDescription:
 * "card_${position}_${state.name}" (добавлено в CardStack.kt).
 * Формат: "card_2_STACKED", "card_2_REVEALED", "card_2_DISMISSED".
 */
@RunWith(AndroidJUnit4::class)
class CardStackRegressionTest {

    @get:Rule
    val rule = createComposeRule()

    // ─── Helpers ────────────────────────────────────────────────────────────

    /**
     * Создаёт тестовую сессию на [playerCount] игроков.
     * Первый игрок (индекс 0) — шпион; остальные видят слово категории.
     */
    private fun fakeSession(playerCount: Int): GameSession {
        // FIX: Category требует три параметра — words был пропущен, что давало compile error.
        val category = Category(
            id    = "airport",
            name  = "Аэропорт",
            words = listOf("Пилот", "Стюардесса", "Пассажир", "Таможенник", "Охранник")
        )
        return GameSession(
            category     = category,
            cards        = List(playerCount) { i ->
                GameCard(
                    index        = i + 1,
                    isSpy        = (i == 0),
                    word         = if (i == 0) GameCard.SPY_WORD_PLACEHOLDER else "Пилот",
                    categoryName = "Аэропорт"
                )
            },
            totalPlayers = playerCount
        )
    }

    /** Возвращает contentDescription, которую CardStack устанавливает для позиции. */
    private fun cardDesc(position: Int, state: CardUiState) =
        "card_${position}_${state.name}"

    // ─── Состояние хоста ────────────────────────────────────────────────────

    /**
     * Мутабельное состояние снаружи setContent.
     * Изменение .value вызывает рекомпозицию CardStack.
     */
    private lateinit var cardStates: MutableState<List<CardUiState>>

    /** Лог всех вызовов onCardTap — для проверки корректности позиции. */
    private val tappedPositions = mutableListOf<Int>()

    /** Лог всех вызовов onCardSwiped. */
    private val swipedPositions = mutableListOf<Int>()

    @Before
    fun clearLogs() {
        tappedPositions.clear()
        swipedPositions.clear()
    }

    /**
     * Рендерит CardStack с заданным начальным состоянием.
     *
     * onCardTap:
     *   1. Записывает позицию в [tappedPositions].
     *   2. Обновляет [cardStates], имитируя поведение ViewModel
     *      (STACKED → REVEALED, REVEALED → DISMISSED).
     *
     * Таймер отключён: timerProgress всегда 1f.
     */
    private fun renderCardStack(initialStates: List<CardUiState>) {
        cardStates = mutableStateOf(initialStates)

        rule.setContent {
            SpyGameTheme {
                val states = cardStates.value
                val topStacked = states
                    .indexOfFirst { it == STACKED }
                    .takeIf { it >= 0 }

                CardStack(
                    session            = fakeSession(playerCount = initialStates.size),
                    cardStates         = states,
                    topStackedPosition = topStacked,
                    timerProgress      = 1f,
                    onCardTap          = { position ->
                        tappedPositions += position
                        val current = cardStates.value[position]
                        cardStates.value = cardStates.value
                            .toMutableList()
                            .apply {
                                this[position] = when (current) {
                                    STACKED   -> REVEALED
                                    REVEALED  -> DISMISSED
                                    DISMISSED -> DISMISSED
                                }
                            }
                    },
                    onCardSwiped       = { position ->
                        swipedPositions += position
                        cardStates.value = cardStates.value
                            .toMutableList()
                            .apply { this[position] = DISMISSED }
                    }
                )
            }
        }

        rule.waitForIdle()
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Сценарий 1 — основной баг-репорт
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Карточки 0 и 1 уже DISMISSED. Верхняя — карточка 2.
     *
     * Ожидается: тап по "card_2" вызывает onCardTap(2) → карточка переходит в REVEALED.
     * Если баг присутствует: тап не доходит до onCardTap (zIndex или pointerInput назначен
     * неверно), и карточка 2 остаётся в STACKED.
     */
    @Test
    fun cardAtIndex2IsTappableAfterCards0And1Dismissed() {
        renderCardStack(
            initialStates = listOf(DISMISSED, DISMISSED, STACKED, STACKED, STACKED)
        )

        rule.onNodeWithTag("card_2").printToLog("BEFORE_TAP_card_2")

        rule.onNodeWithTag("card_2").performTouchInput { click() }
        rule.waitForIdle()

        assertThat(tappedPositions)
            .containsExactly(2)
            .inOrder()

        rule.onNode(hasContentDescription(cardDesc(2, REVEALED)))
            .assertIsDisplayed()

        rule.onNode(hasContentDescription(cardDesc(3, STACKED)))
            .assertIsDisplayed()
        rule.onNode(hasContentDescription(cardDesc(4, STACKED)))
            .assertIsDisplayed()
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Сценарий 2 — полный прогон 5 карточек
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Каждая из 5 карточек последовательно reveal → dismiss.
     *
     * Если хотя бы одна карточка не реагирует на тап (зависает в STACKED),
     * тест упадёт на проверке состояния этой карточки.
     */
    @Test
    fun all5CardsAreTappableInSequence() {
        renderCardStack(
            initialStates = List(5) { STACKED }
        )


        for (position in 0..4) {
            rule.onNodeWithTag("card_$position").performTouchInput { click() }

            // Продвигаем на: delay(150) + animateTo(200) + animateTo(200) + запас
            rule.mainClock.advanceTimeBy(700L)
            rule.waitForIdle()

            rule.onNode(hasContentDescription(cardDesc(position, REVEALED)))
                .assertIsDisplayed()

            rule.onNodeWithTag("card_$position").performTouchInput { click() }
            rule.mainClock.advanceTimeBy(400L) // dismiss-анимация 300мс + запас
            rule.waitForIdle()

            rule.onNode(hasContentDescription(cardDesc(position, DISMISSED)))
                .assertExists()
        }

        assertThat(tappedPositions).hasSize(10) // 2 тапа × 5 карточек
        assertThat(cardStates.value).containsExactly(
            DISMISSED, DISMISSED, DISMISSED, DISMISSED, DISMISSED
        )
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Сценарий 3 — негативный: тап по не-верхней карточке игнорируется
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Верхняя карточка — 2. Тап по карточке 3 (не верхней) не должен ничего делать.
     *
     * SpyCard выставляет isTopCard = false для позиции 3, pointerInput возвращает
     * управление без detectTapGestures. Проверяем, что onCardTap не вызван.
     */
    @Test
    fun nonTopStackedCardDoesNotRespondToTap() {
        renderCardStack(
            initialStates = listOf(DISMISSED, DISMISSED, STACKED, STACKED, STACKED)
        )

        rule.onNodeWithTag("card_3").performTouchInput { click() }
        rule.waitForIdle()

        assertThat(tappedPositions).doesNotContain(3)

        rule.onNode(hasContentDescription(cardDesc(3, STACKED)))
            .assertIsDisplayed()

        rule.onNode(hasContentDescription(cardDesc(2, STACKED)))
            .assertIsDisplayed()
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Сценарий 4 — свайп закрывает открытую карточку
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Карточка 2 открыта (REVEALED). Свайп вправо за порог (> 80dp)
     * должен вызвать onCardSwiped(2) → DISMISSED.
     *
     * Проверяет, что gesture-детектор в SpyCard корректно определяет позицию
     * и вызывает нужный колбэк.
     */
    @Test
    fun revealedCardIsDismissedBySwipe() {
        renderCardStack(
            initialStates = listOf(DISMISSED, DISMISSED, REVEALED, STACKED, STACKED)
        )

        rule.onNodeWithTag("card_2").printToLog("BEFORE_SWIPE_card_2")

        rule.onNodeWithTag("card_2").performTouchInput {
            swipeRight(
                startX         = centerX,
                endX           = centerX + 150.dp.toPx(),
                durationMillis = 200
            )
        }
        rule.waitForIdle()

        assertThat(swipedPositions).containsExactly(2)

        rule.onNode(hasContentDescription(cardDesc(2, DISMISSED)))
            .assertExists()

        rule.onNode(hasContentDescription(cardDesc(3, STACKED))).assertIsDisplayed()
        rule.onNode(hasContentDescription(cardDesc(4, STACKED))).assertIsDisplayed()
    }
}
