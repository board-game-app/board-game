package ru.internet.boardgames.counter.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.ui.unit.dp

/**
 * Переиспользуемая кнопка-таблетка быстрого действия.
 * Используется в [ActionButtonsRow] (wide) и в CompactActionGrid (compact).
 */
@Composable
internal fun ActionChip(
    step: Int,
    counterColor: Color,
    onAction: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val label = if (step >= 0) "+$step" else "$step"
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(counterColor.copy(alpha = 0.18f))
            .clickable { onAction(step) }
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = counterColor,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * FlowRow кнопок-таблеток для широкой карточки ([WideCounterCard]).
 * Кнопки переносятся на новую строку если не влезают.
 *
 * Для компактной карточки используется CompactActionGrid
 * (2 колонки, определена внутри CompactCounterCard.kt).
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun ActionButtonsRow(
    actions: List<Int>,
    counterColor: Color,
    onAction: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        actions.forEach { step ->
            ActionChip(
                step = step,
                counterColor = counterColor,
                onAction = onAction
            )
        }
    }
}
