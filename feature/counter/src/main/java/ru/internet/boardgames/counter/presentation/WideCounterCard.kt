package ru.internet.boardgames.counter.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
 * Широкая горизонтальная карточка-«таблетка» счётчика (§5.2 ТЗ).
 *
 * Структура:
 *   • Название над таблеткой (~14sp).
 *   • Горизонтальная «таблетка» во всю ширину:
 *       [−] (тёмная кнопка слева) | значение (~40sp, центр) | [+] (тёмная кнопка справа)
 *       Фон = цвет счётчика.
 *   • Кнопки быстрых действий (§5.3), если actions не пусты.
 *
 * Жесты (Изменение 1):
 *   • Тап на таблетку (кроме кнопок −/+) → открыть EditCounterScreen ([onTap]).
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

    Column(modifier = modifier.fillMaxWidth()) {
        // ── Название над таблеткой (§5.2) ────────────────────────────────────
        Text(
            text = counter.name,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 8.dp, bottom = 6.dp, end = 8.dp)
        )

        // ── Таблетка (§5.2) ───────────────────────────────────────────────────
        // combinedClickable на всей таблетке: тап = редактировать, долгий тап = сброс.
        // Кнопки −/+ внутри имеют свой clickable и поглощают события — до Row не дойдут.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .clip(RoundedCornerShape(36.dp))
                .background(counterColor)
                .combinedClickable(
                    onClick = onTap,
                    onLongClick = onLongPress
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // ── Кнопка «−» (тёмный полупрозрачный кружок, слева) ────────────
            Box(
                modifier = Modifier
                    .padding(start = 8.dp)
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.28f))
                    .clickable(onClick = onDecrement),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "−",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White
                )
            }

            // ── Значение (центр, ~40sp) ───────────────────────────────────────
            // Клика нет — обрабатывается combinedClickable родительского Row
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = counter.value.toString(),
                    style = MaterialTheme.typography.displaySmall, // ~40sp Bold
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
            }

            // ── Кнопка «+» (тёмный полупрозрачный кружок, справа) ───────────
            Box(
                modifier = Modifier
                    .padding(end = 8.dp)
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.28f))
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
