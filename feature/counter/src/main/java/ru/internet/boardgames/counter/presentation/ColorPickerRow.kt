package ru.internet.boardgames.counter.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

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
 * Выбор цвета счётчика: сетка 2 строки × 5 цветов.
 *
 * Выбранный цвет отмечается рамкой 3dp цвета [MaterialTheme.colorScheme.onSurface] —
 * без галочки, без изменения размера кружка.
 *
 * Используется в [NewCounterDialog] и в EditCounterScreen.
 * Сигнатура не изменилась — вызывающий код менять не нужно.
 */
@Composable
internal fun ColorPickerRow(
    selectedColorArgb: Long,
    onColorSelected: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    // 10 цветов → 2 строки по 5
    val rows = counterColorPalette.chunked(5)

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        rows.forEach { rowColors ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                rowColors.forEach { colorArgb ->
                    val isSelected = colorArgb == selectedColorArgb
                    Box(
                        modifier = Modifier
                            .size(40.dp)
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
    }
}
