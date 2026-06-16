package ru.internet.boardgames.counter.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ru.internet.boardgames.counter.domain.model.Counter
import ru.internet.boardgames.counter.presentation.theme.toCounterColor

/**
 * Широкая карточка для режима списка.
 *
 * Структура:
 *  ┌──────────────────────────────────────────────┐
 *  │                     −                         │ ← 44dp, цвет счётчика
 *  ├─────────────┬────────────────────┬────────────┤
 *  │ отриц. chips│  Название / Число  │ полож. chips│ ← авто высота
 *  │  (2 кол.)  │  тап=ред, долг=сброс│  (2 кол.)  │
 *  ├─────────────┴────────────────────┴────────────┤
 *  │                     +                         │ ← 44dp, цвет счётчика
 *  └──────────────────────────────────────────────┘
 *
 * Chips встроены прямо в среднюю зону, отдельного ActionButtonsRow нет.
 * Если на стороне только одна кнопка — она растягивается на обе колонки.
 * Если actions пустой — средняя зона занимает полную ширину.
 */
@Composable
internal fun WideCounterCard(
    counter: Counter,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onTap: () -> Unit,
    onLongPress: () -> Unit,
    onDeleteRequest: () -> Unit,
    onAction: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val counterColor = counter.colorArgb.toCounterColor()
    val cardBackground = MaterialTheme.colorScheme.surfaceContainer

    // Сортировка: отрицательные по убыванию модуля (−10, −5),
    //             положительные по убыванию значения (+10, +5)
    val negatives = counter.actions.filter { it < 0 }.sortedBy { it }
    val positives = counter.actions.filter { it > 0 }.sortedBy { it }
    val hasActions = negatives.isNotEmpty() || positives.isNotEmpty()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
    ) {

        // ── Верхняя полоса: кнопка «−» ───────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .background(counterColor)
                .clickable(onClick = onDecrement),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "−",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White
            )
        }

        // ── Средняя зона: chips + название/значение + chips ──────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(cardBackground),
            verticalAlignment = Alignment.CenterVertically
        ) {

            // Левая колонка: отрицательные chips
            if (negatives.isNotEmpty()) {
                SideChipGrid(
                    actions = negatives,
                    counterColor = counterColor,
                    onAction = onAction,
                    modifier = Modifier
                        .weight(2f)
                        .padding(horizontal = 6.dp, vertical = 10.dp)
                )
            } else if (hasActions) {
                // Держим пропорцию если с другой стороны есть chips
                Box(modifier = Modifier.weight(2f))
            }

            // Центр: название + значение (тап = редактор, долгое = сброс)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .combinedClickable(
                        onClick = onTap,
                        onLongClick = onLongPress
                    )
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = counter.name,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = counter.value.toString(),
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Visible
                    )
                }
            }

            // Правая колонка: положительные chips
            if (positives.isNotEmpty()) {
                SideChipGrid(
                    actions = positives,
                    counterColor = counterColor,
                    onAction = onAction,
                    modifier = Modifier
                        .weight(2f)
                        .padding(horizontal = 6.dp, vertical = 10.dp)
                )
            } else if (hasActions) {
                Box(modifier = Modifier.weight(2f))
            }
        }

        // ── Нижняя полоса: кнопка «+» ────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .background(counterColor)
                .clickable(onClick = onIncrement),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "+",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White
            )
        }
    }
}

/**
 * Сетка chips для боковой колонки: 2 chips в ряд,
 * если в ряду одна кнопка — занимает обе колонки.
 *
 * Пример для [−10, −5]:
 *  ┌──────┬──────┐
 *  │ -10  │  -5  │
 *  └──────┴──────┘
 *
 * Пример для [−10]:
 *  ┌─────────────┐
 *  │    -10      │
 *  └─────────────┘
 */
@Composable
private fun SideChipGrid(
    actions: List<Int>,
    counterColor: Color,
    onAction: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val rows = actions.chunked(2)

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        rows.forEach { rowItems ->
            if (rowItems.size == 2) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    ActionChip(
                        step = rowItems[0],
                        counterColor = counterColor,
                        onAction = onAction,
                        modifier = Modifier.weight(1f)
                    )
                    ActionChip(
                        step = rowItems[1],
                        counterColor = counterColor,
                        onAction = onAction,
                        modifier = Modifier.weight(1f)
                    )
                }
            } else {
                // Одна кнопка — растягивается на обе колонки
                ActionChip(
                    step = rowItems[0],
                    counterColor = counterColor,
                    onAction = onAction,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
