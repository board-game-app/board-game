package ru.internet.boardgames.soundquiz.presentation.game.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ru.internet.boardgames.soundquiz.domain.model.CardStack

/**
 * Карточка стопки категорий на игровом поле.
 *
 * Рубашка стопки: цветной фон (colorArgb), название категории,
 * счётчик оставшихся карт.
 * Пустая стопка (words.isEmpty): серый цвет, недоступна для нажатия.
 */
@Composable
fun CategoryStackCard(
    stack: CardStack,
    isEnabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isEmpty = stack.words.isEmpty()
    val cardColor = if (isEmpty) MaterialTheme.colorScheme.surfaceVariant
                    else Color(stack.colorArgb)
    val textColor = if (isEmpty) MaterialTheme.colorScheme.onSurfaceVariant
                    else Color.White

    ElevatedCard(
        onClick = onClick,
        enabled = isEnabled && !isEmpty,
        modifier = modifier.aspectRatio(0.75f),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = cardColor,
            disabledContainerColor = cardColor
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Иконка ♪ — ассоциация со звуком (только для непустых стопок)
            if (!isEmpty) {
                Text(
                    text = "♪",
                    style = MaterialTheme.typography.headlineSmall,
                    color = textColor.copy(alpha = 0.7f)
                )
                Spacer(Modifier.height(4.dp))
            }
            Text(
                text = stack.category.name,
                style = MaterialTheme.typography.titleSmall,
                color = textColor,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(8.dp))
            // Счётчик оставшихся карт
            Text(
                text = stack.words.size.toString(),
                style = MaterialTheme.typography.headlineMedium,
                color = textColor.copy(alpha = if (isEmpty) 0.4f else 1f),
                textAlign = TextAlign.Center
            )
        }
    }
}
