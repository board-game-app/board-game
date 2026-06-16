package ru.internet.boardgames.counter.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
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
 * Компактная карточка-«циферблат» для сетки 2 колонки.
 *
 * Структура:
 *  – квадратный [CounterDial]: цветное кольцо с символами −/+
 *    у внешнего края и центральным диском со значением;
 *  – название счётчика под циферблатом;
 *  – [CompactActionGrid]: 2 колонки кнопок быстрых действий
 *    (слева — отрицательные по убыванию модуля,
 *     справа — положительные по убыванию значения).
 */
@Composable
internal fun CompactCounterCard(
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

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(cardBackground)
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ── Циферблат ─────────────────────────────────────────────────────────
        CounterDial(
            value = counter.value,
            counterColor = counterColor,
            cardBackground = cardBackground,
            // 0.52f: кольцо ~24% диаметра с каждой стороны — символы хорошо видны
            diskFraction = 0.52f,
            symbolStyle = MaterialTheme.typography.headlineMedium,
            valueStyle = MaterialTheme.typography.headlineMedium,
            // 16dp отступ: символы смещены к внешнему краю кольца
            symbolPadding = 14.dp,
            onDecrement = onDecrement,
            onIncrement = onIncrement,
            onTap = onTap,
            onLongPress = onLongPress,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
        )

        // ── Название ──────────────────────────────────────────────────────────
        Text(
            text = counter.name,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        )

        // ── Кнопки быстрых действий ───────────────────────────────────────────
        if (counter.actions.isNotEmpty()) {
            CompactActionGrid(
                actions = counter.actions,
                counterColor = counterColor,
                onAction = onAction,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            )
        }
    }
}

/**
 * Сетка кнопок быстрых действий для компактной карточки.
 *
 * Всегда 2 колонки:
 *  – левая: отрицательные действия, сортировка по убыванию модуля (−10, −5, ...)
 *  – правая: положительные действия, сортировка по убыванию значения (+10, +5, ...)
 */
@Composable
private fun CompactActionGrid(
    actions: List<Int>,
    counterColor: Color,
    onAction: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    // sortedascending даёт [-10, -5] (наибольший модуль первым)
    val negatives = actions.filter { it < 0 }.sortedBy { it }
    // sortedDescending даёт [10, 5]
    val positives = actions.filter { it > 0 }.sortedByDescending { it }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Левая колонка — отрицательные
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            negatives.forEach { step ->
                ActionChip(
                    step = step,
                    counterColor = counterColor,
                    onAction = onAction,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        // Правая колонка — положительные
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            positives.forEach { step ->
                ActionChip(
                    step = step,
                    counterColor = counterColor,
                    onAction = onAction,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
