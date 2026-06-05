package ru.internet.boardgames.spygame.presentation.game.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import androidx.compose.ui.zIndex
import ru.internet.boardgames.spygame.domain.model.GameSession
import ru.internet.boardgames.spygame.presentation.game.CardUiState

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
                     CardUiState.DISMISSED -> 0f
                    CardUiState.STACKED   -> (session.totalPlayers - depthBelow).toFloat()
                }

                key(position) {
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
                            .zIndex(zIndex)
                            .offset(y = STACK_Y_OFFSET * depthBelow)
                            .graphicsLayer {
                                val depthScale = 1f - depthBelow * STACK_SCALE_STEP
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
