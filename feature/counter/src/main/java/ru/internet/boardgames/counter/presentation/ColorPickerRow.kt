package ru.internet.boardgames.counter.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Единая палитра цветов счётчика — 10 цветов.
 *
 * Используется в [NewCounterDialog] и [EditCounterScreen].
 * Порядок: тёплые → холодные → нейтральные.
 * [DEFAULT_COUNTER_COLOR_ARGB] (оранжевый) присутствует в палитре.
 */
internal val counterColorPalette: List<Long> = listOf(
    0xFFE53935L, // Красный
    0xFF1E88E5L, // Синий
    0xFFF5A623L, // Оранжевый   (дефолтный §DEFAULT_COUNTER_COLOR_ARGB)
    0xFF43A047L, // Зелёный
    0xFF00ACC1L, // Бирюзовый
    0xFF3949ABL, // Индиго
    0xFFFDD835L, // Жёлтый
    0xFF8E24AAL, // Фиолетовый
    0xFFEC407AL, // Розовый
    0xFF6D4C41L  // Коричневый
)

/**
 * Горизонтально прокручиваемая строка цветовых кружков (36 dp).
 *
 * Активный цвет обведён рамкой [MaterialTheme.colorScheme.onSurface].
 * Скроллится, если кружки не помещаются в ширину — ни один не сплющивается.
 *
 * Используется в [NewCounterDialog] и [EditCounterScreen].
 */
@Composable
internal fun ColorPickerRow(
    selectedColorArgb: Long,
    onColorSelected: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        counterColorPalette.forEach { colorArgb ->
            val isSelected = colorArgb == selectedColorArgb
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color(colorArgb))
                    .then(
                        if (isSelected) Modifier.border(
                            width = 3.dp,
                            color = MaterialTheme.colorScheme.onSurface,
                            shape = CircleShape
                        ) else Modifier
                    )
                    .clickable { onColorSelected(colorArgb) }
            )
        }
    }
}
