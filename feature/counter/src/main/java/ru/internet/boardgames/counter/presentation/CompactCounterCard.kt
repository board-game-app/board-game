package ru.internet.boardgames.counter.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import ru.internet.boardgames.counter.presentation.theme.CounterCardTopBackground
import ru.internet.boardgames.counter.presentation.theme.toCounterColor

/**
 * Компактная вертикальная карточка счётчика (§5.1 ТЗ).
 *
 * Структура:
 *   • Название над карточкой (~14sp, §5.1).
 *   • Верхняя часть: тёмный фон [CounterCardTopBackground], крупное значение (~48sp).
 *   • Нижняя часть: фон = цвет счётчика, кнопки «−» и «+» в равных половинах.
 *   • Кнопки быстрых действий под карточкой (§5.3), если actions не пусты.
 *
 * Жесты (Изменение 1):
 *   • Тап на карточку (кроме кнопок −/+) → открыть EditCounterScreen ([onTap]).
 *   • Долгое нажатие → диалог сброса до resetValue ([onLongPress]).
 *
 * @param onTap          Тап по карточке — перейти на экран редактирования.
 * @param onLongPress    Долгое нажатие — показать диалог сброса.
 * @param onIncrement    Нажатие «+».
 * @param onDecrement    Нажатие «−».
 * @param onDeleteRequest Свайп влево — запросить подтверждение удаления.
 * @param onAction       Нажатие кнопки быстрого действия.
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

    Column(modifier = modifier) {
        // ── Название над карточкой (§5.1) ────────────────────────────────────
        Text(
            text = counter.name,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 4.dp, bottom = 6.dp, end = 4.dp)
        )

        // ── Карточка со скруглёнными углами ~16dp ────────────────────────────
        // combinedClickable на всей карточке: тап = редактировать, долгий тап = сброс
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .combinedClickable(
                    onClick = onTap,
                    onLongClick = onLongPress
                )
        ) {
            // ── Верхняя часть: тёмный фон, крупное значение ──────────────────
            // Клика нет — обрабатывается combinedClickable родительского Column
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.4f)               // пропорция верхней части
                    .background(CounterCardTopBackground),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = counter.value.toString(),
                    style = MaterialTheme.typography.displayMedium, // ~48sp Bold
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
            }

            // ── Нижняя часть: кнопки − / + на цветном фоне ───────────────────
            // Кнопки имеют свой clickable — поглощают событие, не пробрасывают вверх
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(counterColor),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Кнопка «−»
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(vertical = 16.dp)
                        .clickable(onClick = onDecrement),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "−",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White
                    )
                }

                // Разделитель
                Box(
                    modifier = Modifier
                        .background(Color.White.copy(alpha = 0.25f))
                        .padding(horizontal = 0.5.dp, vertical = 8.dp)
                        .fillMaxWidth(fraction = 0.002f)
                )

                // Кнопка «+»
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(vertical = 16.dp)
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

        // ── Кнопки быстрых действий (§5.3) ───────────────────────────────────
        if (counter.actions.isNotEmpty()) {
            ActionButtonsRow(
                actions = counter.actions,
                counterColor = counterColor,
                onAction = onAction,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
            )
        }
    }
}
