package ru.internet.boardgames.counter.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * FlowRow кнопок быстрых действий под карточкой счётчика (§5.3 ТЗ).
 *
 * Правила отображения:
 *   • value > 0 → подпись «+N»
 *   • value < 0 → подпись «-N»
 *   • Максимум 4 кнопки в строке; при большем количестве — перенос.
 *   • Минимальная ширина 60dp, высота 40dp (§5.3).
 *   • Outlined стиль с рамкой цвета счётчика.
 *
 * @param actions       Отсортированный список значений действий (§8 п.4).
 * @param counterColor  Цвет счётчика — используется для рамки и текста кнопок.
 * @param onAction      Вызывается с delta при нажатии кнопки.
 */
@Composable
internal fun ActionButtonsRow(
    actions: List<Int>,
    counterColor: Color,
    onAction: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    if (actions.isEmpty()) return

    FlowRow(
        modifier = modifier.padding(top = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
        maxItemsInEachRow = 4
    ) {
        actions.forEach { action ->
            val label = if (action > 0) "+$action" else "$action"
            OutlinedButton(
                onClick = { onAction(action) },
                modifier = Modifier
                    .widthIn(min = 60.dp)
                    .heightIn(min = 40.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = counterColor
                ),
                border = androidx.compose.foundation.BorderStroke(
                    width = 1.dp,
                    color = counterColor.copy(alpha = 0.7f)
                ),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}
