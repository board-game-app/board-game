package ru.internet.boardgames.spygame.presentation.game.components

import android.content.res.Configuration
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import androidx.compose.ui.zIndex
import ru.internet.boardgames.spygame.domain.model.Category
import ru.internet.boardgames.spygame.domain.model.GameCard
import ru.internet.boardgames.spygame.domain.model.GameSession
import ru.internet.boardgames.spygame.presentation.game.CardUiState
import ru.internet.boardgames.spygame.presentation.theme.SpyGameTheme

/** Максимальное количество карточек, видимых в стопке (не считая верхнюю). */
private const val MAX_STACK_VISIBLE = 2

/** Вертикальное смещение каждого следующего «слоя» стопки. */
private val STACK_Y_OFFSET = 8.dp

/** Уменьшение масштаба для каждого слоя (0.03 = 3% на уровень). */
private const val STACK_SCALE_STEP = 0.03f

/**
 * Визуальная стопка карточек.
 *
 * Отображает:
 * - [MAX_STACK_VISIBLE] + 1 карточек в стопке со смещением вниз и небольшим
 *   уменьшением масштаба — для эффекта глубины.
 * - Только верхняя карточка (наименьший индекс среди STACKED) реагирует
 *   на тап для reveal.
 * - REVEALED-карточка рендерится поверх стопки, SpyCard сам управляет её
 *   анимацией (scale ×1.05, elevation 16dp, флип).
 * - DISMISSED-карточки остаются в дереве композиции, пока SpyCard не
 *   завершит анимацию выхода (slide + fade, 300 мс). После этого у них
 *   alpha = 0 и нет обработчиков ввода.
 *
 * @param session            Активная игровая сессия.
 * @param cardStates         Список состояний карточек (позиция = индекс в session.cards).
 * @param topStackedPosition Индекс верхней карточки стопки (из GameUiState).
 * @param timerProgress      Прогресс таймера для REVEALED-карточки.
 * @param onCardTap          Тап по карточке → GameViewModel.onCardTap(position).
 * @param onCardSwiped       Свайп за порог → GameViewModel.onCardSwiped(position).
 */
@Composable
fun CardStack(
    session: GameSession,
    cardStates: List<CardUiState>,
    topStackedPosition: Int?,
    timerProgress: Float,
    onCardTap: (Int) -> Unit,
    onCardSwiped: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        // Ширина карточки = 65% ширины контейнера, но не более 260dp
        val cardWidth = minOf(maxWidth * 0.65f, 260.dp)
        // Стандартное соотношение игральной карты 5:7 ≈ 0.714
        val cardHeight = cardWidth / 0.714f
        // Высота контейнера стопки = высота карты + смещения видимых слоёв
        val stackHeight = cardHeight + (MAX_STACK_VISIBLE * STACK_Y_OFFSET)


        val revealedPosition: Int? = cardStates
            .indexOfFirst { it == CardUiState.REVEALED }
            .takeIf { it >= 0 }

        val depthReferencePosition: Int? = revealedPosition ?: topStackedPosition

        Box(
            modifier = Modifier
                .width(cardWidth)
                .height(stackHeight),
            contentAlignment = Alignment.TopCenter
        ) {
            session.cards.indices.reversed().forEach { position ->
                val state = cardStates.getOrElse(position) { CardUiState.DISMISSED }


                val depthBelow: Int = when {
                    state == CardUiState.REVEALED  -> 0
                    state == CardUiState.DISMISSED -> 0
                    depthReferencePosition == null -> 0
                    else                           -> position - depthReferencePosition
                }

                if (state == CardUiState.STACKED && depthBelow > MAX_STACK_VISIBLE) return@forEach

                val zIndex = when (state) {
                    CardUiState.REVEALED  -> session.totalPlayers.toFloat() + 1f
                    // DISMISSED-карточки должны быть НИЖЕ любой STACKED-карточки.
                    // Минимальный zIndex у STACKED = totalPlayers − MAX_STACK_VISIBLE ≥ 1.
                    // Если zIndex DISMISSED > zIndex верхней STACKED-карточки,
                    // невидимые (alpha=0) DISMISSED-узлы перехватывают тач-события —
                    // именно это блокировало тап по карточке на позиции 2 при playerCount > 4.
                    CardUiState.DISMISSED -> 0f
                    CardUiState.STACKED   -> (session.totalPlayers - depthBelow).toFloat()
                }

                // key(position) гарантирует, что экземпляр SpyCard (и все его
                // remember-состояния: анимации alpha, translationX, флипа и т.д.)
                // всегда привязан к КОНКРЕТНОЙ карточке, а не к порядковому номеру
                // вызова в цикле.
                //
                // БЕЗ key: когда меняется набор отфильтрованных карточек
                // (depthBelow пересекает MAX_STACK_VISIBLE), Compose сдвигает
                // порядковые номера вызовов. SpyCard для новой позиции N
                // унаследует remember-состояние от той карточки, которая раньше
                // занимала этот порядковый номер — например, анимацию вылета
                // уже DISMISSED-карточки. Это и вызывало «призрак» под 5-й
                // карточкой при playerCount > 4.
                key(position) {
                    // Плавно анимируем глубину в стопке.
                    // Без этого, когда верхняя карточка уходит, все нижние
                    // мгновенно прыгают на новое место — следующая карточка
                    // резко «выскакивает» на позицию топа ещё до того, как
                    // предыдущая успела вылететь за экран.
                    val animatedDepth by animateFloatAsState(
                        targetValue   = depthBelow.toFloat(),
                        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
                        label         = "cardDepth_$position"
                    )

                    // Плавно анимируем zIndex.
                    // Без этого DISMISSED-карточка мгновенно получает zIndex = 0
                    // и оказывается позади следующей карточки, которая уже
                    // заняла её место. В итоге анимация вылета проигрывается
                    // «из-под» следующей карточки.
                    // С анимацией уходящая карточка плавно отдаёт лидерство
                    // по z за те же ~300 мс, что идёт её slide-out анимация,
                    // и визуально «вылетает поверх» следующей — как реальная карта.
                    //
                    // Тач-события при этом не блокируются: SpyCard в состоянии
                    // DISMISSED не регистрирует обработчики ввода, поэтому
                    // тапы сквозь уходящую карточку доходят до следующей.
                    val animatedZ by animateFloatAsState(
                        targetValue   = zIndex,
                        animationSpec = tween(durationMillis = 300, easing = LinearEasing),
                        label         = "cardZ_$position"
                    )

                    SpyCard(
                        card          = session.cards[position],
                        cardUiState   = state,
                        isTopCard     = (position == topStackedPosition),
                        timerProgress = if (state == CardUiState.REVEALED) timerProgress else 1f,
                        onTap         = { onCardTap(position) },
                        onDismissed   = { onCardSwiped(position) },
                        modifier      = Modifier
                            .width(cardWidth)
                            .height(cardHeight)
                            .zIndex(animatedZ)
                            .offset(y = STACK_Y_OFFSET * animatedDepth)
                            .graphicsLayer {
                                val depthScale = 1f - animatedDepth * STACK_SCALE_STEP
                                scaleX = depthScale
                                scaleY = depthScale
                            }
                            .semantics {
                                testTag            = "card_$position"
                                contentDescription = "card_${position}_${state.name}"
                            }
                    )
                }
            }
        }
    }
}

// ═════════════════════════════════════════════════════════════════════════════
// Preview helpers
// ═════════════════════════════════════════════════════════════════════════════

/**
 * Генерирует тестовую сессию для превью.
 * Карточка 0 — шпион, остальные видят слово «Пилот».
 */
private fun previewSession(playerCount: Int): GameSession {
    val category = Category(
        id    = "airport",
        name  = "Аэропорт",
        words = listOf("Пилот", "Стюардесса", "Пассажир", "Таможенник", "Охранник")
    )
    return GameSession(
        category     = category,
        totalPlayers = playerCount,
        cards        = List(playerCount) { i ->
            GameCard(
                index        = i + 1,
                isSpy        = (i == 0),
                word         = if (i == 0) GameCard.SPY_WORD_PLACEHOLDER else "Пилот",
                categoryName = "Аэропорт"
            )
        }
    )
}

/**
 * Вспомогательный composable-обёртка для превью:
 * вычисляет [topStackedPosition] из списка состояний, применяет [SpyGameTheme].
 */
@Composable
private fun PreviewCardStack(
    playerCount   : Int,
    states        : List<CardUiState>,
    timerProgress : Float = 1f
) {
    SpyGameTheme {
        val topStacked = states.indexOfFirst { it == CardUiState.STACKED }.takeIf { it >= 0 }
        CardStack(
            session            = previewSession(playerCount),
            cardStates         = states,
            topStackedPosition = topStacked,
            timerProgress      = timerProgress,
            onCardTap          = {},
            onCardSwiped       = {},
            modifier           = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp)
        )
    }
}

// ═════════════════════════════════════════════════════════════════════════════
// Previews — вариации CardUiState
// ═════════════════════════════════════════════════════════════════════════════

/** Начало игры: все карточки в стопке. Видны 3 слоя (топ + 2 тени). */
@Preview(showBackground = true, name = "CardStack — все STACKED (4 игрока)")
@Composable
private fun PreviewAllStacked4() {
    PreviewCardStack(
        playerCount = 4,
        states      = List(4) { CardUiState.STACKED }
    )
}

/** Верхняя карточка перевёрнута, таймер на 60%. */
@Preview(showBackground = true, name = "CardStack — верхняя REVEALED (4 игрока)")
@Composable
private fun PreviewTopRevealed4() {
    PreviewCardStack(
        playerCount   = 4,
        states        = listOf(
            CardUiState.REVEALED,
            CardUiState.STACKED,
            CardUiState.STACKED,
            CardUiState.STACKED
        ),
        timerProgress = 0.6f
    )
}

/**
 * Середина игры при 5 игроках:
 * 0, 1 — сброшены; 2 — перевёрнута (таймер 30%); 3, 4 — в стопке.
 * Проверяет корректный расчёт depthBelow при ненулевом revealedPosition.
 */
@Preview(showBackground = true, name = "CardStack — середина игры (5 игроков)")
@Composable
private fun PreviewMidGame5() {
    PreviewCardStack(
        playerCount   = 5,
        states        = listOf(
            CardUiState.DISMISSED,
            CardUiState.DISMISSED,
            CardUiState.REVEALED,
            CardUiState.STACKED,
            CardUiState.STACKED
        ),
        timerProgress = 0.3f
    )
}

/**
 * Предпоследняя карточка: 4 сброшены, остался один топ.
 * Проверяет, что стопка из одной карточки отображается без теней-слоёв.
 */
@Preview(showBackground = true, name = "CardStack — последняя STACKED (5 игроков)")
@Composable
private fun PreviewLastCard5() {
    PreviewCardStack(
        playerCount = 5,
        states      = listOf(
            CardUiState.DISMISSED,
            CardUiState.DISMISSED,
            CardUiState.DISMISSED,
            CardUiState.DISMISSED,
            CardUiState.STACKED
        )
    )
}

/**
 * Последняя карточка уже перевёрнута, таймер почти истёк.
 * Финальное «живое» состояние перед уходом в GameComplete.
 */
@Preview(showBackground = true, name = "CardStack — последняя REVEALED, таймер истекает")
@Composable
private fun PreviewLastRevealed5() {
    PreviewCardStack(
        playerCount   = 5,
        states        = listOf(
            CardUiState.DISMISSED,
            CardUiState.DISMISSED,
            CardUiState.DISMISSED,
            CardUiState.DISMISSED,
            CardUiState.REVEALED
        ),
        timerProgress = 0.05f
    )
}

// ═════════════════════════════════════════════════════════════════════════════
// Previews — тёмная тема
// ═════════════════════════════════════════════════════════════════════════════

@Preview(
    showBackground = true,
    name           = "CardStack — все STACKED, тёмная тема",
    uiMode         = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun PreviewAllStackedDark() {
    PreviewCardStack(
        playerCount = 4,
        states      = List(4) { CardUiState.STACKED }
    )
}

@Preview(
    showBackground = true,
    name           = "CardStack — REVEALED, тёмная тема",
    uiMode         = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun PreviewRevealedDark() {
    PreviewCardStack(
        playerCount   = 4,
        states        = listOf(
            CardUiState.REVEALED,
            CardUiState.STACKED,
            CardUiState.STACKED,
            CardUiState.STACKED
        ),
        timerProgress = 0.5f
    )
}
