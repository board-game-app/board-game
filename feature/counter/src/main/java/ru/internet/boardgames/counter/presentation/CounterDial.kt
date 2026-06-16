package ru.internet.boardgames.counter.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Переиспользуемый «циферблат»: цветное кольцо с дуговыми кнопками −/+
 * у внешнего края и центральным диском со значением.
 *
 * Слои (снизу вверх):
 *  1. Прозрачные зоны-кликеры (левая = −, правая = +)
 *  2. Символы −/+ у внешнего края через SpaceBetween — поверх зон, но не
 *     перехватывают клики (Text без handler)
 *  3. Фоновый диск цвета карточки (визуально отделяет кольцо от центра)
 *  4. Значение счётчика + combinedClickable (тап/долгое нажатие) —
 *     перехватывает события в зоне центрального диска
 *
 * @param counterColor   цвет кольца и символов
 * @param cardBackground цвет центрального диска (должен совпадать с фоном карточки)
 * @param diskFraction   доля от общего диаметра для центрального диска; чем меньше —
 *                       тем шире кольцо и заметнее символы
 * @param symbolPadding  горизонтальный отступ символов от краёв кольца
 */
@Composable
internal fun CounterDial(
    value: Int,
    counterColor: Color,
    cardBackground: Color,
    diskFraction: Float,
    symbolStyle: TextStyle,
    valueStyle: TextStyle,
    symbolPadding: Dp,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit,
    onTap: () -> Unit,
    onLongPress: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(counterColor)
    ) {

        // ── Слой 1: зоны клика (прозрачные, весь круг) ───────────────────────
        Row(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable(onClick = onDecrement)
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable(onClick = onIncrement)
            )
        }

        // ── Слой 2: символы у внешнего края кольца ───────────────────────────
        // Text без модификаторов pointer-input — клики проваливаются на Слой 1
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = symbolPadding),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "−",
                style = symbolStyle,
                color = Color.White
            )
            Text(
                text = "+",
                style = symbolStyle,
                color = Color.White
            )
        }

        // ── Слой 3: фоновый диск (визуально закрывает центр кольца) ──────────
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxSize(diskFraction)
                .clip(CircleShape)
                .background(cardBackground)
        )

        // ── Слой 4: значение + тап/долгое нажатие ────────────────────────────
        // combinedClickable здесь поглощает события в зоне диска,
        // не давая им добраться до Слоя 1
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxSize(diskFraction)
                .clip(CircleShape)
                .combinedClickable(
                    onClick = onTap,
                    onLongClick = onLongPress
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = value.toString(),
                style = valueStyle,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Visible
            )
        }
    }
}
